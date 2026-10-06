package mx.fixi.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    fun loadCustomers() = call { customers = api.customers().body()?.content.orEmpty() }
    fun loadInventory() = call { inventory = api.inventory().body().orEmpty() }
    fun loadOrder(id: String) = call { selectedOrder = api.serviceOrder(id).body(); timeline = api.timeline(id).body().orEmpty(); tests = api.tests(id).body().orEmpty() }
    fun loadDevices(customerId: String) = call { devices = api.devices(customerId).body().orEmpty() }
    fun createCustomer(firstName: String, lastName: String, email: String, phone: String) = call { val r=api.createCustomer(CustomerRequest(firstName,lastName,email.ifBlank{null},phone.ifBlank{null})); if(!r.isSuccessful) error="No se pudo crear el cliente (${r.code()})" else { message="Cliente creado"; loadCustomers() } }
    fun createDevice(customerId: String, brand: String, model: String, serial: String) = call { val r=api.createDevice(customerId,DeviceRequest(brand,model,serial.ifBlank{null},customerId)); if(!r.isSuccessful) error="No se pudo registrar el equipo (${r.code()})" else message="Equipo registrado" }
    fun orderAction(id: String, action: String) = call { val r=when(action){"authorize"->api.authorize(id);"reject"->api.reject(id);else->api.deliver(id)}; if(!r.isSuccessful) error="Acción no permitida (${r.code()})" else message="Acción aplicada" }
    fun loadBranches() = call { branches=api.branches().body().orEmpty() }
    private fun call(block: suspend () -> Unit) = viewModelScope.launch { loading=true; error=null; runCatching { block() }.onFailure { error = "No se pudo cargar la información (${it.message ?: "error de red"})" }; loading=false }
}
