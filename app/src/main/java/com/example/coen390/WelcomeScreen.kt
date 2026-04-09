package com.example.coen390

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
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

@Suppress("DEPRECATION", "DEPRECATION")
class WelcomeScreen : ComponentActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val enableBtLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // User allowed enabling Bluetooth
                getPairedDevices()
            } else {
                // User denied enabling Bluetooth
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
                // User allowed Bluetooth permissions
                getPairedDevices()
            }
        }

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
        checkPermissionsAndProceed()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private fun checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
                return
            }
        }
        getPairedDevices()
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
                    val device: BluetoothDevice?
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: After finding a device to connect to, stop discovery with cancelDiscovery() before
        //  attempting a connection. Also, do not perform discovery while connected to a device.
        // ...
        unregisterReceiver(receiver)
    }

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
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
                    title = "One Hit",
                    description = "One big hit that will give the force of that hit",
                    onClick = { val mode = Intent(context, ModeScreen::class.java)

                                mode.putExtra("mode", "One Hit")
                                mode.putExtra("description", "This mode asks the user to hit a randomly selected pad one time as hard as they possibly can and it will record your strongest punch.")

                                context.startActivity(mode) }
                )


//                Spacer(modifier = Modifier.height(24.dp))
//
//                ModeCard(
//                    title = "Timed",
//                    description = "Practice for a set amount of time"
//                )
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
}