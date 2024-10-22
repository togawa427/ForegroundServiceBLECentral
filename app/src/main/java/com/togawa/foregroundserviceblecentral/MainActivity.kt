package com.togawa.foregroundserviceblecentral

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.togawa.foregroundserviceblecentral.ui.theme.ForegroundServiceBLECentralTheme
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 1

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    private val REQUEST_ENABLE_BT = 1
    private val SCAN_PERIOD: Long = 30000

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

//        private BluetoothAdapter mBluetoothAdapter;
//
//        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null) {
//            // Bluetooth 利用不可の端末.
//        }
        // 要求する権限
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )

        // パーミッションが許可されていない時の処理
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            // パーミッションが許可されていない時の処理
            //Log.d("debug", "権限欲しいよ")
            EasyPermissions.requestPermissions(this, "権限の説明", PERMISSION_REQUEST_CODE, *permissions)
        }else{
            // パーミッションが許可されている時の処理
            //Log.d("debug", "権限許可されているよ")
        }

        super.onCreate(savedInstanceState)
        setContent {
            ForegroundServiceBLECentralTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        Greeting("Android")

                        Button(onClick = {
                            Log.d("Activity", "スキャン開始ボタン押された")
                            scanLeDevice()
                            Log.d("Activity", "ボタンの処理終了")
                        }) {
                            Text(text = "スキャン開始停止")
                        }

                        Button(onClick = {
                            Log.d("Activity", "サービス開始ボタン押された")
                            val serviceIntent = Intent(application, BleCentralService::class.java)
                            startForegroundService(serviceIntent)
                            Log.d("Activity", "ボタンの処理終了")
                        }) {
                            Text("セントラルサービス開始")
                        }

                        Button(onClick = {
                            val serviceIntent = Intent(application, BleCentralService::class.java)
                            stopService(serviceIntent)
                        }) {
                            Text("サービス終了")
                        }

                    }
                }
            }
        }
    }

    private fun scanLeDevice() {
        bluetoothLeScanner?.let { scanner ->
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    scanner.stopScan(leScanCallback)
                    println("stopScan")
                }, SCAN_PERIOD)
                scanning = true
                scanner.startScan(leScanCallback)
                println("startScan")
            } else {
                scanning = false
                scanner.stopScan(leScanCallback)
                println("stopScan")
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //Log.d("MainActivity", "コールバック")
            super.onScanResult(callbackType, result)

            //nameをログで出力する。nullだった場合No Name
            //Log.d("scanResult", result.device.toString() ?: "No Name")
            val uuids = result.scanRecord?.serviceUuids
            val receiveRssi = result.rssi
            if(uuids != null){
                for(uuid in uuids) {
                    val uuidString = uuid.uuid.toString()
                    val rssi =
                    Log.d("scanResult", "$uuidString")
                    Log.d("scanResult", "$receiveRssi")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ForegroundServiceBLECentralTheme {
        Greeting("Android")
    }
}