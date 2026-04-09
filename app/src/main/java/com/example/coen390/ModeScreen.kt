package com.example.coen390

import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ModeScreen : ComponentActivity() {
    companion object {
        const val ESP32_ADDRESS = "E8:6B:EA:C9:DE:BE"
        val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private lateinit var db : SessionDatabaseHelper
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    var RTminVal = mutableStateOf("0 ms")
    var RTmaxVal = mutableStateOf("0 ms")
    var RTmeanVal = mutableStateOf("0 ms")
    var ForcemeanVal = mutableStateOf("0 N")
    var ForcemaxVal = mutableStateOf("0 N")
    var ForceminVal = mutableStateOf("0 N")
    var remainingAttempts = mutableStateOf(0)
    var mode = mutableStateOf("")
    var date = mutableStateOf("")
    var totalHits = mutableStateOf(0)
    var timer = mutableStateOf("")



    // The listener must be called inside the connection setup
    @RequiresApi(Build.VERSION_CODES.O)
    private fun listenForData() {
        Thread{
            try {
                val reader = bluetoothSocket?.inputStream?.bufferedReader()
                while (isConnected()) {
                    val line = reader?.readLine() ?: ""

                    //Handle Countdown update
                    if (line.startsWith("<RT")) {
                        runOnUiThread {
                            if (remainingAttempts.value > 0){
                                remainingAttempts.value -= 1
                                // Log it to logcat
                                android.util.Log.d("BT_DEBUG", "Count decreased to: ${remainingAttempts.value}")
                            }
                        }
                    }

                    if (line.startsWith("<SUM")) {
                        val parts = line.replace("<", "").replace(">", "").split(",")
                        if (parts.size >= 7) {
                            runOnUiThread {
                                if (remainingAttempts.value < 1) {
                                    RTminVal.value = "${parts[1]} ms"
                                    RTmaxVal.value = "${parts[2]} ms"
                                    RTmeanVal.value = "${parts[3]} ms"
                                    ForcemeanVal.value = "${parts[4]} N"
                                    ForcemaxVal.value = "${parts[5]} N"
                                    ForceminVal.value = "${parts[6]} N"

                                    var now = LocalDateTime.now()
                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                    date.value = now.format(formatter)

                                    db = SessionDatabaseHelper(this)

                                    val minHit = Hit(RTminVal.value, ForceminVal.value)
                                    val maxHit = Hit(RTmaxVal.value, ForcemaxVal.value)
                                    val meanHit = Hit(RTmeanVal.value, ForcemeanVal.value)
                                    val session = Session(maxHit, minHit, meanHit, mode.value, date.value, totalHits.value)
                                    db.insertSession(session)

                                    remainingAttempts.value = 0
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBluetoothConnection() // This will now start the listener too
        enableEdgeToEdge()
        setContent {
            COEN390Theme {
                val modeName = intent.getStringExtra("mode") ?: "Mode Name"
                mode.value = modeName
                val modeDescription = intent.getStringExtra("description") ?: "Activity"

                // We pass the .value here so Compose knows to track it
                Mode(
                    mode = modeName,
                    description = modeDescription,
                    min = RTminVal.value +", " + ForceminVal.value,
                    max = RTmaxVal.value + ", " + ForcemaxVal.value,
                    mean = RTmeanVal.value + ", " + ForcemeanVal.value,
                    remaining = remainingAttempts.value,
                    onStartClick =
                        {
                            data ->
                        remainingAttempts.value = data.filter { it.isDigit() }.toIntOrNull() ?:0
                        totalHits.value = remainingAttempts.value
                        sendData(data)
                    },
                    onStopClick = {
                        data -> sendData(data) },
                    High_Score = ForcemaxVal.value,
                    Timer = timer.value
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupBluetoothConnection() {
        Thread {
            try {
                val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                val device = bluetoothAdapter.getRemoteDevice(ESP32_ADDRESS)
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream

                // Start listening for data ONLY after connection is successful
                listenForData()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun isConnected(): Boolean = bluetoothSocket?.isConnected == true

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendData(data: String) {
        Thread {
            try {
                if (outputStream == null || !isConnected()) {
                    setupBluetoothConnectionSync()
                }
                outputStream?.write(data.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                outputStream = null
                bluetoothSocket = null
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupBluetoothConnectionSync() {
        try {
            val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(ESP32_ADDRESS)
            bluetoothSocket?.close()
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            listenForData() // Restart listener on new socket
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mode(
    modifier: Modifier = Modifier
    , mode: String
    , description: String
    , min: String
    , max: String
    , mean: String
    , High_Score: String
    , Timer: String
    , remaining: Int
    , onStartClick: (String) -> Unit
    , onStopClick: (String) -> Unit
) {

    val context = LocalContext.current
    var showPopup by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(5f)}

    if (showPopup) {
        AttemptsPopup(
            attempts = remaining,
            onDismiss = { showPopup = false },
            onStopClick = {
                onStartClick("S") //ESP32 will stop
                showPopup = false
            }
        )

        if (remaining == 0){
            showPopup = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            IconButton(
                onClick = {
                    val back = Intent(context, WelcomeScreen::class.java)
                    context.startActivity(back)
                },
                modifier = Modifier.padding(start = 8.dp, top = 50.dp)
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

            if (mode != "One Hit")
            {
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
                        onStartClick("A${sliderPosition.toInt()}")
                        showPopup = true
                              },
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
                    value = min
                )
                Spacer(modifier = Modifier.height(8.dp))
                PerformanceRow(
                    label = stringResource(id = R.string.mean_label),
                    value = mean
                )
                Spacer(modifier = Modifier.height(8.dp))
                PerformanceRow(
                    label = stringResource(id = R.string.max_label),
                    value = max
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            else
            {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(36.dp))



                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = {
                        onStartClick("A${sliderPosition.toInt()}")
                        showPopup = true
                    },
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
                    label = stringResource(id = R.string.High_Score),
                    value = High_Score
                )

                Spacer(modifier = Modifier.height(24.dp))
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
    onStopClick: () -> Unit,
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
                    onClick = { onStopClick() //send stop signal
                              },
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
        Mode(
            mode = stringResource(id = R.string.mode_name_title),
            description = stringResource(id = R.string.mode_detailed_description),
            min = "0ms, 0N",
            max = "0ms, 0N",
            mean = "0ms, 0N",
            remaining = 5,
            onStartClick = {},
            onStopClick = {},
            High_Score = "0N",
            Timer = "00:00"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AttemptsPopupPreview() {
    COEN390Theme {
        AttemptsPopup(attempts = 5, onDismiss = {}, onStopClick = {})
    }
}