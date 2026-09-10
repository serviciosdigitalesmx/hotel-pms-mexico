import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useAuthStore } from './authStore';
import { useSettingsStore } from './settingsStore';
import { stayService } from '../services/stayService';
import i18n from '../i18n';
import type { HotelSettingsResponse } from '../types/stay.types';

vi.mock('../services/stayService', () => ({
  stayService: { getHotelSettings: vi.fn() },
}));
vi.mock('../i18n', () => ({ default: { changeLanguage: vi.fn().mockResolvedValue(undefined) } }));

const tenantSettings = (name: string) => ({
  hotelName: name, logoUrl: `/${name}.svg`, currency: 'MXN',
  locale: 'es-MX', timezone: 'America/Monterrey',
}) as HotelSettingsResponse;
const login = (sub: string) => useAuthStore.getState().login({ sub, username: sub, role: 'ADMIN' });

describe('tenant settings session isolation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.getState().logout();
  });

  it('clears tenant A identity on logout and uses tenant B settings on login', async () => {
    login('a');
    vi.mocked(stayService.getHotelSettings).mockResolvedValue(tenantSettings('Tenant A'));
    await useSettingsStore.getState().loadHotelSettings();
    expect(useSettingsStore.getState().hotelName).toBe('Tenant A');
    useAuthStore.getState().logout();
    expect(useSettingsStore.getState()).toMatchObject({ hotelName: 'PMS', logoUrl: '' });
    login('b');
    vi.mocked(stayService.getHotelSettings).mockResolvedValue(tenantSettings('Tenant B'));
    await useSettingsStore.getState().loadHotelSettings();
    expect(useSettingsStore.getState().hotelName).toBe('Tenant B');
  });

  it('preserves device accessibility preferences while clearing tenant identity', () => {
    login('a');
    const settings = useSettingsStore.getState();
    settings.setContrast('high');
    settings.setFontScale('large');
    settings.setLanguage('es');
    login('b');
    expect(useSettingsStore.getState()).toMatchObject({
      hotelName: 'PMS', logoUrl: '', contrast: 'high', fontScale: 'large',
    });
    expect(document.documentElement.getAttribute('data-contrast')).toBe('high');
    expect(document.documentElement.style.getPropertyValue('--md-font-scale')).toBe('18px');
    expect(localStorage.getItem('hotel-pms-contrast')).toBe('high');
    expect(i18n.changeLanguage).toHaveBeenCalledWith('es');
    settings.setContrast('normal');
    settings.setFontScale('normal');
  });

  it('ignores a late response from A after B has loaded its own settings', async () => {
    login('a');
    let resolveA!: (settings: HotelSettingsResponse) => void;
    vi.mocked(stayService.getHotelSettings).mockReturnValueOnce(new Promise((resolve) => { resolveA = resolve; }));
    const pendingA = useSettingsStore.getState().loadHotelSettings();
    login('b');
    vi.mocked(stayService.getHotelSettings).mockResolvedValueOnce(tenantSettings('Tenant B'));
    await useSettingsStore.getState().loadHotelSettings();
    resolveA(tenantSettings('Tenant A'));
    await pendingA;
    expect(useSettingsStore.getState()).toMatchObject({ hotelName: 'Tenant B', logoUrl: '/Tenant B.svg' });
  });
});
