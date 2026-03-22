package com.example.coen390

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coen390.ui.theme.COEN390Theme
import java.io.IOException

class WelcomeScreen : ComponentActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val myUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Service UUID of ESP32
    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // User allowed enabling Bluetooth
                getPairedDevices()
            } else {
                // User denied enabling Bluetooth
            }
        }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            COEN390Theme {
                Welcome()
            }
        }

        // Get the Bluetooth adapter
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        // TODO: Query paired devices when it is actually needed (e.g., button click) instead of
        //  in onCreate() every time the app opens.
        getPairedDevices()

        // Register the receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // Start discovery
        bluetoothAdapter?.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice object and
                    // its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    device?.let {
                        val deviceName = it.name
                        val deviceHardwareAddress = it.address // MAC address
                    }
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter?.cancelDiscovery()
        unregisterReceiver(receiver)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getPairedDevices() {
        // 1. Check Bluetooth
        // 2. If OFF -> ask user to enable it
        // 3. If ON -> query paired devices

        if (bluetoothAdapter?.isEnabled == false) {
            // Display a dialog requesting user permission to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            ConnectThread(device).start() // Connect to remote device
            val deviceName = device.name
            val deviceHardwareAccess = device.address // MAC address
        }
    }

    @Composable
    fun Welcome(modifier: Modifier = Modifier) {
        val context = LocalContext.current

        Scaffold(
            modifier = modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val activityLogIntent = Intent(context, ActivityLogScreen::class.java)
                        context.startActivity(activityLogIntent)
                    },
                    containerColor = Color(0xFF2C2C2C),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .padding(bottom = 16.dp, end = 8.dp)
                        .size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_list),
                        contentDescription = stringResource(id = R.string.list_icon_description),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                Text(
                    text = stringResource(id = R.string.welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    color = Color(0xFF1C1B1F)
                )

                Spacer(modifier = Modifier.height(80.dp))

                ModeCard(
                    title = "Random",
                    description = "Practice with a randomly generated pattern",
                    onClick = {
                        val mode = Intent(context, ModeScreen::class.java)

                        mode.putExtra("mode", "Random")
                        mode.putExtra("description", "This mode will generate a random pattern for you to strike. You may specify the duration of the activity, etc.")


                        context.startActivity(mode)
                    }

                )

                Spacer(modifier = Modifier.height(24.dp))

                ModeCard(
                    title = stringResource(id = R.string.mode_title),
                    description = stringResource(id = R.string.mode_description)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ModeCard(
                    title = stringResource(id = R.string.mode_title),
                    description = stringResource(id = R.string.mode_description)
                )
            }
        }
    }

    @Composable
    fun ModeCard(
        title: String,
        description: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFF1C1B1F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF49454F),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun WelcomePreview() {
        COEN390Theme {
            Welcome()
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        // thread that connects the app to the ESP32 over Bluetooth

        // Open a Bluetooth client socket that allows the app to connect to the ESP32.
        private val mmSocket: BluetoothSocket? by Lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(myUuid)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the ESP32 through the socket.
                // This call blocks until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with the
                // connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            // Keep listening to the InputStream until an exception occurs.
            while(true) {
                // Read from InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = hander.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred while sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

}