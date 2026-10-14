package mx.fixi.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import mx.fixi.app.data.*
import javax.inject.Inject

@HiltViewModel class OperationsViewModel @Inject constructor(private val api: FixiApi): ViewModel() {
    var customers by mutableStateOf<List<Customer>>(emptyList()); private set
    var devices by mutableStateOf<List<Device>>(emptyList()); private set
    var inventory by mutableStateOf<List<InventoryProduct>>(emptyList()); private set
    var timeline by mutableStateOf<List<TimelineEvent>>(emptyList()); private set
    var tests by mutableStateOf<List<RepairTest>>(emptyList()); private set
    var selectedOrder by mutableStateOf<ServiceOrder?>(null); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var message by mutableStateOf<String?>(null); private set
    var branches by mutableStateOf<List<Branch>>(emptyList()); private set
    var evidence by mutableStateOf<List<Evidence>>(emptyList()); private set
    fun loadReceptionData() = call { customers=fetchAllCustomers();val locations=api.branches();if(locations.isSuccessful)branches=locations.body().orEmpty() }
    fun loadReceptionDevices(customerId: String) = call { val response=api.devices(customerId);if(!response.isSuccessful)error="No se pudieron cargar los equipos (${response.code()})" else devices=response.body().orEmpty() }
    fun receiveOrder(customerId:String,deviceId:String?,branchId:String?,summary:String,failure:String,checklist:String,promisedAt:String,priority:String) = call { if(customerId.isBlank()||summary.isBlank()||failure.isBlank()||checklist.isBlank()||promisedAt.isBlank()){error="Completa cliente, resumen, falla, checklist y compromiso";return@call};val response=api.createServiceOrder(ServiceOrderRequest(customerId,deviceId,branchId,summary.trim(),failure.trim(),checklist.trim(),promisedAt.trim(),priority=priority));if(!response.isSuccessful)error="No se pudo crear la orden (${response.code()})" else {message="Orden recibida correctamente";devices=emptyList()} }
    private suspend fun fetchAllCustomers():List<Customer>{val rows=mutableListOf<Customer>();val first=api.customers();if(!first.isSuccessful)throw IllegalStateException("CUSTOMERS_HTTP_${first.code()}");val page=first.body()?:return rows;rows+=page.content;for(index in 1 until page.totalPages){val next=api.customers(index);if(!next.isSuccessful)throw IllegalStateException("CUSTOMERS_HTTP_${next.code()}");rows+=next.body()?.content.orEmpty()};return rows}
    fun loadCustomers() = call { customers = fetchAllCustomers() }
    fun loadInventory() = call { inventory = api.inventory().body().orEmpty() }
    fun loadOrder(id: String) = call { val order=api.serviceOrder(id);if(!order.isSuccessful)error="No se pudo cargar la orden (${order.code()})" else selectedOrder=order.body();val history=api.timeline(id);if(history.isSuccessful)timeline=history.body().orEmpty();val checks=api.tests(id);if(checks.isSuccessful)tests=checks.body().orEmpty();val documents=api.evidence(id);if(documents.isSuccessful)evidence=documents.body().orEmpty() }
    fun loadDevices(customerId: String) = call { devices = api.devices(customerId).body().orEmpty() }
    fun createCustomer(firstName: String, lastName: String, email: String, phone: String) = call { val r=api.createCustomer(CustomerRequest(firstName,lastName,email.ifBlank{null},phone.ifBlank{null})); if(!r.isSuccessful) error="No se pudo crear el cliente (${r.code()})" else { customers=fetchAllCustomers();message="Cliente creado" } }
    fun createDevice(customerId: String, category: String, manufacturer: String, model: String, serial: String, imei: String, details: String) = call { if(category.isBlank()){error="Indica la categoría del equipo";return@call};val r=api.createDevice(customerId,DeviceRequest(category.trim(),manufacturer.ifBlank{null},model.ifBlank{null},serial.ifBlank{null},imei.ifBlank{null},details.ifBlank{null})); if(!r.isSuccessful) error="No se pudo registrar el equipo (${r.code()})" else {message="Equipo registrado";devices=api.devices(customerId).body().orEmpty()} }
    fun updateOrder(id: String, status: String, diagnosis: String, workLog: String) = call { val r=api.updateServiceOrder(id, ServiceOrderUpdateRequest(status, diagnosis.ifBlank{null}, workLog.ifBlank{null})); if(!r.isSuccessful) error="No se pudo actualizar la orden (${r.code()})" else { selectedOrder=r.body(); message="Orden actualizada" } }
    fun quote(id: String, solution: String, minutes: Int, labor: Double, parts: Double, tax: Double) = call { val r=api.quote(id, QuotationRequest(solution,minutes,labor,parts,tax)); if(!r.isSuccessful) error="No se pudo guardar la cotización (${r.code()})" else { selectedOrder=r.body(); message="Cotización guardada" } }
    fun addTest(id: String, name: String, passed: Boolean, notes: String) = call { val r=api.addTest(id, RepairTestRequest(name,passed,notes.ifBlank{null})); if(!r.isSuccessful) error="No se pudo guardar la prueba (${r.code()})" else { tests=tests + listOfNotNull(r.body()); message="Prueba guardada" } }
    fun addEvidence(orderId: String, fileName: String, contentType: String, storageKey: String) = call { val r=api.addEvidence(DocumentEvidenceRequest(serviceOrderId=orderId,fileName=fileName,contentType=contentType,storageKey=storageKey,accessLevel="STAFF")); if(!r.isSuccessful) error="No se pudo registrar la evidencia (${r.code()})" else { evidence=evidence+listOfNotNull(r.body()); message="Evidencia registrada" } }
    fun orderAction(id: String, action: String, evidenceText: String) = call { if(evidenceText.isBlank()){error="Agrega la evidencia o nota requerida";return@call};val response=when(action){"deliver"->api.deliver(id);"authorize"->api.authorize(id,AuthorizationRequest(evidenceText.trim()));else->api.reject(id,AuthorizationRequest(evidenceText.trim()))};if(!response.isSuccessful)error="No se pudo ${if(action=="deliver")"entregar" else if(action=="authorize")"autorizar" else "rechazar"} la orden (${response.code()})" else {message=when(action){"deliver"->"Orden entregada";"authorize"->"Cotización autorizada";else->"Cotización rechazada"};loadOrder(id)} }
    fun loadBranches() = call { branches=api.branches().body().orEmpty() }
    private fun call(block: suspend () -> Unit) = viewModelScope.launch { loading=true;error=null;message=null;try{block()}catch(cancelled:CancellationException){throw cancelled}catch(failure:Exception){error="No se pudo completar la operación (${failure.message?:"error de red"})"}finally{loading=false} }
}
