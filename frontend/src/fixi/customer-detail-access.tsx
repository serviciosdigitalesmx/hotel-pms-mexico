import { useEffect, useState } from 'react'
import * as api from './api'
import { CustomerDetailPage } from './operations'

export function CustomerDetailAccessPage() {
  const [canEdit, setCanEdit] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    api.effectiveCapabilities()
      .then(result => { if (active) setCanEdit(result.capabilities.includes('CUSTOMERS_WRITE')) })
      .catch(reason => { if (active) setError(reason instanceof Error ? reason.message : 'No se pudieron consultar los permisos') })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  return <>
    {!canEdit && <style>{'.customer-detail-readonly button { display: none !important; }'}</style>}
    <div className="customer-detail-readonly">
      <CustomerDetailPage />
      {loading && <p role="status" className="px-4 text-xs text-muted-foreground">Consultando permisos de edición…</p>}
      {error && <p role="alert" className="px-4 text-xs text-destructive">No se habilitó la edición: {error}</p>}
    </div>
  </>
}
