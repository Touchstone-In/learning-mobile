// ── Auth Types ──────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ProfileDto {
  id?: string;
  role?: string;
  project?: string;
}

export interface AuthUserDto {
  id?: string;
  email?: string;
  profiles?: ProfileDto[];
  isOtpEnabled?: boolean;
  otpMeans?: string;
}

export interface LoginResponse {
  success?: boolean;
  token?: string;
  refreshToken?: string;
  expiresIn?: number;
  id?: string;
  email?: string;
  role?: string;
  isGeneratedPassword?: boolean;
  user?: AuthUserDto;
  requiresMfa?: boolean;
  mfaMethod?: string;
  skipValidation?: boolean;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
}

export interface VerifyOtpRequest {
  email: string;
  token: string;
}

export interface MfaVerificationResponse {
  success?: boolean;
  token?: string;
  refreshToken?: string;
  expiresIn?: number;
  user?: AuthUserDto;
}

// ── User Types ───────────────────────────────────────────────────────────────

export interface UserDto {
  id?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  role?: string;
  avatar?: string;
  isStaff?: boolean;
}

// ── Mobile / Learner Types ───────────────────────────────────────────────────

export interface NextSessionSummary {
  sessionName?: string;
  day?: string;
  period?: string;
  track?: string;
  group?: string;
}

export interface KeyDateSummary {
  label: string;
  date: string;
}

export interface LearnerOverviewResponse {
  programName?: string;
  programType?: string;
  applicationStatus?: string;
  registrationStatus?: string;
  nextSession?: NextSessionSummary;
  keyDates?: KeyDateSummary[];
}

export interface ScheduleEntry {
  sessionName: string;
  track: string;
  group: string;
}

export interface ScheduleDay {
  day: string;
  period: string;
  session: ScheduleEntry;
}

export interface ScheduleWeek {
  weekName: string;
  days: ScheduleDay[];
}

export interface ScheduleResponse {
  lastUpdated?: string;
  weeks: ScheduleWeek[];
}

export interface LearnerResultSummary {
  id: string;
  attendanceStatus?: string;
  asyncStatus?: string;
  associatedMedicalSchool?: string;
  postgraduateTrainingProgram?: string;
  name?: string;
  comment?: string;
  publishedAt?: string;
}

export interface LearnerResultsResponse {
  results: LearnerResultSummary[];
}

export interface NotificationPreferencesResponse {
  pushEnabled: boolean;
  scheduleReminders: boolean;
  orientationReminders: boolean;
}

export interface UpdatePreferencesRequest {
  pushEnabled?: boolean;
  scheduleReminders?: boolean;
  orientationReminders?: boolean;
}

