import { authClient } from './client';
import type {
  LoginRequest,
  LoginResponse,
  MfaVerificationResponse,
  VerifyOtpRequest,
} from '../types/api';

export const authApi = {
  login: (data: LoginRequest): Promise<LoginResponse> =>
    authClient.post<LoginResponse>('/auth/login', data).then((r) => r.data),

  refreshToken: (refreshToken: string) =>
    authClient.post('/auth/refresh', { refreshToken }).then((r) => r.data),

  /** Verify TOTP from authenticator app */
  validateOtp: (data: VerifyOtpRequest): Promise<MfaVerificationResponse> =>
    authClient.post<MfaVerificationResponse>('/auth/validate-otp', data).then((r) => r.data),

  /** Verify email-based OTP */
  confirmEmailOtp: (otp: string): Promise<MfaVerificationResponse> =>
    authClient.get<MfaVerificationResponse>(`/auth/confirm-email-2fa/${otp}`).then((r) => r.data),
};

