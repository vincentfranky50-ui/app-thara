package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Red500

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, licensePlate: String, imei: String, driverName: String, driverPhone: String) -> Unit,
    errorMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_vehicle_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Nouveau Véhicule Flotte",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Enregistrement du boîtier GPS IMEI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Red500,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du véhicule (ex: Toyota Hilux 4x4)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("Plaque d'immatriculation (ex: DK-4920-AB)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_license_plate"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = imei,
                    onValueChange = { imei = it },
                    label = { Text("Code IMEI Boîtier GPS (15 chiffres)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_imei"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Nom du Chauffeur Assigné") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_driver_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = driverPhone,
                    onValueChange = { driverPhone = it },
                    label = { Text("Téléphone du Chauffeur (+221...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_driver_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_add_vehicle_button")
                    ) {
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSubmit(name, licensePlate, imei, driverName, driverPhone)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                        modifier = Modifier.testTag("submit_add_vehicle_button")
                    ) {
                        Text("Ajouter", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
