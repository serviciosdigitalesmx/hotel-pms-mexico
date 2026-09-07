import { useCallback, memo } from 'react';
import type { ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { MaterialIcon } from '../../components/MaterialIcon';
import { M3Button } from '../../components/m3/M3Button';
import { M3Card } from '../../components/m3/M3Card';
import { M3TextField } from '../../components/m3/M3TextField';
import type { IdentifiableGuest } from './stayGuestFieldHelpers';

const ICON_SIZE_20 = { fontSize: 20 };

export interface GuestFieldSectionProps {
  guest: IdentifiableGuest;
  index: number;
  canRemove: boolean;
  onRemove: (idx: number) => void;
  onChange: (idx: number, patch: Partial<IdentifiableGuest>) => void;
}

export const GuestFieldSection = memo(({
  guest, index, canRemove, onRemove, onChange,
}: GuestFieldSectionProps) => {
  const { t } = useTranslation('stays');

  const handleSimpleChange = useCallback((e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    onChange(index, { [e.target.name]: e.target.value } as Partial<IdentifiableGuest>);
  }, [index, onChange]);

  const handleRemove = useCallback(() => onRemove(index), [index, onRemove]);

  return (
    <M3Card className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-display font-medium text-on-surface flex items-center gap-2">
          <MaterialIcon name="person" className="text-primary" />
          {index === 0 ? t('guest_label') : t('guest_number', { number: index + 1 })}
        </h2>
        {canRemove && (
          <M3Button variant="text" icon="close" onClick={handleRemove} type="button">
            {t('btn_remove')}
          </M3Button>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <M3TextField label={t('label_first_name')} name="firstName" value={guest.firstName} onChange={handleSimpleChange} required />
        <M3TextField label={t('label_last_name')} name="lastName" value={guest.lastName} onChange={handleSimpleChange} required />

        <div className="relative">
          <div className="relative flex items-center rounded-shape-xs border transition-colors border-outline hover:border-on-surface">
            <select
              id={`gender-${index}`}
              name="gender"
              value={guest.gender}
              onChange={handleSimpleChange}
              required
              className="peer w-full bg-transparent px-4 pt-5 pb-1.5 text-sm font-body text-on-surface focus:outline-none appearance-none"
            >
              <option value="" disabled hidden />
              <option value="1">{t('gender_male')}</option>
              <option value="2">{t('gender_female')}</option>
            </select>
            <label htmlFor={`gender-${index}`} className="absolute pointer-events-none font-body left-4 top-1 text-xs text-on-surface-variant">
              {t('label_gender')} *
            </label>
            <span className="material-symbols-outlined absolute right-3 pointer-events-none text-on-surface-variant z-10" style={ICON_SIZE_20}>arrow_drop_down</span>
          </div>
        </div>

        <M3TextField label={t('label_date_of_birth')} name="dateOfBirth" type="date" value={guest.dateOfBirth} onChange={handleSimpleChange} required />
        <M3TextField label={t('label_place_of_birth')} name="placeOfBirth" value={guest.placeOfBirth} onChange={handleSimpleChange} required />
        <M3TextField label={t('label_citizenship')} name="citizenship" value={guest.citizenship} onChange={handleSimpleChange} required />
        <M3TextField label={t('label_stay_reason')} name="travelPurpose" value={guest.travelPurpose ?? ''} onChange={handleSimpleChange} />
      </div>
    </M3Card>
  );
});
GuestFieldSection.displayName = 'GuestFieldSection';
