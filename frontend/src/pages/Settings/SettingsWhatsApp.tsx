import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import api from '../../services/api';
import { useAuthStore } from '../../store/authStore';
import { SettingsPageHeader } from '../../components/SettingsPageHeader';
import { M3Card } from '../../components/m3/M3Card';
import { M3Button } from '../../components/m3/M3Button';

type Connection = { state: string; qrCode: string | null };
const endpoint = '/api/v1/stays/whatsapp';

// Remount on tenant/session changes so another hotel cannot inherit a QR or pending response.
export const SettingsWhatsApp = () => {
  const user = useAuthStore((s) => s.user);
  return <WhatsAppConnection key={`${user?.sub}:${user?.username}`} />;
};

const WhatsAppConnection = () => {
  const { t } = useTranslation('settings');
  const navigate = useNavigate();
  const [connection, setConnection] = useState<Connection | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(false);
  const active = useRef(true);
  const back = useCallback(() => navigate('/settings'), [navigate]);

  useEffect(() => {
    active.current = true;
    const abort = new AbortController();
    void api.get<Connection>(endpoint, { signal: abort.signal }).then(({ data }) => {
      if (active.current) setConnection(data);
    }).catch(() => { if (!abort.signal.aborted) setError(true); });
    return () => { active.current = false; abort.abort(); };
  }, []);

  useEffect(() => {
    if (connection?.state !== 'connecting') return;
    const abort = new AbortController();
    const timer = window.setInterval(() => {
      void api.get<Connection>(endpoint, { signal: abort.signal }).then(({ data }) => {
        if (!abort.signal.aborted && data.state === 'connected') setConnection(data);
      }).catch(() => { /* Keep QR visible while a transient status request fails. */ });
    }, 5000);
    // QR credentials are intentionally short-lived and never persisted in browser storage.
    const expiry = window.setTimeout(() => {
      setConnection({ state: 'expired', qrCode: null });
    }, 45000);
    return () => { abort.abort(); window.clearInterval(timer); window.clearTimeout(expiry); };
  }, [connection]);

  const connect = useCallback(async () => {
    setBusy(true);
    setError(false);
    setConnection(null);
    try {
      const { data } = await api.post<Connection>(`${endpoint}/connect`);
      if (active.current) setConnection(data);
    } catch {
      if (active.current) setError(true);
    } finally {
      if (active.current) setBusy(false);
    }
  }, []);

  return (
    <div className="space-y-6 max-w-2xl mx-auto pb-10">
      <SettingsPageHeader icon="qr_code_2" title={t('whatsapp_title')} subtitle={t('whatsapp_description')} onBack={back} />
      <M3Card variant="outlined" className="p-6 space-y-4">
        <p role="status">{t(`whatsapp_state_${connection?.state ?? 'loading'}`)}</p>
        {error && <p role="alert" className="text-error">{t('whatsapp_error')}</p>}
        {connection?.state === 'not_configured' ? <p>{t('whatsapp_setup')}</p> : (
          <>
            <p className="text-on-surface-variant">{t('whatsapp_scan')}</p>
            {connection?.qrCode && <img src={connection.qrCode} alt={t('whatsapp_qr_alt')} className="w-64 h-64 max-w-full mx-auto bg-white p-3" />}
            <M3Button onClick={connect} loading={busy} disabled={connection?.state === 'connected'} icon="qr_code_2">
              {t('whatsapp_connect')}
            </M3Button>
          </>
        )}
      </M3Card>
      <M3Card variant="outlined" className="p-6">
        <p className="text-on-surface-variant">{t('whatsapp_bot_pending')}</p>
      </M3Card>
    </div>
  );
};
