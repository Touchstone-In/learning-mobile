import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import * as SecureStore from 'expo-secure-store';
import { authApi } from '../api/auth';
import { clearTokens, SECURE_KEYS, saveTokens } from '../api/client';
import type { AuthUserDto, LoginResponse } from '../types/api';

// ── Types ────────────────────────────────────────────────────────────────────

interface AuthState {
  isLoading: boolean;
  isCheckingSession: boolean;
  isLoggedIn: boolean;
  user: AuthUserDto | null;
  accessToken: string | null;
  /** Set when login succeeds but MFA is required */
  pendingMfaEmail: string | null;
  /** The user needs to set up MFA before they can use the app */
  mfaSetupRequired: boolean;
  errorMessage: string | null;
}

interface AuthContextValue extends AuthState {
  login: (email: string, password: string) => Promise<LoginResponse>;
  completeMfa: (email: string, otp: string, method: 'app' | 'email') => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
  dismissMfaSetup: () => void;
}

// ── Context ──────────────────────────────────────────────────────────────────

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    isLoading: false,
    isCheckingSession: true,
    isLoggedIn: false,
    user: null,
    accessToken: null,
    pendingMfaEmail: null,
    mfaSetupRequired: false,
    errorMessage: null,
  });

  // Restore session on mount
  useEffect(() => {
    (async () => {
      try {
        const token = await SecureStore.getItemAsync(SECURE_KEYS.ACCESS_TOKEN);
        if (token) {
          setState((s) => ({ ...s, isCheckingSession: false, isLoggedIn: true, accessToken: token }));
        } else {
          setState((s) => ({ ...s, isCheckingSession: false }));
        }
      } catch {
        setState((s) => ({ ...s, isCheckingSession: false }));
      }
    })();
  }, []);

  const login = useCallback(async (email: string, password: string): Promise<LoginResponse> => {
    setState((s) => ({ ...s, isLoading: true, errorMessage: null }));
    try {
      const response = await authApi.login({ email, password });

      if (!response.success) {
        const msg = (response as any).message ?? 'Login failed. Check your credentials.';
        setState((s) => ({ ...s, isLoading: false, errorMessage: msg }));
        return response;
      }

      // MFA required: OTP is enabled but skipValidation is false
      if (response.user?.isOtpEnabled && !response.skipValidation) {
        setState((s) => ({
          ...s, isLoading: false, pendingMfaEmail: email,
        }));
        return response;
      }

      // MFA setup required: OTP not enabled
      if (!response.user?.isOtpEnabled) {
        setState((s) => ({ ...s, isLoading: false, mfaSetupRequired: true }));
        return response;
      }

      // Direct login (skipValidation = true)
      if (response.token && response.refreshToken) {
        await saveTokens(response.token, response.refreshToken);
        await SecureStore.setItemAsync(SECURE_KEYS.USER_EMAIL, email);
        setState((s) => ({
          ...s, isLoading: false, isLoggedIn: true,
          accessToken: response.token!, user: response.user ?? null,
        }));
      }

      return response;
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Unable to sign in. Please try again.';
      setState((s) => ({ ...s, isLoading: false, errorMessage: msg }));
      throw err;
    }
  }, []);

  const completeMfa = useCallback(async (email: string, otp: string, method: 'app' | 'email') => {
    setState((s) => ({ ...s, isLoading: true, errorMessage: null }));
    try {
      const result = method === 'app'
        ? await authApi.validateOtp({ email, token: otp })
        : await authApi.confirmEmailOtp(otp);

      if (result.token && result.refreshToken) {
        await saveTokens(result.token, result.refreshToken);
        await SecureStore.setItemAsync(SECURE_KEYS.USER_EMAIL, email);
        setState((s) => ({
          ...s, isLoading: false, isLoggedIn: true, pendingMfaEmail: null,
          accessToken: result.token!, user: result.user ?? null,
        }));
      } else {
        setState((s) => ({ ...s, isLoading: false, errorMessage: 'MFA verification failed.' }));
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? 'Invalid code. Please try again.';
      setState((s) => ({ ...s, isLoading: false, errorMessage: msg }));
    }
  }, []);

  const logout = useCallback(async () => {
    await clearTokens();
    setState({
      isLoading: false, isCheckingSession: false, isLoggedIn: false,
      user: null, accessToken: null, pendingMfaEmail: null,
      mfaSetupRequired: false, errorMessage: null,
    });
  }, []);

  const clearError = useCallback(() => {
    setState((s) => ({ ...s, errorMessage: null }));
  }, []);

  const dismissMfaSetup = useCallback(() => {
    setState((s) => ({ ...s, mfaSetupRequired: false }));
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, completeMfa, logout, clearError, dismissMfaSetup }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}

