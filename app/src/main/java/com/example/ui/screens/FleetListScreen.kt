package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Vehicle
import com.example.model.VehicleStatus
import com.example.ui.components.VehicleCard
import com.example.ui.theme.TharaRed
import com.example.ui.theme.TharaRedBorder
import com.example.ui.theme.TharaTextMuted
import com.example.ui.theme.TharaTextSecondary

@Composable
fun FleetListScreen(
    vehicles: List<Vehicle>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSelectVehicle: (Vehicle) -> Unit,
    onToggleLock: (String) -> Unit,
    onOpenAddVehicleDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("fleet_list_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search Fleet...", color = TharaTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TharaTextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fleet_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TharaRedBorder,
                    unfocusedBorderColor = TharaRedBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicles List
            if (vehicles.isEmpty()) {
                FleetEmptyStateComponent(
                    searchQuery = searchQuery,
                    onClearSearch = { onSearchQueryChanged("") },
                    onAddVehicle = onOpenAddVehicleDialog,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onClick = { onSelectVehicle(vehicle) },
                            onToggleLock = { onToggleLock(vehicle.id) }
                        )
                    }
                }
            }
        }

        // FAB to Add Vehicle
        FloatingActionButton(
            onClick = onOpenAddVehicleDialog,
            containerColor = TharaRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_vehicle_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FleetEmptyStateComponent(
    searchQuery: String,
    onClearSearch: () -> Unit,
    onAddVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("fleet_empty_state"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(TharaRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (searchQuery.isNotBlank()) Icons.Default.SearchOff else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = TharaRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (searchQuery.isNotBlank()) "Aucun résultat trouvé" else "Flotte vide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (searchQuery.isNotBlank()) {
                        "Aucun véhicule ne correspond à la recherche « $searchQuery ». Vérifiez l'orthographe ou effacez le filtre."
                    } else {
                        "Aucun véhicule n'est enregistré dans votre flotte. Ajoutez votre premier véhicule pour commencer le suivi GPS."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TharaTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (searchQuery.isNotBlank()) {
                    OutlinedButton(
                        onClick = onClearSearch,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Effacer la recherche")
                    }
                } else {
                    Button(
                        onClick = onAddVehicle,
                        colors = ButtonDefaults.buttonColors(containerColor = TharaRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("empty_add_vehicle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajouter un véhicule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
