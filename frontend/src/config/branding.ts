export interface BrandConfig {
  name: string;
  logoUrl: string;
}

/**
 * Neutral fallback only. Tenant identity comes from authenticated hotel settings.
 */
export const DEFAULT_BRAND: BrandConfig = {
  name: 'PMS',
  logoUrl: '',
};
