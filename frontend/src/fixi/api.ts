export type Json = Record<string, unknown>
export type SessionUser = { username:string; role:string; hotelId?:string; tenantId?:string; branchId?:string; businessName?:string; tenantStatus?:string; onboardingCompleted?:boolean; mustChangePassword?:boolean }
export type ServiceOrder = { id:string; customerId:string; deviceId?:string; branchId?:string; summary:string; status:string; diagnosis?:string; workLog?:string; assignedUserId?:string; proposedSolution?:string; estimatedMinutes?:number; laborAmount?:number; partsAmount?:number; taxAmount?:number; quoteTotal?:number; deliveredAt?:string; createdAt?:string; updatedAt?:string }
export type Customer = { id:string; firstName:string; lastName:string; email?:string; phone?:string; city?:string; createdAt?:string }
export type Device = { id:string; customerId:string; category:string; manufacturer?:string; model?:string; serialNumber?:string; imei?:string; customFields?:string }
export type Branch = { id:string; hotelId:string; name:string; active:boolean; createdAt:string }
export type TeamUser = { id:string; username:string; email:string; role:string; active:boolean; mustChangePassword:boolean }
export type Product = { id:string; sku:string; name:string; optional:boolean }
export type Supplier = { id:string; name:string; taxId?:string; contact?:string }

function csrfToken(){return document.cookie.match(/(?:^|;\s*)csrf_token=([^;]*)/)?.[1]}
async function raw(path:string, init:RequestInit={}, retry=true){
  const headers=new Headers(init.headers); if(init.body&&!headers.has('Content-Type'))headers.set('Content-Type','application/json')
  const token=csrfToken(); if(token&&init.method&&init.method!=='GET')headers.set('X-CSRF-Token',decodeURIComponent(token))
  const response=await fetch(path,{...init,headers,credentials:'include'})
  if(response.status===401&&retry&&!path.includes('/login')&&!path.includes('/refresh')){const refreshed=await fetch('/api/v1/auth/refresh',{method:'POST',credentials:'include'});if(refreshed.ok)return raw(path,init,false)}
  return response
}
async function api<T>(path:string,init:RequestInit={}){const r=await raw(path,init);if(!r.ok){let message=`HTTP ${r.status}`;try{const p=await r.json() as {detail?:string;title?:string;errors?:string[]};message=p.errors?.filter(Boolean).join(' ')||p.detail||p.title||message}catch{message=`HTTP ${r.status}`}throw new Error(message)}if(r.status===204)return undefined as T;return r.json() as Promise<T>}
export const get=<T>(p:string)=>api<T>(p)
export const post=<T>(p:string,b?:unknown)=>api<T>(p,{method:'POST',body:b===undefined?undefined:JSON.stringify(b)})
export const patch=<T>(p:string,b?:unknown)=>api<T>(p,{method:'PATCH',body:b===undefined?undefined:JSON.stringify(b)})
export const put=<T>(p:string,b?:unknown)=>api<T>(p,{method:'PUT',body:b===undefined?undefined:JSON.stringify(b)})
export const del=<T>(p:string)=>api<T>(p,{method:'DELETE'})

