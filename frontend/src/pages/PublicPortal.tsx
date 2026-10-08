/* eslint-disable react-perf/jsx-no-new-function-as-prop */
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';

type Operation = {
  resourceType: string;
  resourceId: string;
  customerId: string;
  expiresAt: string;
};
type DocumentEvidence = { id: string; fileName: string; contentType: string; storageKey: string };

/** Token-scoped customer portal for OTP verification and authorization. */
export function PublicPortal() {
  const { token = '' } = useParams<{ token: string }>();
  const [operation, setOperation] = useState<Operation | null>(null);
  const [code, setCode] = useState('');
  const [evidence, setEvidence] = useState('Customer authorized from public portal');
  const [message, setMessage] = useState('Loading…');
  const [verified, setVerified] = useState(false);
  const [documents, setDocuments] = useState<DocumentEvidence[]>([]);

  useEffect(() => {
    api.get<Operation>(`/api/public/portal/operations/${encodeURIComponent(token)}`)
      .then(({ data }) => {
        setOperation(data);
        setMessage('Request an OTP to continue.');
        return api.get<DocumentEvidence[]>(`/api/public/portal/operations/${encodeURIComponent(token)}/documents`);
      })
      .then(({ data }) => setDocuments(data))
      .catch(() => setMessage('This portal link is invalid or expired.'));
  }, [token]);

  const requestOtp = async () => {
    try {
      const { data } = await api.post<{ testCode?: string }>(
        `/api/public/portal/operations/${encodeURIComponent(token)}/otp/challenge`, {});
      setMessage(data.testCode ? `Local verification code: ${data.testCode}` : 'OTP sent.');
    } catch {
      setMessage('Unable to send OTP.');
    }
  };

  const verifyOtp = async (event: FormEvent) => {
    event.preventDefault();
    const { data } = await api.post<{ verified: boolean }>(
      `/api/public/portal/operations/${encodeURIComponent(token)}/otp/verify`, null,
      { params: { code } });
    setVerified(data.verified);
    setMessage(data.verified ? 'Identity verified. You can authorize this request.' : 'Invalid OTP.');
  };

  const authorize = async () => {
    await api.post(`/api/public/portal/operations/${encodeURIComponent(token)}/authorize`, null,
      { params: { evidence } });
    setMessage('Authorization recorded successfully.');
  };

  return (
    <main className="min-h-screen bg-surface p-6 text-on-surface">
      <section className="mx-auto max-w-xl rounded-2xl bg-surface-container p-6 shadow-sm">
        <h1 className="text-2xl font-semibold">Customer portal</h1>
        <p className="mt-2 text-on-surface-variant">{message}</p>
        {operation && <p className="mt-4 text-sm">Operation: {operation.resourceType} · {operation.resourceId}</p>}
        {documents.length > 0 && <ul aria-label="Documents" className="mt-4 space-y-2 text-sm">
          {documents.map((document) => <li key={document.id} className="rounded-lg border p-3">
            <span>{document.fileName}</span>
            <span className="ml-2 text-on-surface-variant">{document.contentType}</span>
            <span className="ml-2 break-all text-on-surface-variant">{document.storageKey}</span>
          </li>)}
        </ul>}
        {operation && <div className="mt-6 flex gap-3">
          <button type="button" onClick={requestOtp} className="rounded-lg bg-primary px-4 py-2 text-on-primary">
            Request OTP
          </button>
          <form onSubmit={verifyOtp} className="flex gap-2">
            <input aria-label="OTP code" value={code} onChange={(event) => setCode(event.target.value)}
              className="w-32 rounded-lg border p-2" inputMode="numeric" />
            <button type="submit" className="rounded-lg border px-4 py-2">Verify</button>
          </form>
        </div>}
        {verified && <div className="mt-6 space-y-3">
          <textarea aria-label="Authorization evidence" value={evidence}
            onChange={(event) => setEvidence(event.target.value)} className="min-h-24 w-full rounded-lg border p-2" />
          <button type="button" onClick={authorize} className="rounded-lg bg-primary px-4 py-2 text-on-primary">
            Authorize request
          </button>
        </div>}
      </section>
    </main>
  );
}
