package com.example.coen390

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coen390.ui.theme.COEN390Theme

data class SessionItem(
    val mode: String,
    val date: String,
    val minTime: String,
    val meanTime: String,
    val maxTime: String,
    val totalHits: String
)

class ActivityLogScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COEN390Theme {
                ActivityLog()
            }
        }
    }
}

@Composable
fun ActivityLog(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val sessions = listOf(
        SessionItem("Random Mode", "Mar 23, 11:30 PM", "210 ms", "260 ms", "320 ms", "6"),
        SessionItem("Timed Mode", "Mar 23, 10:05 PM", "190 ms", "240 ms", "300 ms", "8"),
        SessionItem("Endless Mode", "Mar 22, 8:15 PM", "220 ms", "275 ms", "340 ms", "10")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            IconButton(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier.padding(start = 8.dp, top = 25.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(id = R.string.back_icon_description),
                    modifier = Modifier.size(24.dp),
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Activity Log",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        onClick = {
                            val intent = Intent(context, SessionDetailsScreen::class.java).apply {
                                putExtra("mode", session.mode)
                                putExtra("date", session.date)
                                putExtra("minTime", session.minTime)
                                putExtra("meanTime", session.meanTime)
                                putExtra("maxTime", session.maxTime)
                                putExtra("totalHits", session.totalHits)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SessionCard(
    session: SessionItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.mode,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.date,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}