package com.togawa.foregroundserviceblecentral

import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.concurrent.schedule

class BleCentralService: Service() {

    companion object {
        const val CHANNEL_ID = "bbb123"
        const val CHANNEL_TITLE = "セントラル中"
    }

    private lateinit var notificationManager: NotificationManagerCompat

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()

    private lateinit var wakeLock: PowerManager.WakeLock

    private val SCAN_PERIOD: Long = 5000    // スキャン時間

    override fun onCreate() {
        Log.d("Service", "サービス開始するよ")

        super.onCreate()

        // WakeLockを取得
//        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        wakeLock = powerManager.newWakeLock(
//            PowerManager.PARTIAL_WAKE_LOCK,
//            "MyApp:MyWakeLockTag"
//        )
//        wakeLock.acquire()

        notificationManager = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName("滞在ウォッチ動作中")
            .build()
        notificationManager.createNotificationChannel(channel)

        //manager.notify(CHANNEL_ID, 新しいnotification)

        // アクティビティを起動するIntentを作成
        val openIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, channel.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(CHANNEL_TITLE)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()

        // スキャンのセッティング
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val mUuid = UUID.fromString("8ebc2114-4abd-ba0d-b7c6-ff0a00200037") // ここにサービスのUUIDを設定

        val scanFilters = mutableListOf<ScanFilter>()
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(mUuid))
            .build()
        scanFilters.add(filter)

        bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)



//        // スキャンする
//        val timer = Timer()
//        // val delayMillis: Long = 1 * 60 * 1000 // 実行間隔(1分をミリ秒に変換)
//        val delayMillis: Long = 10 * 1000
//        val timerTask = object : TimerTask() {
//            override fun run() {
//                // ここにscanLeDevice()メソッドの呼び出しを追加
//                //Log.d("Service", "1分ごとの処理")
//                Log.d("Service", "10秒ごとの処理")
//                scanLeDevice()
//            }
//        }
//        timer.schedule(timerTask, 0, delayMillis)

//        // スキャンする（ライブラリ使用）
////        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
////        ScanSettings settings = new ScanSettings.Builder()
////            .setLegacy(false)
////            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
////            .setReportDelay(5000)
////            .setUseHardwareBatchingIfSupported(true)
////            .build();
////        List<ScanFilter> filters = new ArrayList<>();
////        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
////        scanner.startScan(filters, settings, scanCallback);
//
//        val scanner = BluetoothLeScannerCompat.getScanner()
//        val settings = ScanSettings.Builder()
//            .setLegacy(false)
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
//            .setReportDelay(5000)
//            .setUseHardwareBatchingIfSupported(true)
//            .build()
//
//        val mUuid = UUID.fromString("8ebc2114-4abd-ba0d-b7c6-ff0a00200037") // ここにサービスのUUIDを設定
//        val filters = mutableListOf<no.nordicsemi.android.support.v18.scanner.ScanFilter>()
////        val scanFilter = no.nordicsemi.android.support.v18.scanner.ScanFilter.Builder()
////            .setServiceUuid(ParcelUuid(mUuid))
////            .build()
////        filters.add(scanFilter)
//
//        scanner.startScan(filters, settings, scanCallback)


        //5. 通知の表示
        startForeground(2222, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

//    private fun scanLeDevice() {
//        bluetoothLeScanner?.let { scanner ->
//            if (!scanning) { // Stops scanning after a pre-defined scan period.
//                handler.postDelayed({
//                    scanning = false
//                    scanner.stopScan(leScanCallback)
//                    println("stopScan")
//                }, SCAN_PERIOD)
//                scanning = true
//                scanner.startScan(leScanCallback)
//                println("startScan")
//            } else {
//                scanning = false
//                scanner.stopScan(leScanCallback)
//                println("stopScans")
//            }
//        }
//    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            //Log.d("Service", "コールバック")
            //nameをログで出力する。nullだった場合No Name
            //Log.d("scanResult", result.device.toString() ?: "No Name")
            val uuids = result.scanRecord?.serviceUuids
            if(uuids != null){
                for(uuid in uuids) {
                    val uuidString = uuid.uuid.toString()
                    Log.d("ServiceScanUuid", "$uuidString, RSSI: ${result.rssi}")
                }
            }
        }
    }

//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            val uuids = result.scanRecord?.serviceUuids
//            if (uuids != null) {
//                for (uuid in uuids) {
//                    val uuidString = uuid.uuid.toString()
//                    // BluetoothデバイスのUUIDをログに出力
//                    // また、必要に応じてRSSIなども取得可能
//                    Log.d("ServiceScanUuid", "$uuidString, RSSI: ${result.rssi}")
//                }
//            }
//        }
}