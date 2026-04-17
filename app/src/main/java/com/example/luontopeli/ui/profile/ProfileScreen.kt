package com.example.luontopeli.ui.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.luontopeli.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {

    val currentUser by viewModel.currentUser.collectAsState()
    val totalSpots by viewModel.totalSpots.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (currentUser != null) {

            Text(
                text = if (currentUser!!.isAnonymous) "Anonyymi käyttäjä"
                else currentUser!!.email ?: "Käyttäjä",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "ID: ${currentUser!!.uid.take(8)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Tilastot", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = "$totalSpots", label = "Löytöä")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = {
                Log.e("PROFILE_TEST", "Logout button clicked")
                viewModel.signOut()
            }) {
                Text("Kirjaudu ulos")
            }

        } else {

            Text(
                text = "Et ole kirjautunut",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.e("PROFILE_TEST", "BUTTON CLICKED")
                    viewModel.signInAnonymously()
                }
            ) {
                Text("Kirjaudu anonyymisti")
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}