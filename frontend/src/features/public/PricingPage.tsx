import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { billingApi } from '../../api/endpoints'
import type { Plan } from '../../api/types'
import { Loading, Select } from '../../components/ui'
import { billingCountry, COUNTRIES, CORE_FEATURES, detectCountry, PLAN_TAGLINE, planFeatures, planPrice } from '../../lib/billing'
import { cn } from '../../lib/cn'

export function PublicHeader() {
  return (
    <header className="fx-public-header">
      <div className="fx-public-nav">
      <Link to="/precios" className="fx-public-brand">
        <span className="fixi-brand-mark">F</span>
        <span>Fixi</span>
      </Link>
      <nav className="flex items-center gap-2">
        <Link to="/login" className="fx-public-link">
          Ingresar
        </Link>
        <Link to="/registro" className="fx-public-cta">
          Probar gratis
        </Link>
      </nav>
      </div>
    </header>
  )
}

export function PublicFooter() {
  return (
    <footer className="fx-public-footer">
      <span>Fixi · Operación para talleres de reparación</span>
      <nav>
        <Link to="/terminos">Términos</Link>
        <Link to="/privacidad">Privacidad</Link>
      </nav>
    </footer>
  )
}

export function PricingPage() {
  const [country, setCountry] = useState(detectCountry)
  const plans = useQuery({ queryKey: ['plans', billingCountry(country)], queryFn: () => billingApi.plans(billingCountry(country)) })

  return (
    <div className="fx-public-shell">
      <PublicHeader />
      <main className="fx-public-main">
        <section className="fx-pricing-hero">
          <span className="fx-kicker">Planes claros, taller sin límites</span>
          <h1>El sistema para tu servicio técnico</h1>
          <p>
            Órdenes, cotizaciones, stock, caja y avisos a tus clientes en un solo lugar. Usuarios ilimitados en todos los planes.
          </p>
          <span className="fx-pricing-trial">{plans.data?.trialDays ?? 14} días gratis, sin tarjeta.</span>
          <label className="fx-country-select">
            Precios para
            <Select value={country} onChange={(e) => setCountry(e.target.value)} className="w-48">
              {COUNTRIES.map((c) => (
                <option key={c.code} value={c.code}>
                  {c.name}
                </option>
              ))}
            </Select>
          </label>
        </section>

        {plans.isLoading ? (
          <Loading />
        ) : plans.data ? (
          <div className="fx-pricing-cards">
            {plans.data.plans.map((p) => (
              <PlanCard key={p.id} plan={p} highlight={p.id === 'Standard'} />
            ))}
          </div>
        ) : null}

        <section className="fx-pricing-features">
          <div>
            <span className="fx-kicker">La base completa</span>
            <h2>Todos los planes incluyen</h2>
          </div>
          <ul>
            {CORE_FEATURES.map((f) => (
              <li key={f} className="flex gap-2">
                <span aria-hidden="true" className="fx-feature-check">✓</span>
                {f}
              </li>
            ))}
            {country === 'AR' ? (
              <li className="flex gap-2">
                <span aria-hidden="true" className="fx-feature-check">✓</span>
                Factura electrónica ARCA
              </li>
            ) : null}
          </ul>
          <p>
            {country === 'AR'
              ? 'Precios finales en pesos, cobrados por Mercado Pago cada mes.'
              : country === 'MX'
                ? 'Precios en pesos mexicanos por mes. Mercado Pago realiza el cobro mensual.'
                : 'Precios en dólares por mes. Paddle cobra el pago mensual y suma los impuestos que correspondan en tu país.'}{' '}
            Cancelás cuando quieras.
          </p>
        </section>
      </main>
      <PublicFooter />
    </div>
  )
}

function PlanCard({ plan, highlight }: { plan: Plan; highlight: boolean }) {
  return (
    <article className={cn('fx-plan-card', highlight && 'is-highlighted')}>
      {highlight ? <span className="fx-plan-badge">Más elegido</span> : null}
      <p className="fx-plan-name">{plan.name}</p>
      <p className="fx-plan-tagline">{PLAN_TAGLINE[plan.id]}</p>
      <p className="fx-plan-price">
        <span>{planPrice(plan)}</span>
        <small> /mes</small>
      </p>
      <ul className="fx-plan-features">
        {planFeatures(plan).map((f) => (
          <li key={f} className="flex gap-2">
            <span aria-hidden="true" className="fx-feature-check">✓</span>
            {f}
          </li>
        ))}
      </ul>
      <Link
        to="/registro"
        className={cn('fx-plan-cta', highlight && 'is-primary')}
      >
        Empezar la prueba gratis
      </Link>
    </article>
  )
}
