import { ServiceCatalogManagementPage } from './service-catalog'
import { InventoryCatalogPage } from './inventory'

/** Legacy route adapter retained for links from older Fixi screens. */
export function ServiceCatalogPage() {
  return <ServiceCatalogManagementPage />
}

/** Legacy import-panel adapter; inventory owns the authoritative CSV workflow. */
export function ProductImportPanel({ onImported }: { onImported: () => void }) {
  void onImported
  return <InventoryCatalogPage />
}
