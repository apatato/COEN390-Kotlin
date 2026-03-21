package com.example.coen390

import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.coen390.ui.theme.COEN390Theme
import java.io.OutputStream
import java.util.UUID

class ModeScreen : ComponentActivity() {
    companion object {
        const val ESP32_ADDRESS = "E8:6B:EA:C9:DE:BE" // Change to yours!
        val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // Global variables so all buttons can access them
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetoothConnection()
        enableEdgeToEdge()
        setContent {
            COEN390Theme {
                val modeName = intent.getStringExtra("mode") ?: "Mode Name"
                val modeDescription = intent.getStringExtra("description") ?: stringResource(id = R.string.mode_detailed_description)
                Mode(mode = modeName, description = modeDescription, onStartClick = {sendData("A")})
            }
        }
    }
    private fun setupBluetoothConnection(){
        Thread {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                val device = bluetoothAdapter.getRemoteDevice(ESP32_ADDRESS)
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
            } catch(e: Exception){
                e.printStackTrace()
            }
        }.start()
    }

    private fun sendData(data: String){
        Thread{
            try{
                outputStream?.write(data.toByteArray())
            }catch (e: Exception) {e.printStackTrace()}
        }.start()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mode(modifier: Modifier = Modifier, mode: String, description: String, onStartClick: () -> Unit) {
    val context = LocalContext.current
    var showPopup by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(5f)}

    if (showPopup) {
        AttemptsPopup(
            attempts = sliderPosition.toInt(),
            onDismiss = { showPopup = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            IconButton(
                onClick = {
                    val back = Intent(context, WelcomeScreen::class.java)
                    context.startActivity(back)
                },
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
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mode,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = Color(0xFF1C1B1F),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            Column(modifier = modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Number of hits: ${sliderPosition.toInt()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = "5-100",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = sliderPosition,
                        valueRange = 5f..100f,
                        steps = 95,
                        onValueChange = { sliderPosition = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Transparent,
                            activeTrackColor = Color(0xFFE0E0E0),
                            inactiveTrackColor = Color(0xFFE0E0E0)
                        ),
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2C))
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    onStartClick()
                    showPopup = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2C)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.start_activity),
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.overall_performance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
                color = Color.Black,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            PerformanceRow(
                label = stringResource(id = R.string.min_label),
                value = stringResource(id = R.string.value_zero)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PerformanceRow(
                label = stringResource(id = R.string.mean_label),
                value = stringResource(id = R.string.value_zero)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PerformanceRow(
                label = stringResource(id = R.string.max_label),
                value = stringResource(id = R.string.value_zero)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterControl(
    label: String,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF1C1B1F)
            )
            Text(
                text = stringResource(id = R.string.range_1_100),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF49454F),
                fontSize = 14.sp
            )
        }
        Box(
            modifier = Modifier.height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = Color(0xFFE0E0E0),
                    inactiveTrackColor = Color(0xFFE0E0E0)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C))
                    )
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(12.dp).clip(CircleShape)
                        .background(Color(0xFF2C2C2C))
                )
                Box(
                    modifier = Modifier.size(12.dp).clip(CircleShape)
                        .background(Color(0xFF2C2C2C))
                )
            }
        }
    }
}

@Composable
fun PerformanceRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AttemptsPopup(
    attempts: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Activity Running",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = attempts.toString(),
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 110.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2C2C)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.end_activity),
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModePreview() {
    COEN390Theme {
        Mode(mode = stringResource(id = R.string.mode_name_title), description = stringResource(id = R.string.mode_detailed_description), onStartClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AttemptsPopupPreview() {
    COEN390Theme {
        AttemptsPopup(attempts = 5, onDismiss = {})
    }
}