package com.example.luontopeli.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.luontopeli.ml.ClassificationResult

@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is ClassificationResult.Success -> {
                    if (result.confidence > 0.8f) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                }
                is ClassificationResult.NotNature -> MaterialTheme.colorScheme.errorContainer
                is ClassificationResult.Error -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tunnistettu:",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Badge {
                            Text("${(result.confidence * 100).toInt()}%")
                        }
                    }

                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    LinearProgressIndicator(
                        progress = { result.confidence },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )

                    if (result.allLabels.isNotEmpty()) {
                        Text(
                            text = "Muut osumat: " +
                                    result.allLabels.joinToString { "${it.text} (${(it.confidence * 100).toInt()}%)" },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is ClassificationResult.NotNature -> {
                    Text(
                        text = "Ei luontokohde",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (result.allLabels.isNotEmpty()) {
                        Text(
                            text = "Kuvassa tunnistettiin: " +
                                    result.allLabels.joinToString { it.text },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is ClassificationResult.Error -> {
                    Text(
                        text = "Tunnistus epäonnistui",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}