package mx.fixi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.fixi.app.data.*

@Composable fun CustomersScreen(vm: OperationsViewModel = hiltViewModel()) { LaunchedEffect(Unit){vm.loadCustomers()}; DataScreen("Clientes", vm.loading, vm.error, vm.customers.isEmpty(), {vm.loadCustomers()}) { vm.customers.forEach { CustomerRow(it) } } }
@Composable private fun CustomerRow(x: Customer) { ListItem(headlineContent={Text(listOfNotNull(x.firstName,x.lastName).joinToString(" ").ifBlank{"Cliente"})}, supportingContent={Text(x.email ?: x.phone ?: "Sin contacto")}) }
@Composable fun InventoryScreen(vm: OperationsViewModel = hiltViewModel()) { LaunchedEffect(Unit){vm.loadInventory()}; DataScreen("Inventario", vm.loading, vm.error, vm.inventory.isEmpty(), {vm.loadInventory()}) { vm.inventory.forEach { ListItem(headlineContent={Text(it.name ?: it.sku ?: "Refacción")}, supportingContent={Text("Existencia: ${it.quantity ?: 0}")}) } } }
@Composable fun OrderDetailScreen(id: String, vm: OperationsViewModel = hiltViewModel()) { LaunchedEffect(id){vm.loadOrder(id)}; Scaffold(topBar={TopAppBar(title={Text("Detalle de orden")})}) { p -> LazyColumn(Modifier.padding(p).padding(16.dp)){ item{vm.selectedOrder?.let{Text(it.orderNumber ?: it.id ?: "Orden",style=MaterialTheme.typography.headlineSmall);Text(it.status ?: "Sin estado");Text(it.customerName ?: "Cliente pendiente");Spacer(Modifier.height(18.dp))}; Text("Timeline",style=MaterialTheme.typography.titleLarge)}; items(vm.timeline){ListItem(headlineContent={Text(it.type ?: "Evento")}, supportingContent={Text(it.description ?: "")})}; item{Text("Pruebas",style=MaterialTheme.typography.titleLarge)}; items(vm.tests){ListItem(headlineContent={Text(it.name ?: "Prueba")}, supportingContent={Text(if(it.passed==true)"Aprobada" else "Pendiente")})}; vm.error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}} } } }
@Composable private fun DataScreen(title:String, loading:Boolean, error:String?, empty:Boolean, retry:()->Unit, content:@Composable ColumnScope.()->Unit) { Scaffold(topBar={TopAppBar(title={Text(title)})}){p-> Column(Modifier.padding(p).padding(16.dp)){ if(loading) LinearProgressIndicator(Modifier.fillMaxWidth()); if(empty&&!loading) Text("Sin información",Modifier.padding(vertical=24.dp)); content(); error?.let{Text(it,color=MaterialTheme.colorScheme.error,Modifier.padding(vertical=12.dp))}; OutlinedButton(retry,Modifier.fillMaxWidth()){Text("Actualizar")} } } }
