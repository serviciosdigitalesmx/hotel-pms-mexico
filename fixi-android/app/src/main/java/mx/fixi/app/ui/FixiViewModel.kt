package mx.fixi.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import mx.fixi.app.data.*
import javax.inject.Inject

@HiltViewModel class FixiViewModel @Inject constructor(private val api: FixiApi, private val session: SessionStore): ViewModel() {
    var loggedIn by mutableStateOf(session.isLoggedIn()); private set
    var busy by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var user by mutableStateOf<UserResponse?>(null); private set
    var orders by mutableStateOf<List<ServiceOrder>>(emptyList()); private set
    var branches by mutableStateOf<List<Branch>>(emptyList()); private set
    fun login(username:String,password:String) = viewModelScope.launch { busy=true;error=null;try{val response=api.login(LoginRequest(username,password));if(!response.isSuccessful)error="No se pudo iniciar sesión (${response.code()})" else {session.username=username;loggedIn=true;loadMe();loadOrdersNow()}}catch(_:Exception){error="No hay conexión con Fixi"}finally{busy=false} }
    private suspend fun loadMe(){val response=api.me();if(response.isSuccessful)user=response.body() else error="No se pudo cargar el perfil (${response.code()})"}
    fun loadBranches() = viewModelScope.launch { try{val response=api.branches();if(response.isSuccessful)branches=response.body().orEmpty() else error="No se pudieron cargar las sucursales (${response.code()})"}catch(_:Exception){error="No se pudieron cargar las sucursales"} }
    private suspend fun loadOrdersNow(){val first=api.serviceOrders();if(!first.isSuccessful){error="No se pudieron cargar las órdenes (${first.code()})";return};val page=first.body();val loaded=page?.content.orEmpty().toMutableList();val pageCount=page?.totalPages?:0;for(index in 1 until pageCount){val next=api.serviceOrders(index);if(!next.isSuccessful){error="No se pudieron completar las órdenes (${next.code()})";return};loaded+=next.body()?.content.orEmpty()};orders=loaded}
    fun loadOrders() = viewModelScope.launch { busy=true;error=null;try{loadOrdersNow()}catch(_:Exception){error="No hay conexión con Fixi"}finally{busy=false} }
    fun logout() = viewModelScope.launch { runCatching { api.logout() }; session.clear(); loggedIn=false; user=null; orders=emptyList() }
    fun selectBranch(branchId: String) = viewModelScope.launch { busy=true;error=null;try{val response=api.selectBranch(SelectBranchRequest(branchId));if(!response.isSuccessful)error="No se pudo cambiar de sucursal (${response.code()})" else {loadMe();loadOrdersNow()}}catch(_:Exception){error="No se pudo cambiar de sucursal"}finally{busy=false} }
}
