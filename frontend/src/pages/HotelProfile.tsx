import { useState, useEffect, useCallback, useMemo, useRef, memo } from 'react';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';
import { stayService } from '../services/stayService';
import type { HotelSettingsResponse, HotelSettingsRequest } from '../types/stay.types';
import { MaterialIcon } from '../components/MaterialIcon';
import { M3Button } from '../components/m3/M3Button';
import { M3Card } from '../components/m3/M3Card';
import { useToastStore } from '../store/toastStore';
import { useSettingsStore } from '../store/settingsStore';
import { getErrorMessage } from '../utils/errorMessage';

const RFC_REGEX = /^[A-ZÑ&]{3,4}\d{6}[A-Z0-9]{3}$/i;

// -----------------------------------------------------------------------
// ProfileField — reusable labelled input
// -----------------------------------------------------------------------

interface ProfileFieldProps {
  id: string;
  label: string;
  value: string;
  placeholder?: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  required?: boolean;
  error?: string;
  type?: 'text' | 'url' | 'password';
  autoComplete?: string;
}

const ProfileField = memo(({
  id, label, value, placeholder, onChange, required, error, type = 'text', autoComplete,
}: ProfileFieldProps) => {
  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-on-surface mb-1">
        {label}{required && <span aria-hidden="true"> *</span>}
      </label>
      <div className="relative">
        <input
          id={id}
          type={type}
          value={value}
          placeholder={placeholder}
          onChange={onChange}
          autoComplete={autoComplete}
          className={`w-full rounded-md border border-outline bg-surface px-3 py-2 text-sm text-on-surface
            focus:outline-none focus:ring-2 focus:ring-primary`}
          aria-invalid={!!error}
          aria-describedby={error ? `${id}-error` : undefined}
        />
      </div>
      {error && (
        <p id={`${id}-error`} role="alert" className="mt-1 text-sm text-error">{error}</p>
      )}
    </div>
  );
});
ProfileField.displayName = 'ProfileField';

// -----------------------------------------------------------------------
// HotelProfile page
// -----------------------------------------------------------------------

