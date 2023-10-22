package com.example.carremote.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.example.bletutorial.data.ble.printGattTable
import com.example.carremote.util.Resource
import com.exanple.carremote.com.example.carremote.data.ConnectionState
import com.exanple.carremote.com.example.carremote.data.FeedbackReceiveManager
import com.exanple.carremote.com.example.carremote.data.FeedbackResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
@Suppress("MissingPermission")
class FeedbackBLEReceiveManager @Inject constructor(
    private val bluetoothManager: BluetoothAdapter,
    private val context: Context
) : FeedbackReceiveManager {
    // Constants for BLE device identification and service/characteristic UUIDs.
    private val DEVICE_NAME = "Forester"
    private val  DEVICE_ADDRESS = "EC:DA:3B:AA:B9:AE"
    private val LOCK_SERVICE_UUID = "66b0a5cd-c1e8-4358-8e33-78045552379c"
    private val LOCK_CHARACTERISTIC_UUID = "5860118b-4b4a-475c-8f2a-4f763059ca90"
//    private val KEY = "DontStealMyCar" //add your own key here
    private val KEY = com.example.carremote.data.MY_SECRET_KEY //my secret key
    // Shared flow for emitting feedback data.
    override val data: MutableSharedFlow<Resource<FeedbackResult>> = MutableSharedFlow()

    // BLE scanner for discovering available devices.
    private val bleScanner by lazy {
        bluetoothManager.bluetoothLeScanner
    }

    // BLE scan settings configuration.
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    // BLE GATT object for interacting with the BLE device.

    private var gatt: BluetoothGatt? = null
    private var isScanning = false

    // Coroutine scope for launching asynchronous tasks.
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    // BLE scan callback handling the discovery of BLE devices.
    private val scanCallback = object: ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name == DEVICE_NAME || result.device.address == DEVICE_ADDRESS){ //alternatively can check address
                coroutineScope.launch {
                    data.emit(Resource.Loading(message = "Found device, connecting..."))
                }
                    if (isScanning) {
                        result.device.connectGatt(
                            context,
                            false,
                            gattCallback
                        ) // may need to add BluetoothDevice.TRANSPORT_LE
                        isScanning = false
//                        BluetoothDevice.TRANSPORT_LE
                        bleScanner.stopScan(this)
                    }
            }
        }
    }
    private var currentConnectionAttempt = 1
    private val MAXIMUM_CONNECTION_ATTEMPTS = 5

    // BLE GATT callback handling various GATT events like connection state change, service discovery, etc.
    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Connected to device, discovering services..."))
                    }
                    gatt.discoverServices()
                    this@FeedbackBLEReceiveManager.gatt = gatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    coroutineScope.launch {
                        data.emit(
                            Resource.Success(
                                data = FeedbackResult(
//                                    LockState.Unknown,
                                    ConnectionState.Disconnected
                                )
                            )
                        )
                    }
                }
            } else {
                gatt.close()
                currentConnectionAttempt+=1
                coroutineScope.launch { data.emit(Resource.Loading(message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS")) }
                if (currentConnectionAttempt <= MAXIMUM_CONNECTION_ATTEMPTS){
                    coroutineScope.launch {data.emit(Resource.Error("Failed to connect to device"))}
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt){
                printGattTable()
                coroutineScope.launch {
                    val result = FeedbackResult(
                            connectionState = ConnectionState.Connected
                        )
//
                            data.emit(
                                Resource.Success(
                                    data = result
                                )
                            )
                        }
//                coroutineScope.launch {
//                    data.emit(Resource.Loading(message = "Adjusting MTU space..."))
//                }
//                gatt.requestMtu(517)

            }
        }

//        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
//            val characteristic = findCharacteristic(LOCK_SERVICE_UUID, LOCK_CHARACTERISTIC_UUID)
//            if (characteristic == null) {
//                coroutineScope.launch {
//                    data.emit(Resource.Error("Could not find lock publisher"))
//                }
//                return
//            }
////            enableNotifciation(characteristic)
//
//        }

//        override fun onCharacteristicChanged(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic
//        ) {
//            with(characteristic) {
//                when(uuid){
//                    UUID.fromString(LOCK_CHARACTERISTIC_UUID)-> {
//                        //XX XX XX XX
//                        //value[0].toInt()?
//                        val lockState = when (value[0].toInt()) {
//                            0 -> LockState.Locked
//                            1 -> LockState.Unlocked
//                            else -> LockState.Unknown
//                        }
//                        val lockStateResult = FeedbackResult(
//                            lockState = lockState,
//                            connectionState = ConnectionState.Connected
//                        )
//                        coroutineScope.launch {
//                            data.emit(
//                                Resource.Success(
//                                    data = lockStateResult
//                                    )
//                            )
//                        }
//                }
//                else -> Unit
//                }
//            }
//        }
//        override fun onCharacteristicRead(
//            gatt: BluetoothGatt,
//            characteristic: BluetoothGattCharacteristic,
//            status: Int
//        ) {
//            super.onCharacteristicRead(gatt, characteristic, status)
//        }
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }
    }


//    private fun example() {
//        gatt?.getService(UUID.fromString(""))?.getCharacteristic(UUID.fromString(""))?.let { characteristic ->
//            characteristic.value = byteArrayOf(0x01)
//            gatt?.writeCharacteristic(characteristic)
//        }
//    }
//    private fun enableNotifciation(characteristic: BluetoothGattCharacteristic) {
//        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID) //TODO: may need to edit ccdd_uuid
//        val payload = when {
//            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
//            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//            else ->
//                return
//        }
//        characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
//            if (gatt?.setCharacteristicNotification(characteristic, true) == false) {
//                Log.d("BLEReceiveManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
//                return
//            }
//            writeDescripton(cccdDescriptor, payload)
//
//        }
//    }
//    private fun writeDescripton(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
//        gatt?.let { gatt ->
//            descriptor.value = payload
//            gatt.writeDescriptor(descriptor)
//            }?: error("Not connected to a BLE device")
//    }
    private fun findCharacteristic(serviceUUID: String, characteristics: String): BluetoothGattCharacteristic? {
        return gatt?.services?.find {service ->
            service.uuid.toString() == serviceUUID
        }?.characteristics?.find { characteristic ->
            characteristic.uuid.toString() == characteristics
        }
    }


    override fun startReceiving() {
        coroutineScope.launch {
            data.emit(Resource.Loading(message =  "Scanning BLE devices..."))
        }
        isScanning = true
        bleScanner.startScan(null, scanSettings, scanCallback)
    }
    override fun reconnect() {
        gatt?.connect()
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun closeConnection() {
        bleScanner.stopScan(scanCallback)
//        val characteristic = findCharacteristic(LOCK_SERVICE_UUID, LOCK_CHARACTERISTIC_UUID)
//        if (characteristic != null) {
//            disconnectCharacteristic(characteristic)
//        }
        gatt?.close()
    }
//    private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
//        val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
//        characteristic.getDescriptor(cccdUuid)?.let { cccdDescruptor ->
//            if (gatt?.setCharacteristicNotification(characteristic, false) == false){
//                Log.d("BLEReceiveManager", "setCharacteristicNotification failed for ${characteristic.uuid}")
//                return
//            }
//            writeDescripton(cccdDescruptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
//        }
//    }
    override fun writeLockUnlockValue(value: ByteArray) {
    Log.d("BLEReceiveManager", "writeLockUnlockValue: ${value.contentToString()}")
        val characteristic = gatt?.getService(UUID.fromString(LOCK_SERVICE_UUID))?.getCharacteristic(UUID.fromString(LOCK_CHARACTERISTIC_UUID))
        gatt?.writeCharacteristic(characteristic?.apply {
            this.value = value
        })
    }

}