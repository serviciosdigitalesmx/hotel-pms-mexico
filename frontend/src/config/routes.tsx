import { type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { Landing,LoginPage,SignupPage,ForgotPasswordPage,ResetPasswordPage,VerifyEmailPage,AcceptInvitationPage,CustomerPortalPage,ChangePasswordPage } from '@/fixi/pages'
import { Protected,RealDashboard,BranchDashboard,OrderReportingPage,InventoryReportingPage,ProductivityReportingPage,CustomersPage,OrdersPage,BranchesPage,TeamPage,SuppliersPage,OnboardingPage,SettingsPage,IntegrationPage } from '@/fixi/operations'
import { ServiceOrderDetail } from '@/fixi/order-detail'
import { ServiceOrderPaymentSettlementPage } from '@/fixi/payment-settlement'
import { Purchases } from '@/fixi/purchases'
import { BusinessSettings } from '@/fixi/settings'
import { ExpensesPage } from '@/fixi/expenses'
import { ServiceCatalogManagementPage } from '@/fixi/service-catalog'
import { ReceptionOrderForm } from '@/fixi/reception'
import { InventoryCatalogPage } from '@/fixi/inventory'
import { CustomerCsvTools } from '@/fixi/customer-tools'
import { WorkflowSettingsPage } from '@/fixi/workflow-settings'
import { OrderPreferencesPage } from '@/fixi/order-preferences'
import { DeviceManagementPage } from '@/fixi/devices'
import { NotificationSettingsPage } from '@/fixi/notification-settings'
import { FixiFinancialReportingPage } from '@/fixi/financial-reporting'
import { TeamUserEditor } from '@/fixi/team-user-editor'
import { OperationalAlertsPage } from '@/fixi/operational-alerts'
import { CustomerDetailAccessPage } from '@/fixi/customer-detail-access'
import { AccountProfilePage } from '@/fixi/account-profile'
import { PlatformAuditPage, PlatformDashboardPage, PlatformPlansPage, PlatformShell, PlatformSubscriptionsPage, PlatformTenantDetailPage, PlatformTenantsPage, PlatformUsersPage } from '@/platform/PlatformConsole'
export interface RouteConfig { path:string; element:ReactNode; children?:RouteConfig[] }
const secure=(element:ReactNode,roles?:string[],capabilities?:string[])=><Protected roles={roles} capabilities={capabilities}>{element}</Protected>
export const routes:RouteConfig[]=[
  {path:'/',element:<Landing/>},{path:'/landing',element:<Landing/>},{path:'/login',element:<LoginPage/>},{path:'/change-password',element:<ChangePasswordPage/>},{path:'/signup',element:<SignupPage/>},{path:'/forgot-password',element:<ForgotPasswordPage/>},{path:'/reset-password',element:<ResetPasswordPage/>},{path:'/verify-email',element:<VerifyEmailPage/>},{path:'/accept-invitation',element:<AcceptInvitationPage/>},{path:'/portal/:token',element:<CustomerPortalPage/>},
  {path:'/dashboard',element:secure(<RealDashboard/>,undefined,['REPORTS_READ'])},{path:'/branch-dashboard',element:secure(<BranchDashboard/>,undefined,['BRANCHES_READ'])},{path:'/reports/orders',element:secure(<OrderReportingPage/>,undefined,['REPORTS_READ'])},{path:'/reports/inventory',element:secure(<InventoryReportingPage/>,undefined,['REPORTS_READ'])},{path:'/reports/productivity',element:secure(<ProductivityReportingPage/>,undefined,['REPORTS_READ'])},{path:'/onboarding',element:secure(<OnboardingPage/>,['OWNER'],['SETTINGS_MANAGE'])},
  {path:'/customers',element:secure(<CustomersPage/>,undefined,['CUSTOMERS_READ'])},{path:'/customers/:id',element:secure(<CustomerDetailAccessPage/>,undefined,['CUSTOMERS_READ'])},{path:'/devices',element:secure(<DeviceManagementPage/>,undefined,['DEVICES_READ'])},{path:'/equipment',element:<Navigate to="/devices" replace/>},{path:'/reception',element:<Navigate to="/reception/new" replace/>},
  {path:'/reception/new',element:secure(<ReceptionOrderForm/>,undefined,['ORDERS_CREATE'])},{path:'/services',element:secure(<ServiceCatalogManagementPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders',element:secure(<OrdersPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders/:id/payment',element:secure(<ServiceOrderPaymentSettlementPage/>,undefined,['PAYMENTS_RECEIVE'])},{path:'/service-orders/:id',element:secure(<ServiceOrderDetail/>,undefined,['ORDERS_READ'])},
  {path:'/diagnostics',element:secure(<OrdersPage title="Diagnóstico" statuses={['REVIEW','DIAGNOSIS']}/>,undefined,['ORDERS_READ'])},{path:'/repairs',element:secure(<OrdersPage title="Reparaciones" statuses={['AUTHORIZED','IN_PROGRESS']}/>,undefined,['ORDERS_UPDATE'])},{path:'/tests',element:secure(<OrdersPage title="Pruebas de reparación" statuses={['QA']}/> ,undefined,['ORDERS_UPDATE'])},{path:'/delivery',element:secure(<OrdersPage title="Entrega" statuses={['QA','COMPLETED']}/> ,undefined,['PAYMENTS_RECEIVE'])},
  {path:'/technicians',element:secure(<TeamPage/>,['ADMIN','OWNER','MANAGER'],['USERS_MANAGE'])},{path:'/team',element:secure(<><TeamPage/><div className="px-4 lg:px-6"><TeamUserEditor/></div></>,['ADMIN','OWNER','MANAGER'],['USERS_MANAGE'])},{path:'/branches',element:secure(<BranchesPage/>,['ADMIN','OWNER','MANAGER'],['BRANCHES_READ'])},
  {path:'/parts',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/inventory',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/suppliers',element:secure(<SuppliersPage/>,undefined,['INVENTORY_READ'])},{path:'/purchases',element:secure(<Purchases/>,undefined,['INVENTORY_PURCHASE'])},
  {path:'/expenses',element:secure(<ExpensesPage/>,undefined,['FINANCE_READ'])},{path:'/settings',element:secure(<BusinessSettings/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_READ'])},{path:'/settings/overview',element:secure(<SettingsPage/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_READ'])},{path:'/whatsapp',element:secure(<IntegrationPage kind="WhatsApp"/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_MANAGE'])},{path:'/ai',element:secure(<IntegrationPage kind="IA"/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_MANAGE'])},
  {path:'/calendar',element:secure(<OrdersPage title="Agenda operativa"/>,undefined,['ORDERS_READ'])},{path:'/reports',element:secure(<RealDashboard/>,undefined,['REPORTS_READ'])},
  {path:'/customers/import',element:secure(<CustomerCsvTools/>,undefined,['CUSTOMERS_WRITE'])},
  {path:'/devices',element:secure(<DeviceManagementPage/>,undefined,['DEVICES_READ'])},
  {path:'/notifications',element:secure(<OperationalAlertsPage/>,undefined,['ORDERS_READ','INVENTORY_READ','FINANCE_READ'])},
  {path:'/settings/order-workflows',element:secure(<WorkflowSettingsPage/>,['ADMIN','OWNER'],['WORKFLOWS_MANAGE'])},
  {path:'/settings/order-preferences',element:secure(<OrderPreferencesPage/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_MANAGE'])},
  {path:'/settings/account',element:secure(<AccountProfilePage/>)},
  {path:'/settings/notifications',element:secure(<NotificationSettingsPage/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_MANAGE'])},
  {path:'/reports/finance',element:secure(<FixiFinancialReportingPage/>,['ADMIN','OWNER'],['FINANCE_READ'])},
  {path:'/orders/unassigned',element:secure(<OrdersPage title="Por asignar" unassignedOnly/>,undefined,['USERS_MANAGE'])},
  {path:'/orders/pending-payment',element:secure(<OrdersPage title="Pendientes de pago" paymentPendingOnly/>,undefined,['PAYMENTS_RECEIVE'])},
  {path:'/platform',element:<Protected platformRole="SUPER_ADMIN"><PlatformShell/></Protected>,children:[
    {path:'',element:<PlatformDashboardPage/>},
    {path:'tenants',element:<PlatformTenantsPage/>},
    {path:'tenants/:tenantId',element:<PlatformTenantDetailPage/>},
    {path:'users',element:<PlatformUsersPage/>},
    {path:'plans',element:<PlatformPlansPage/>},
    {path:'subscriptions',element:<PlatformSubscriptionsPage/>},
    {path:'audit',element:<PlatformAuditPage/>},
    {path:'hotels',element:<Navigate to="/platform/tenants" replace/>},
  ]},
  {path:'*',element:<Navigate to="/" replace/>},
]
