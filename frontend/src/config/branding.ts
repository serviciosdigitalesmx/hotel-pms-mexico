export interface BrandConfig {
  name: string;
  logoUrl: string;
}

/**
 * Default product brand. Every component reads branding from this config or
 * from the hotel's persisted settings; no layout hardcodes a hotel name/logo.
 */
export const DEFAULT_BRAND: BrandConfig = {
  name: 'Hotel Palmas',
  logoUrl: '',
};
