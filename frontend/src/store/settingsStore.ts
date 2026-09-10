import { create } from 'zustand';
import i18n from '../i18n';
import { stayService } from '../services/stayService';
import { DEFAULT_BRAND } from '../config/branding';
import { useAuthStore } from './authStore';

export type FontScale = 'small' | 'normal' | 'large';
export type ContrastMode = 'normal' | 'high';

const FONT_SCALE_MAP: Record<FontScale, string> = {
  small: '14px',
  normal: '16px',
  large: '18px',
};

const STORAGE_KEY_CONTRAST = 'hotel-pms-contrast';
const STORAGE_KEY_FONT = 'hotel-pms-font-scale';
const DEFAULT_CURRENCY = 'MXN';
const DEFAULT_LOCALE = 'es-MX';
const DEFAULT_TIMEZONE = 'America/Monterrey';

let sessionVersion = 0;
const tenantDefaults = {
  hotelName: DEFAULT_BRAND.name,
  logoUrl: DEFAULT_BRAND.logoUrl,
  currency: DEFAULT_CURRENCY,
  locale: DEFAULT_LOCALE,
  timezone: DEFAULT_TIMEZONE,
};

const applyContrast = (mode: ContrastMode) => {
  const root = document.documentElement;
  if (mode === 'high') {
    root.setAttribute('data-contrast', 'high');
  } else {
    root.removeAttribute('data-contrast');
  }
  localStorage.setItem(STORAGE_KEY_CONTRAST, mode);
};

const applyFontScale = (scale: FontScale) => {
  document.documentElement.style.setProperty(
    '--md-font-scale',
    FONT_SCALE_MAP[scale]
  );
  localStorage.setItem(STORAGE_KEY_FONT, scale);
};

const getInitialContrast = (): ContrastMode => {
  if (typeof window === 'undefined') return 'normal';
  return (localStorage.getItem(STORAGE_KEY_CONTRAST) as ContrastMode) ?? 'normal';
};

const getInitialFontScale = (): FontScale => {
  if (typeof window === 'undefined') return 'normal';
  return (localStorage.getItem(STORAGE_KEY_FONT) as FontScale) ?? 'normal';
};

interface SettingsState {
  hotelName: string;
  logoUrl: string;
  contrast: ContrastMode;
  fontScale: FontScale;
  currency: string;
  locale: string;
  timezone: string;
  setContrast: (mode: ContrastMode) => void;
  setFontScale: (scale: FontScale) => void;
  setLanguage: (lang: string) => void;
  loadHotelSettings: () => Promise<void>;
}

export const useSettingsStore = create<SettingsState>(() => {
  const initialContrast = getInitialContrast();
  const initialFontScale = getInitialFontScale();

  // Apply stored preferences immediately on store creation
  applyContrast(initialContrast);
  applyFontScale(initialFontScale);

  return {
    hotelName: DEFAULT_BRAND.name,
    logoUrl: DEFAULT_BRAND.logoUrl,
    contrast: initialContrast,
    fontScale: initialFontScale,
    currency: DEFAULT_CURRENCY,
    locale: DEFAULT_LOCALE,
    timezone: DEFAULT_TIMEZONE,
    setContrast: (mode) => {
      applyContrast(mode);
      useSettingsStore.setState({ contrast: mode });
    },
    setFontScale: (scale) => {
      applyFontScale(scale);
      useSettingsStore.setState({ fontScale: scale });
    },
    setLanguage: (lang) => {
      i18n.changeLanguage(lang);
    },
    loadHotelSettings: async () => {
      const requestedSession = sessionVersion;
      const settings = await stayService.getHotelSettings();
      if (requestedSession !== sessionVersion) return;
      const locale = settings.locale || DEFAULT_LOCALE;
      useSettingsStore.setState({
        hotelName: settings.hotelName || DEFAULT_BRAND.name,
        logoUrl: settings.logoUrl || DEFAULT_BRAND.logoUrl,
        currency: settings.currency || DEFAULT_CURRENCY,
        locale,
        timezone: settings.timezone || DEFAULT_TIMEZONE,
      });
      await i18n.changeLanguage(locale.split('-')[0]);
    },
  };
});

// Clear tenant data synchronously at every session transition and discard any
// settings response that was started by the previous session.
useAuthStore.subscribe((state, previous) => {
  if (state.user !== previous.user) {
    sessionVersion += 1;
    useSettingsStore.setState(tenantDefaults);
  }
});
