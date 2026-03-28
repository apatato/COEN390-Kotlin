package com.example.coen390

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coen390.ui.theme.COEN390Theme

class SessionDetailsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mode = intent.getStringExtra("mode") ?: "Unknown Mode"
        val date = intent.getStringExtra("date") ?: "Unknown Date"
        val minTime = intent.getStringExtra("minTime") ?: "0 ms"
        val meanTime = intent.getStringExtra("meanTime") ?: "0 ms"
        val maxTime = intent.getStringExtra("maxTime") ?: "0 ms"
        val totalHits = intent.getStringExtra("totalHits") ?: "0"

        setContent {
            COEN390Theme {
                SessionDetails(
                    mode = mode,
                    date = date,
                    minTime = minTime,
                    meanTime = meanTime,
                    maxTime = maxTime,
                    totalHits = totalHits
                )
            }
        }
    }
}

@Composable
fun SessionDetails(
    mode: String,
    date: String,
    minTime: String,
    meanTime: String,
    maxTime: String,
    totalHits: String
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            IconButton(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier.padding(start = 8.dp, top = 25.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        },
        containerColor = Color(0xFFF7F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Session Details",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailCard("Mode", mode)
            DetailCard("Date", date)
            DetailCard("Min Reaction Time", minTime)
            DetailCard("Mean Reaction Time", meanTime)
            DetailCard("Max Reaction Time", maxTime)
            DetailCard("Total Hits", totalHits)
        }
    }
}

@Composable
fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}