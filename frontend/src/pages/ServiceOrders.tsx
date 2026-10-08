import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MaterialIcon } from '../components/MaterialIcon';
import { M3Table, M3TableCell, M3TableRow } from '../components/m3/M3Table';
import { serviceOrderService } from '../services/serviceOrderService';
import type { ServiceOrderResponse } from '../types/serviceOrder.types';

const SERVICE_ORDER_HEADERS = ['Summary', 'Status', 'Customer', 'Actions'];

export function ServiceOrders() {
  const [orders, setOrders] = useState<ServiceOrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setError(null);
      setOrders(await serviceOrderService.listAll());
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Unable to load service orders');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Async loading effect intentionally updates component state after the request.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void load();
  }, [load]);

  return (
    <main className="p-6 space-y-6">
      <header className="flex items-center gap-3">
        <MaterialIcon name="build" className="text-primary" />
        <div><h1 className="text-2xl font-semibold">Service orders</h1><p className="text-on-surface-variant">Repair workflow and authorization</p></div>
      </header>
      {error && <p className="text-error">{error}</p>}
      {loading ? <p>Loading…</p> : (
        <M3Table headers={SERVICE_ORDER_HEADERS}>
          {orders.map((order) => <M3TableRow key={order.id}>
            <M3TableCell>{order.summary}</M3TableCell>
            <M3TableCell>{order.status}</M3TableCell>
            <M3TableCell>{order.customerId}</M3TableCell>
            <M3TableCell><Link className="text-primary hover:underline" to={`/service-orders/${order.id}`}>Open</Link></M3TableCell>
          </M3TableRow>)}
        </M3Table>
      )}
    </main>
  );
}
