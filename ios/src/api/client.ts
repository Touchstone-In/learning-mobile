import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import * as SecureStore from 'expo-secure-store';

export const AUTH_BASE_URL = 'https://zxkbbj3pcy.us-east-1.awsapprunner.com/api';
export const BACKEND_BASE_URL = 'https://learn-test-be.tsin.ca/api';

export const SECURE_KEYS = {
  ACCESS_TOKEN: 'tsin_access_token',
  REFRESH_TOKEN: 'tsin_refresh_token',
  USER_EMAIL: 'tsin_user_email',
};

// ── Token storage helpers ────────────────────────────────────────────────────

export async function getAccessToken(): Promise<string | null> {
  return SecureStore.getItemAsync(SECURE_KEYS.ACCESS_TOKEN);
}

export async function saveTokens(accessToken: string, refreshToken: string): Promise<void> {
  await Promise.all([
    SecureStore.setItemAsync(SECURE_KEYS.ACCESS_TOKEN, accessToken),
    SecureStore.setItemAsync(SECURE_KEYS.REFRESH_TOKEN, refreshToken),
  ]);
}

export async function clearTokens(): Promise<void> {
  await Promise.all([
    SecureStore.deleteItemAsync(SECURE_KEYS.ACCESS_TOKEN),
    SecureStore.deleteItemAsync(SECURE_KEYS.REFRESH_TOKEN),
    SecureStore.deleteItemAsync(SECURE_KEYS.USER_EMAIL),
  ]);
}

// ── Auth client (no token — used for login / refresh) ────────────────────────

export const authClient: AxiosInstance = axios.create({
  baseURL: AUTH_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// ── Backend client (auto-attaches Bearer token) ──────────────────────────────

export const backendClient: AxiosInstance = axios.create({
  baseURL: BACKEND_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

backendClient.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const token = await getAccessToken();
  if (token) {
    config.headers = config.headers ?? {};
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Token refresh interceptor
backendClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = await SecureStore.getItemAsync(SECURE_KEYS.REFRESH_TOKEN);
        if (!refreshToken) throw new Error('No refresh token');
        const res = await authClient.post('/auth/refresh', { refreshToken });
        const newToken: string = res.data.accessToken;
        await SecureStore.setItemAsync(SECURE_KEYS.ACCESS_TOKEN, newToken);
        originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
        return backendClient(originalRequest);
      } catch {
        await clearTokens();
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

