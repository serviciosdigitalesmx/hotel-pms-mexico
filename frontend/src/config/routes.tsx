import { type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { Landing,LoginPage,SignupPage,ForgotPasswordPage,ResetPasswordPage,VerifyEmailPage,AcceptInvitationPage,CustomerPortalPage } from '@/fixi/pages'
import { Protected,RealDashboard,BranchDashboard,OrderReportingPage,InventoryReportingPage,ProductivityReportingPage,CustomersPage,CustomerDetailPage,EquipmentPage,ReceptionPage,OrdersPage,BranchesPage,TeamPage,SuppliersPage,OnboardingPage,SettingsPage,IntegrationPage } from '@/fixi/operations'
import { ServiceOrderDetail } from '@/fixi/order-detail'
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
export interface RouteConfig { path:string; element:ReactNode; children?:RouteConfig[] }
const secure=(element:ReactNode,roles?:string[],capabilities?:string[])=><Protected roles={roles} capabilities={capabilities}>{element}</Protected>
export const routes:RouteConfig[]=[
  {path:'/',element:<Landing/>},{path:'/landing',element:<Landing/>},{path:'/login',element:<LoginPage/>},{path:'/signup',element:<SignupPage/>},{path:'/forgot-password',element:<ForgotPasswordPage/>},{path:'/reset-password',element:<ResetPasswordPage/>},{path:'/verify-email',element:<VerifyEmailPage/>},{path:'/accept-invitation',element:<AcceptInvitationPage/>},{path:'/portal/:token',element:<CustomerPortalPage/>},
  {path:'/dashboard',element:secure(<RealDashboard/>)},{path:'/branch-dashboard',element:secure(<BranchDashboard/>)},{path:'/reports/orders',element:secure(<OrderReportingPage/>)},{path:'/reports/inventory',element:secure(<InventoryReportingPage/>)},{path:'/reports/productivity',element:secure(<ProductivityReportingPage/>)},{path:'/onboarding',element:secure(<OnboardingPage/>)},
  {path:'/customers',element:secure(<CustomersPage/>)},{path:'/customers/:id',element:secure(<CustomerDetailPage/>)},{path:'/equipment',element:secure(<EquipmentPage/>)},{path:'/reception',element:secure(<ReceptionPage/>)},
  {path:'/reception/new',element:secure(<ReceptionOrderForm/>)},{path:'/services',element:secure(<ServiceCatalogManagementPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders',element:secure(<OrdersPage/>,undefined,['ORDERS_READ'])},{path:'/service-orders/:id',element:secure(<ServiceOrderDetail/>,undefined,['ORDERS_READ'])},
  {path:'/diagnostics',element:secure(<OrdersPage title="Diagnóstico" statuses={['REVIEW','DIAGNOSIS']}/>)},{path:'/repairs',element:secure(<OrdersPage title="Reparaciones" statuses={['AUTHORIZED','IN_PROGRESS']}/>)},{path:'/tests',element:secure(<OrdersPage title="Pruebas de reparación" statuses={['QA']}/>)},{path:'/delivery',element:secure(<OrdersPage title="Entrega" statuses={['QA','COMPLETED']}/>)},
  {path:'/technicians',element:secure(<TeamPage/>,['ADMIN','OWNER','MANAGER'])},{path:'/team',element:secure(<TeamPage/>,['ADMIN','OWNER','MANAGER'])},{path:'/branches',element:secure(<BranchesPage/>,['ADMIN','OWNER','MANAGER'])},
  {path:'/parts',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/inventory',element:secure(<InventoryCatalogPage/>,undefined,['INVENTORY_READ'])},{path:'/suppliers',element:secure(<SuppliersPage/>,undefined,['INVENTORY_READ'])},{path:'/purchases',element:secure(<Purchases/>,undefined,['INVENTORY_PURCHASE'])},
  {path:'/expenses',element:secure(<ExpensesPage/>,undefined,['FINANCE_READ'])},{path:'/settings',element:secure(<BusinessSettings/>,['ADMIN','OWNER','MANAGER'])},{path:'/settings/overview',element:secure(<SettingsPage/>,['ADMIN','OWNER','MANAGER'])},{path:'/whatsapp',element:secure(<IntegrationPage kind="WhatsApp"/>,['ADMIN','OWNER','MANAGER'])},{path:'/ai',element:secure(<IntegrationPage kind="IA"/>,['ADMIN','OWNER','MANAGER'])},
  {path:'/calendar',element:secure(<OrdersPage title="Agenda operativa"/>)},{path:'/reports',element:secure(<RealDashboard/>,undefined,['REPORTS_READ'])},
  {path:'/customers/import',element:secure(<CustomerCsvTools/>)},
  {path:'/devices',element:secure(<DeviceManagementPage/>)},
  {path:'/settings/order-workflows',element:secure(<WorkflowSettingsPage/>,['ADMIN','OWNER'])},
  {path:'/settings/notifications',element:secure(<NotificationSettingsPage/>,['ADMIN','OWNER','MANAGER'])},
  {path:'/reports/finance',element:secure(<FixiFinancialReportingPage/>,['ADMIN','OWNER'])},
  {path:'*',element:<Navigate to="/" replace/>},
]
