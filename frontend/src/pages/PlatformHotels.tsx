import { useCallback, useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import api from '../services/api';
import { M3Card } from '../components/m3/M3Card';
import { M3Button } from '../components/m3/M3Button';

type Hotel = { id: string; name: string; slug: string };
type Form = { name: string; slug: string; ownerUsername: string; ownerEmail: string; initialPassword: string };
const initial: Form = { name: '', slug: '', ownerUsername: '', ownerEmail: '', initialPassword: '' };

export const PlatformHotels = () => {
  const { t } = useTranslation('settings');
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [form, setForm] = useState<Form>(initial);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const abort = new AbortController();
    void api.get<Hotel[]>('/api/v1/auth/platform/hotels', { signal: abort.signal })
      .then(({ data }) => setHotels(data))
      .catch(() => { if (!abort.signal.aborted) setError(t('platform_load_error')); });
    return () => abort.abort();
  }, [t]);

  const update = useCallback((event: ChangeEvent<HTMLInputElement>) => {
    const field = event.target.name as keyof Form;
    const value = event.target.value;
    setForm((current) => ({ ...current, [field]: value }));
  }, []);

  const create = useCallback(async (event: FormEvent) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const { data } = await api.post<Hotel>('/api/v1/auth/platform/hotels', form);
      setHotels((current) => [...current, data]);
      setForm(initial);
    } catch {
      setError(t('platform_create_error'));
    } finally {
      setBusy(false);
    }
  }, [form, t]);

  const field = (key: keyof Form, type: string = 'text') => (
    <label key={key} className="block text-sm font-medium text-on-surface">
      {t(`platform_${key}`)}
      <input
        type={type}
        name={key}
        value={form[key]}
        onChange={update}
        required
        maxLength={key === 'name' ? 160 : key === 'slug' ? 80 : key === 'ownerEmail' ? 100 : undefined}
        autoComplete={key === 'initialPassword' ? 'new-password' : 'off'}
        className="mt-1 block w-full rounded-lg border border-outline bg-surface-container-lowest px-3 py-2 text-on-surface focus-visible:ring-2 focus-visible:ring-primary"
      />
    </label>
  );

  return (
    <div className="mx-auto max-w-3xl space-y-6 pb-10">
      <div>
        <h1 className="text-2xl font-semibold text-on-surface">{t('platform_hotels')}</h1>
        <p className="text-sm text-on-surface-variant">{t('platform_intro')}</p>
      </div>
      <M3Card variant="outlined" className="p-6">
        <h2 className="mb-4 text-lg font-semibold">{t('platform_new_hotel')}</h2>
        <form onSubmit={create} className="grid gap-4 sm:grid-cols-2">
          {field('name')}
          {field('slug')}
          {field('ownerUsername')}
          {field('ownerEmail', 'email')}
          {field('initialPassword', 'password')}
          <div className="sm:col-span-2">
            <p className="mb-3 text-xs text-on-surface-variant">{t('platform_password_note')}</p>
            <M3Button type="submit" loading={busy}>{t('platform_create')}</M3Button>
          </div>
        </form>
      </M3Card>
      {error && <p role="alert" className="text-error">{error}</p>}
      <M3Card variant="outlined" className="p-6">
        <h2 className="mb-3 text-lg font-semibold">{t('platform_existing')}</h2>
        <ul className="divide-y divide-outline-variant">
          {hotels.map((hotel) => (
            <li key={hotel.id} className="py-3">
              <span className="font-medium">{hotel.name}</span>
              <span className="ml-3 text-sm text-on-surface-variant">{hotel.slug}</span>
            </li>
          ))}
        </ul>
      </M3Card>
    </div>
  );
};