export function HotelProfile() {
  const { t } = useTranslation('admin');
  const { addToast } = useToastStore();
  const loadHotelSettings = useSettingsStore((state) => state.loadHotelSettings);

  const [form, setForm] = useState<HotelSettingsRequest>({
    hotelName: '',
    address: '',
    vatNumber: '',
    fiscalCode: '',
    logoUrl: '',
    city: '',
    state: '',
    country: 'México',
    postalCode: '',
    currency: 'MXN',
    locale: 'es-MX',
    timezone: 'America/Monterrey',
    publicSlug: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const imgRef = useRef<HTMLImageElement>(null);

  const profileSchema = useMemo(() => z.object({
    vatNumber: z.union([z.string().regex(RFC_REGEX, t('common:err_invalid_vat')), z.literal('')]),
    logoUrl: z.union([z.string().url(t('common:err_invalid_url')), z.literal('')]),
  }), [t]);
  const handleLogoError = useCallback(() => {
    if (imgRef.current) imgRef.current.style.display = 'none';
  }, []);

  useEffect(() => {
    stayService
      .getHotelSettings()
      .then((s: HotelSettingsResponse) => {
        setForm({
          hotelName: s.hotelName ?? '',
          address: s.address ?? '',
          vatNumber: s.vatNumber ?? '',
          fiscalCode: s.fiscalCode ?? '',
          logoUrl: s.logoUrl ?? '',
          city: s.city ?? '',
          state: s.state ?? '',
          country: s.country ?? 'México',
          postalCode: s.postalCode ?? '',
          currency: s.currency ?? 'MXN',
          locale: s.locale ?? 'es-MX',
          timezone: s.timezone ?? 'America/Monterrey',
          publicSlug: s.publicSlug ?? '',
        });
      })
      .catch(() => addToast(t('err_profile_save'), 'error'))
      .finally(() => setLoading(false));
  }, [addToast, t]);

  const handleChange = useCallback(
    (field: keyof HotelSettingsRequest) =>
      (e: React.ChangeEvent<HTMLInputElement>) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value })),
    [],
  );

  const handleSave = useCallback(async () => {
    setFieldErrors({});

    const result = profileSchema.safeParse({
      vatNumber: (form.vatNumber ?? '').trim(),
      logoUrl: (form.logoUrl ?? '').trim(),
    });
    if (!result.success) {
      const errors: Record<string, string> = {};
      for (const issue of result.error.issues) {
        const field = issue.path[0];
        if (typeof field === 'string' && !errors[field]) errors[field] = issue.message;
      }
      setFieldErrors(errors);
      return;
    }

    setSaving(true);
    try {
      await stayService.updateHotelSettings({ ...form, ...result.data });
      void loadHotelSettings();
      addToast(t('toast_profile_saved'), 'success');
    } catch (err: unknown) {
      const detail = (err as { response?: { data?: { detail?: unknown } } })?.response?.data?.detail;
      addToast(
        typeof detail === 'string' && detail.trim() !== ''
          ? detail
          : getErrorMessage(err, t('err_profile_save')),
        'error',
      );
    } finally {
      setSaving(false);
    }
  }, [form, profileSchema, addToast, t, loadHotelSettings]);

  const handleSaveClick = useCallback(() => {
    void handleSave();
  }, [handleSave]);

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <MaterialIcon name="progress_activity" size={32} className="text-primary animate-spin" />
      </div>
    );
  }

  return (
    <main className="max-w-xl mx-auto p-6 space-y-6" aria-labelledby="hotel-profile-title">
      <div>
        <h1 id="hotel-profile-title" className="text-2xl font-semibold text-on-surface flex items-center gap-2">
          <MaterialIcon name="apartment" className="text-primary" />
          {t('hotel_profile_title')}
        </h1>
        <p className="text-sm text-on-surface-variant mt-1">{t('hotel_profile_subtitle')}</p>
      </div>

      <M3Card className="p-6 space-y-4">
        {/* Logo preview */}
        {form.logoUrl && (
          <div className="flex justify-center mb-2">
            <img
              ref={imgRef}
              src={form.logoUrl}
              alt="hotel logo preview"
              className="max-h-20 object-contain rounded-md border border-outline-variant"
              onError={handleLogoError}
            />
          </div>
        )}

        <ProfileField
          id="profile-hotel-name"
          label={t('label_hotel_name')}
          value={form.hotelName ?? ''}
          placeholder={t('placeholder_hotel_name')}
          onChange={handleChange('hotelName')}
        />

        <ProfileField
          id="profile-address"
          label={t('label_hotel_address')}
          value={form.address ?? ''}
          placeholder={t('placeholder_address')}
          onChange={handleChange('address')}
        />

        <div className="grid grid-cols-2 gap-4">
          <ProfileField
            id="profile-city"
            label={t('label_city')}
            value={form.city ?? ''}
            onChange={handleChange('city')}
          />
          <ProfileField
            id="profile-state"
            label={t('label_state')}
            value={form.state ?? ''}
            onChange={handleChange('state')}
          />
          <ProfileField
            id="profile-postal-code"
            label={t('label_postal_code')}
            value={form.postalCode ?? ''}
            onChange={handleChange('postalCode')}
          />
          <ProfileField
            id="profile-country"
            label={t('label_country')}
            value={form.country ?? ''}
            onChange={handleChange('country')}
          />
        </div>

        <ProfileField
          id="profile-rfc"
          label={t('label_vat_number')}
          value={form.vatNumber ?? ''}
          placeholder={t('placeholder_vat_number')}
          onChange={handleChange('vatNumber')}
          error={fieldErrors.vatNumber}
        />

        <ProfileField
          id="profile-fiscal-code"
          label={t('label_fiscal_code')}
          value={form.fiscalCode ?? ''}
          onChange={handleChange('fiscalCode')}
        />

        <div className="grid grid-cols-2 gap-4">
          <ProfileField
            id="profile-currency"
            label={t('label_currency')}
            value={form.currency ?? ''}
            onChange={handleChange('currency')}
          />
          <ProfileField
            id="profile-locale"
            label={t('label_locale')}
            value={form.locale ?? ''}
            onChange={handleChange('locale')}
          />
          <ProfileField
            id="profile-timezone"
            label={t('label_timezone')}
            value={form.timezone ?? ''}
            onChange={handleChange('timezone')}
          />
          <ProfileField
            id="profile-public-slug"
            label={t('label_public_slug')}
            value={form.publicSlug ?? ''}
            onChange={handleChange('publicSlug')}
          />
        </div>

        <ProfileField
          id="profile-logo"
          label={t('label_logo_url')}
          value={form.logoUrl ?? ''}
          placeholder={t('placeholder_logo_url')}
          onChange={handleChange('logoUrl')}
          error={fieldErrors.logoUrl}
          type="url"
        />

      </M3Card>

      <div className="flex justify-end">
        <M3Button icon="save" onClick={handleSaveClick} disabled={saving}>
          {saving ? t('btn_saving') : t('btn_save_profile')}
        </M3Button>
      </div>
    </main>
  );
}