export const login=(username:string,password:string)=>post<{mustChangePassword?:boolean}>('/api/v1/auth/login',{username,password})
export const logout=()=>post<void>('/api/v1/auth/logout')
export const currentUser=()=>get<SessionUser>('/api/v1/auth/me').catch(()=>null)
export const signup=(body:unknown)=>post('/api/v1/auth/register',body)
export const verifyEmail=(token:string)=>post<void>(`/api/v1/auth/public/verify-email?token=${encodeURIComponent(token)}`)
export const forgotPassword=(email:string)=>post<void>('/api/v1/auth/public/forgot-password',{email})
export const resetPassword=(token:string,password:string)=>post<void>('/api/v1/auth/public/reset-password',{token,password})
export const acceptInvitation=(body:unknown)=>post<void>('/api/v1/auth/public/invitations/accept',body)
export const completeOnboarding=(body:unknown)=>post<void>('/api/v1/auth/onboarding/complete',body)
export const listServiceOrders=()=>get<ServiceOrder[]>('/api/v1/service-orders')
export const getServiceOrder=(id:string)=>get<ServiceOrder>(`/api/v1/service-orders/${id}`)
export const createServiceOrder=(body:unknown)=>post<ServiceOrder>('/api/v1/service-orders',body)
export const updateServiceOrder=(id:string,body:unknown)=>patch<ServiceOrder>(`/api/v1/service-orders/${id}`,body)
export const authorizeServiceOrder=(id:string,evidence:string)=>post<ServiceOrder>(`/api/v1/service-orders/${id}/authorize`,{evidence})
export const rejectServiceOrder=(id:string,evidence:string)=>post<ServiceOrder>(`/api/v1/service-orders/${id}/reject`,{evidence})
export const quoteServiceOrder=(id:string,body:unknown)=>post<ServiceOrder>(`/api/v1/service-orders/${id}/quotation`,body)
export const addRepairTest=(id:string,body:unknown)=>post(`/api/v1/service-orders/${id}/tests`,body)
export const listRepairTests=(id:string)=>get<Array<{id:string;testName:string;passed:boolean;notes?:string;createdAt:string}>>(`/api/v1/service-orders/${id}/tests`)
export const serviceOrderTimeline=(id:string)=>get<Array<{id:string;fromStatus?:string;toStatus:string;actor:string;notes?:string;createdAt:string}>>(`/api/v1/service-orders/${id}/timeline`)
export const deliverServiceOrder=(id:string)=>post<ServiceOrder>(`/api/v1/service-orders/${id}/deliver`)
export type TimeEntry={id:string;serviceOrderId:string;userId:string;minutes:number;note?:string;createdAt:string}
export const listTimeEntries=(id:string)=>get<TimeEntry[]>(`/api/v1/service-orders/${id}/time`)
export const addTimeEntry=(id:string,body:unknown)=>post<TimeEntry>(`/api/v1/service-orders/${id}/time`,body)
export type DocumentEvidence={id:string;serviceOrderId:string;customerId:string;fileName:string;contentType:string;storageKey:string;accessLevel:string;createdAt:string}
export const listEvidence=(id:string)=>get<DocumentEvidence[]>(`/api/v1/documents/order/${id}`)
export const addEvidence=(body:unknown)=>post<DocumentEvidence>('/api/v1/documents',body)
export type PortalOperation={resourceType:string;resourceId:string;customerId:string;expiresAt:string}
export const issuePortalToken=(orderId:string)=>post<{token:string;resourceId:string}>(`/api/v1/portal/tokens/service-orders/${orderId}?ttlHours=72`)
export const portalOperation=(token:string)=>get<PortalOperation>(`/api/public/portal/operations/${encodeURIComponent(token)}`)
export const portalDocuments=(token:string)=>get<DocumentEvidence[]>(`/api/public/portal/operations/${encodeURIComponent(token)}/documents`)
export const portalOtp=(token:string)=>post<{deliveryMode:string;testCode?:string;expiresAt:string}>(`/api/public/portal/operations/${encodeURIComponent(token)}/otp/challenge`)
export const portalVerify=(token:string,code:string)=>post<{verified:boolean}>(`/api/public/portal/operations/${encodeURIComponent(token)}/otp/verify?code=${encodeURIComponent(code)}`)
export const portalAuthorize=(token:string,evidence:string)=>post<{authorized:boolean}>(`/api/public/portal/operations/${encodeURIComponent(token)}/authorize?evidence=${encodeURIComponent(evidence)}`)
export const listCustomers=(page=0)=>get<{content:Customer[]} | Customer[]>(`/api/v1/customers?page=${page}&size=100`)
export const getCustomer=(id:string)=>get<Customer>(`/api/v1/customers/${id}`)
export const createCustomer=(body:unknown)=>post<Customer>('/api/v1/customers',body)
export const updateCustomer=(id:string,body:unknown)=>put<Customer>(`/api/v1/customers/${id}`,body)
export const listDevices=(customerId:string)=>get<Device[]>(`/api/v1/customers/${customerId}/devices`)
export const createDevice=(customerId:string,body:unknown)=>post<Device>(`/api/v1/customers/${customerId}/devices`,body)
export const customerServiceOrders=(customerId:string)=>get<ServiceOrder[]>(`/api/v1/service-orders/customer/${customerId}`)
export const listBranches=()=>get<Branch[]>('/api/v1/auth/branches')
export const createBranch=(name:string)=>post<Branch>('/api/v1/auth/branches',{name})
export const updateBranch=(id:string,name:string)=>patch<Branch>(`/api/v1/auth/branches/${id}`,{name})
export const setBranchActive=(id:string,value:boolean)=>patch<void>(`/api/v1/auth/branches/${id}/active?value=${value}`)
export const selectBranch=(branchId:string)=>post<void>('/api/v1/auth/select-branch',{branchId})
export const listUsers=()=>get<TeamUser[]>('/api/v1/auth/users')
export const createUser=(body:unknown)=>post<TeamUser>('/api/v1/auth/users',body)
export const inviteUser=(body:unknown)=>post<void>('/api/v1/auth/invitations',body)
export const assignUserBranch=(branchId:string,userId:string)=>post<void>(`/api/v1/auth/branches/${branchId}/members/${userId}`)
export const removeUserBranch=(branchId:string,userId:string)=>del<void>(`/api/v1/auth/branches/${branchId}/members/${userId}`)
export type CapabilityGrant={id:string;tenantId:string;userId:string;branchId?:string;capabilityKey:string;enabled:boolean;updatedAt:string}
export const listUserCapabilityGrants=(userId:string)=>get<CapabilityGrant[]>(`/api/v1/auth/capabilities/users/${userId}`)
export const setUserCapabilityGrant=(userId:string,capabilityKey:string,enabled:boolean,branchId?:string)=>put<CapabilityGrant>(`/api/v1/auth/capabilities/users/${userId}`,{capabilityKey,enabled,branchId:branchId||null})
export const listProducts=()=>get<Product[]>('/api/v1/inventory/products')
export const createProduct=(body:unknown)=>post<Product>('/api/v1/inventory/products',body)
export const moveStock=(body:unknown)=>post('/api/v1/inventory/movements',body)
export type StockBalance={id:string;branchId:string;productId:string;quantity:number;updatedAt:string}
export const stock=(branch:string)=>get<StockBalance[]>(`/api/v1/inventory/branches/${branch}/stock`)
export const reserveStock=(body:unknown)=>post('/api/v1/inventory/reservations',body)
export const releaseStock=(orderId:string,productId:string)=>post(`/api/v1/inventory/reservations/${orderId}/${productId}/release`)
export type WarrantyClaim={id:string;serviceOrderId:string;customerId:string;expiresAt:string;description:string;status:string;createdAt:string}
export const listWarrantyClaims=(customerId:string)=>get<WarrantyClaim[]>(`/api/v1/warranty-claims/customer/${customerId}`)
export const openWarrantyClaim=(body:unknown)=>post<WarrantyClaim>('/api/v1/warranty-claims',body)
export const closeWarrantyClaim=(id:string)=>post<WarrantyClaim>(`/api/v1/warranty-claims/${id}/close`)
export const listSuppliers=()=>get<Supplier[]>('/api/v1/procurement/suppliers')
export const createSupplier=(body:unknown)=>post<Supplier>('/api/v1/procurement/suppliers',body)
export const listPurchases=()=>get<unknown[]>('/api/v1/procurement/purchases')
export const createPurchase=(body:unknown)=>post('/api/v1/procurement/purchases',body)
export const receivePurchase=(id:string,body:unknown)=>post(`/api/v1/procurement/purchases/${id}/receive`,body)
export const whatsappStatus=()=>get<{state:string;qrCode?:string}>('/api/v1/stays/whatsapp')
export const whatsappConnect=()=>post<{state:string;qrCode?:string}>('/api/v1/stays/whatsapp/connect')
export const whatsappMessages=(id:string)=>get<Array<{id:string;direction:string;body:string;createdAt:string}>>(`/api/v1/whatsapp/inbox/conversations/${id}/messages`)
export type WhatsAppConversation={id:string;channelKey:string;displayName?:string;aiMode:string;handoff:boolean;updatedAt:string}
export const whatsappConversations=()=>get<WhatsAppConversation[]>('/api/v1/whatsapp/inbox/conversations')
export const whatsappReply=(id:string,text:string)=>post(`/api/v1/whatsapp/inbox/conversations/${id}/messages?text=${encodeURIComponent(text)}`)
export const whatsappHandoff=(id:string)=>post(`/api/v1/whatsapp/inbox/conversations/${id}/handoff`)
export const assistantChat=(content:string)=>post<{answer:string;toolCalls:unknown[]}>('/api/v1/stays/assistant/chat',{messages:[{role:'user',content,toolCallId:null,toolName:null,toolCalls:[]}]})
export const industryProfile=()=>get<{industryKey:string;enabledModulesJson:string;dynamicFieldsJson:string;formsLabelsJson:string;catalogsTemplatesJson:string}>('/api/v1/auth/industry-profile')
export const updateIndustryProfile=(body:unknown)=>put('/api/v1/auth/industry-profile',body)
export const changePassword=(currentPassword:string,newPassword:string)=>post<void>('/api/v1/auth/change-password',{currentPassword,newPassword})
