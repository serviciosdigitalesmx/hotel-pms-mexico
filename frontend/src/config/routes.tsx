import { type ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { Landing,LoginPage,SignupPage,ForgotPasswordPage,ResetPasswordPage,VerifyEmailPage,AcceptInvitationPage,CustomerPortalPage } from '@/fixi/pages'
import { Protected,RealDashboard,CustomersPage,CustomerDetailPage,EquipmentPage,ReceptionPage,OrdersPage,BranchesPage,TeamPage,InventoryPage,SuppliersPage,OnboardingPage,SettingsPage,IntegrationPage } from '@/fixi/operations'
import { ServiceOrderDetail } from '@/fixi/order-detail'
import { Purchases } from '@/fixi/purchases'
import { BusinessSettings } from '@/fixi/settings'
export interface RouteConfig { path:string; element:ReactNode; children?:RouteConfig[] }
const secure=(element:ReactNode)=><Protected>{element}</Protected>
export const routes:RouteConfig[]=[
  {path:'/',element:<Landing/>},{path:'/landing',element:<Landing/>},{path:'/login',element:<LoginPage/>},{path:'/signup',element:<SignupPage/>},{path:'/forgot-password',element:<ForgotPasswordPage/>},{path:'/reset-password',element:<ResetPasswordPage/>},{path:'/verify-email',element:<VerifyEmailPage/>},{path:'/accept-invitation',element:<AcceptInvitationPage/>},{path:'/portal/:token',element:<CustomerPortalPage/>},
  {path:'/dashboard',element:secure(<RealDashboard/>)},{path:'/onboarding',element:secure(<OnboardingPage/>)},
  {path:'/customers',element:secure(<CustomersPage/>)},{path:'/customers/:id',element:secure(<CustomerDetailPage/>)},{path:'/equipment',element:secure(<EquipmentPage/>)},{path:'/reception',element:secure(<ReceptionPage/>)},
  {path:'/service-orders',element:secure(<OrdersPage/>)},{path:'/service-orders/:id',element:secure(<ServiceOrderDetail/>)},
  {path:'/diagnostics',element:secure(<OrdersPage title="Diagnóstico" statuses={['REVIEW','DIAGNOSIS']}/>)},{path:'/repairs',element:secure(<OrdersPage title="Reparaciones" statuses={['AUTHORIZED','IN_PROGRESS']}/>)},{path:'/tests',element:secure(<OrdersPage title="Pruebas de reparación" statuses={['QA']}/>)},{path:'/delivery',element:secure(<OrdersPage title="Entrega" statuses={['QA','COMPLETED']}/>)},
  {path:'/technicians',element:secure(<TeamPage/>)},{path:'/team',element:secure(<TeamPage/>)},{path:'/branches',element:secure(<BranchesPage/>)},
  {path:'/parts',element:secure(<InventoryPage/>)},{path:'/inventory',element:secure(<InventoryPage/>)},{path:'/suppliers',element:secure(<SuppliersPage/>)},{path:'/purchases',element:secure(<Purchases/>)},
  {path:'/settings',element:secure(<BusinessSettings/>)},{path:'/settings/overview',element:secure(<SettingsPage/>)},{path:'/whatsapp',element:secure(<IntegrationPage kind="WhatsApp"/>)},{path:'/ai',element:secure(<IntegrationPage kind="IA"/>)},
  {path:'/calendar',element:secure(<OrdersPage title="Agenda operativa"/>)},{path:'/reports',element:secure(<RealDashboard/>)},
  {path:'*',element:<Navigate to="/" replace/>},
]
