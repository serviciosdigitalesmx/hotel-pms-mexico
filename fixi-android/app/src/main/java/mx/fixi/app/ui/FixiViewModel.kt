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
    fun login(username:String,password:String) = viewModelScope.launch { busy=true; error=null; runCatching { val r=api.login(LoginRequest(username,password)); if(!r.isSuccessful) error="No se pudo iniciar sesión (${r.code()})" else { session.username=username; loggedIn=true; loadMe(); loadOrders() } }.onFailure { error="No hay conexión con Fixi" }; busy=false }
    private suspend fun loadMe(){ api.me().body()?.let { user=it } }
    fun loadOrders() = viewModelScope.launch { busy=true; runCatching { val first=api.serviceOrders(); if(!first.isSuccessful) error="No se pudieron cargar las órdenes (${first.code()})" else { val page=first.body(); val loaded=page?.content.orEmpty().toMutableList(); val pageCount=page?.totalPages?:0; for(index in 1 until pageCount){val next=api.serviceOrders(index);if(!next.isSuccessful){error="No se pudieron completar las órdenes (${next.code()})";break};loaded+=next.body()?.content.orEmpty()};orders=loaded} }.onFailure { error="No hay conexión con Fixi" }; busy=false }
    fun logout() = viewModelScope.launch { runCatching { api.logout() }; session.clear(); loggedIn=false; user=null; orders=emptyList() }
    fun selectBranch(branchId: String) = viewModelScope.launch { busy=true; error=null; runCatching { val r=api.selectBranch(SelectBranchRequest(branchId)); if(!r.isSuccessful) error="No se pudo cambiar de sucursal (${r.code()})" else { loadMe(); loadOrders() } }.onFailure { error="No se pudo cambiar de sucursal" }; busy=false }
}
