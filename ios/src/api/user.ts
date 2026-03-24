import { backendClient } from './client';
import type { UserDto } from '../types/api';

export const userApi = {
  getMe: (): Promise<UserDto> =>
    backendClient.get<UserDto>('/user/me').then((r) => r.data),
};

