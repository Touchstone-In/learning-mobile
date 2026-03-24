import { backendClient } from './client';
import type {
  LearnerOverviewResponse,
  LearnerResultsResponse,
  NotificationPreferencesResponse,
  ScheduleResponse,
  UpdatePreferencesRequest,
} from '../types/api';

export const mobileApi = {
  getOverview: (): Promise<LearnerOverviewResponse> =>
    backendClient.get<LearnerOverviewResponse>('/mobile/me/overview').then((r) => r.data),

  getSchedule: (): Promise<ScheduleResponse> =>
    backendClient.get<ScheduleResponse>('/mobile/me/schedule').then((r) => r.data),

  getResults: (): Promise<LearnerResultsResponse> =>
    backendClient.get<LearnerResultsResponse>('/mobile/me/results').then((r) => r.data),

  getPreferences: (): Promise<NotificationPreferencesResponse> =>
    backendClient.get<NotificationPreferencesResponse>('/mobile/me/preferences').then((r) => r.data),

  updatePreferences: (data: UpdatePreferencesRequest): Promise<NotificationPreferencesResponse> =>
    backendClient.patch<NotificationPreferencesResponse>('/mobile/me/preferences', data).then((r) => r.data),
};

