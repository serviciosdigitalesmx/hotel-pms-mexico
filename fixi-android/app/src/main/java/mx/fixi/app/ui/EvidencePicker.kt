package mx.fixi.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable fun EvidencePicker(onSelected: (Uri) -> Unit, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { uri -> selected=uri; onSelected(uri) } }
    OutlinedButton({picker.launch("image/*")}, modifier) { Text(if(selected==null) "Seleccionar evidencia" else "Evidencia seleccionada") }
}
