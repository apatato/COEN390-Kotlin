package com.example.coen390

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            IconButton(
                onClick = {
                    val back = Intent(context, WelcomeScreen::class.java)
                    context.startActivity(back)
                },
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(id = R.string.back_icon_description),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "Activity Log",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "No activity history yet.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                color = Color(0xFF49454F)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityLogPreview() {
    COEN390Theme {
        ActivityLog()
    }
}