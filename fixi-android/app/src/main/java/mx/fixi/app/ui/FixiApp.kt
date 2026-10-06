package mx.fixi.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.fixi.app.data.ServiceOrder

@Composable fun FixiApp(vm: FixiViewModel = hiltViewModel()) {
    MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF155E75))) {
        Surface(Modifier.fillMaxSize()) { if (vm.loggedIn) Home(vm) else Login(vm) }
    }
}

@Composable private fun Login(vm: FixiViewModel) {
    var user by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Fixi", style = MaterialTheme.typography.displaySmall); Text("Operación de taller", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp)); OutlinedTextField(user, { user = it }, Modifier.fillMaxWidth(), label={Text("Correo o usuario")})
        Spacer(Modifier.height(12.dp)); OutlinedTextField(pass, { pass = it }, Modifier.fillMaxWidth(), label={Text("Contraseña")})
        Spacer(Modifier.height(16.dp)); Button({ vm.login(user, pass) }, Modifier.fillMaxWidth(), enabled=!vm.busy) { Text(if(vm.busy) "Iniciando..." else "Iniciar sesión") }
        vm.error?.let { Text(it, color=MaterialTheme.colorScheme.error, modifier=Modifier.padding(top=12.dp)) }
    }
}

@Composable private fun Home(vm: FixiViewModel) {
    Scaffold(topBar={ TopAppBar(title={Text("Fixi")}, actions={ TextButton({vm.logout()}){Text("Salir")} })}) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp)) {
            item { Text("Dashboard", style=MaterialTheme.typography.headlineMedium); Text("Tenant: ${vm.user?.tenantId ?: "actual"}"); Spacer(Modifier.height(20.dp)); Text("Órdenes de servicio", style=MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)) }
            items(vm.orders) { OrderCard(it) }
            if (vm.orders.isEmpty() && !vm.busy) item { Text("No hay órdenes disponibles", modifier=Modifier.padding(vertical=24.dp)) }
            item { Button({vm.loadOrders()}, enabled=!vm.busy, modifier=Modifier.fillMaxWidth()) { Text("Actualizar") } }
            vm.error?.let { item { Text(it, color=MaterialTheme.colorScheme.error) } }
        }
    }
}
@Composable private fun OrderCard(order: ServiceOrder) { Card(Modifier.fillMaxWidth().padding(vertical=5.dp)) { Column(Modifier.padding(16.dp)){ Text(order.orderNumber ?: order.id ?: "Orden", style=MaterialTheme.typography.titleMedium); Text(order.status ?: "Sin estado"); Text(order.customerName ?: "Cliente pendiente"); Text(order.deviceDescription ?: "Equipo pendiente") } } }
