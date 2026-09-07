import { useSettingsStore } from '../store/settingsStore';
import { DEFAULT_BRAND } from '../config/branding';

export interface UseBrandResult {
  hotelName: string;
  logoUrl: string;
}

export const useBrand = (): UseBrandResult => {
  const hotelName = useSettingsStore((state) => state.hotelName);
  const logoUrl = useSettingsStore((state) => state.logoUrl);
  return {
    hotelName: hotelName || DEFAULT_BRAND.name,
    logoUrl: logoUrl || '',
  };
};
