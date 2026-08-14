package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailBottomSheet(
    vehicle: Vehicle,
    onDismiss: () -> Unit = {},
    onClose: (() -> Unit)? = null,
    onToggleLock: (() -> Unit)? = null,
    onOpenTripHistory: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val primaryRed = Color(0xFFDC2626)
    val dismissAction = onClose ?: onDismiss

    ModalBottomSheet(
        onDismissRequest = dismissAction,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = dismissAction) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color(0xFF1E293B))
                }
                Text(
                    text = "${vehicle.name}(NDJESSA)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                TextButton(onClick = {
                    Toast.makeText(context, "Mode Édition ouvert", Toast.LENGTH_SHORT).show()
                }) {
                    Text(
                        text = "Éditer",
                        color = primaryRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(4.dp))

            // Detail rows
            DetailRow(label = "Icône", valueWidget = {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = primaryRed, modifier = Modifier.size(28.dp))
            })
            DetailRow(label = "IMEI de l'appareil", value = "868166057971103")
            DetailRow(label = "numéro de téléphone", value = "650872032")
            DetailRow(label = "Numéro de plaque d'immatriculation", value = vehicle.licensePlate)
            DetailRow(label = "Couleur de la plaque d'immatriculation", value = "Yellow")
            DetailRow(label = "Groupe", value = "NdJessa car")
            DetailRow(label = "type de produit", value = "303F/G")
            DetailRow(label = "Numéro de version", value = "303FG-2_V4.7_231010")
            DetailRow(label = "Date d'enregistrement", value = "2024-04-19 17:15:32")
            DetailRow(label = "Durée de validité", value = "2029-04-18 17:15:32")
            DetailRow(
                label = "Informations d'installation",
                valueWidget = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {
                    Toast.makeText(context, "Informations d'installation", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String? = null,
    valueWidget: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Medium
            )
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Normal
                )
            } else if (valueWidget != null) {
                valueWidget()
            }
        }
        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
    }
}
