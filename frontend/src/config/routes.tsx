import { type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { Landing,LoginPage,SignupPage,ForgotPasswordPage,ResetPasswordPage,VerifyEmailPage,AcceptInvitationPage,CustomerPortalPage,ChangePasswordPage } from '@/fixi/pages'
import { Protected,RealDashboard,BranchDashboard,OrderReportingPage,InventoryReportingPage,ProductivityReportingPage,CustomersPage,CustomerDetailPage,OrdersPage,BranchesPage,TeamPage,SuppliersPage,OnboardingPage,SettingsPage,IntegrationPage } from '@/fixi/operations'
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
import { DeviceManagementPage } from '@/fixi/devices'
import { NotificationSettingsPage } from '@/fixi/notification-settings'
import { FixiFinancialReportingPage } from '@/fixi/financial-reporting'
import { TeamUserEditor } from '@/fixi/team-user-editor'
import { OperationalAlertsPage } from '@/fixi/operational-alerts'
export interface RouteConfig { path:string; element:ReactNode; children?:RouteConfig[] }
const secure=(element:ReactNode,roles?:string[],capabilities?:string[])=><Protected roles={roles} capabilities={capabilities}>{element}</Protected>
export const routes:RouteConfig[]=[
  {path:'/',element:<Landing/>},{path:'/landing',element:<Landing/>},{path:'/login',element:<LoginPage/>},{path:'/change-password',element:<ChangePasswordPage/>},{path:'/signup',element:<SignupPage/>},{path:'/forgot-password',element:<ForgotPasswordPage/>},{path:'/reset-password',element:<ResetPasswordPage/>},{path:'/verify-email',element:<VerifyEmailPage/>},{path:'/accept-invitation',element:<AcceptInvitationPage/>},{path:'/portal/:token',element:<CustomerPortalPage/>},
  {path:'/dashboard',element:secure(<RealDashboard/>)},{path:'/branch-dashboard',element:secure(<BranchDashboard/>)},{path:'/reports/orders',element:secure(<OrderReportingPage/>,undefined,['REPORTS_READ'])},{path:'/reports/inventory',element:secure(<InventoryReportingPage/>,undefined,['REPORTS_READ'])},{path:'/reports/productivity',element:secure(<ProductivityReportingPage/>,undefined,['REPORTS_READ'])},{path:'/onboarding',element:secure(<OnboardingPage/>)},
  {path:'/customers',element:secure(<CustomersPage/>,undefined,['CUSTOMERS_READ'])},{path:'/customers/:id',element:secure(<CustomerDetailPage/>,undefined,['CUSTOMERS_READ'])},{path:'/equipment',element:secure(<DeviceManagementPage/>,undefined,['DEVICES_READ'])},{path:'/reception',element:<Navigate to="/reception/new" replace/>},
  {path:'/reception/new',element:secure(<ReceptionOrderForm/>,undefined,['ORDERS_CREATE'])},{path:'/services',element:secure(<ServiceCatalogManagementPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders',element:secure(<OrdersPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders/:id/payment',element:secure(<ServiceOrderPaymentSettlementPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders/:id',element:secure(<ServiceOrderDetail/>,undefined,['ORDERS_READ'])},
  {path:'/diagnostics',element:secure(<OrdersPage title="Diagnóstico" statuses={['REVIEW','DIAGNOSIS']}/>,undefined,['ORDERS_READ'])},{path:'/repairs',element:secure(<OrdersPage title="Reparaciones" statuses={['AUTHORIZED','IN_PROGRESS']}/>,undefined,['ORDERS_READ'])},{path:'/tests',element:secure(<OrdersPage title="Pruebas de reparación" statuses={['QA']}/> ,undefined,['ORDERS_READ'])},{path:'/delivery',element:secure(<OrdersPage title="Entrega" statuses={['QA','COMPLETED']}/> ,undefined,['ORDERS_READ'])},
  {path:'/technicians',element:secure(<TeamPage/>,['ADMIN','OWNER','MANAGER'],['USERS_MANAGE'])},{path:'/team',element:secure(<><TeamPage/><div className="px-4 lg:px-6"><TeamUserEditor/></div></>,['ADMIN','OWNER','MANAGER'],['USERS_MANAGE'])},{path:'/branches',element:secure(<BranchesPage/>,['ADMIN','OWNER','MANAGER'],['BRANCHES_READ'])},
  {path:'/parts',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/inventory',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/suppliers',element:secure(<SuppliersPage/>,undefined,['INVENTORY_READ'])},{path:'/purchases',element:secure(<Purchases/>,undefined,['INVENTORY_PURCHASE'])},
  {path:'/expenses',element:secure(<ExpensesPage/>,undefined,['FINANCE_READ'])},{path:'/settings',element:secure(<BusinessSettings/>,['ADMIN','OWNER','MANAGER'])},{path:'/settings/overview',element:secure(<SettingsPage/>,['ADMIN','OWNER','MANAGER'])},{path:'/whatsapp',element:secure(<IntegrationPage kind="WhatsApp"/>,['ADMIN','OWNER','MANAGER'])},{path:'/ai',element:secure(<IntegrationPage kind="IA"/>,['ADMIN','OWNER','MANAGER'])},
  {path:'/calendar',element:secure(<OrdersPage title="Agenda operativa"/>,undefined,['ORDERS_READ'])},{path:'/reports',element:secure(<RealDashboard/>,undefined,['REPORTS_READ'])},
  {path:'/customers/import',element:secure(<CustomerCsvTools/>,undefined,['CUSTOMERS_WRITE'])},
  {path:'/devices',element:secure(<DeviceManagementPage/>,undefined,['DEVICES_READ'])},
  {path:'/notifications',element:secure(<OperationalAlertsPage/>,undefined,['ORDERS_READ','INVENTORY_READ','FINANCE_READ'])},
  {path:'/settings/order-workflows',element:secure(<WorkflowSettingsPage/>,['ADMIN','OWNER'],['WORKFLOWS_MANAGE'])},
  {path:'/settings/notifications',element:secure(<NotificationSettingsPage/>,['ADMIN','OWNER','MANAGER'],['SETTINGS_MANAGE'])},
  {path:'/reports/finance',element:secure(<FixiFinancialReportingPage/>,['ADMIN','OWNER'],['FINANCE_READ'])},
  {path:'/orders/unassigned',element:secure(<OrdersPage title="Por asignar" unassignedOnly/>,undefined,['ORDERS_READ'])},
  {path:'/orders/pending-payment',element:secure(<OrdersPage title="Pendientes de pago" paymentPendingOnly/>,undefined,['ORDERS_READ'])},
  {path:'*',element:<Navigate to="/" replace/>},
]
