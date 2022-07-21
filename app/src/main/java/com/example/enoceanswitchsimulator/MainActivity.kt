package com.example.enoceanswitchsimulator

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.enoceanswitchsimulator.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var buttons: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            buttons = listOf(firstButton, secondButton, thirdButton, fourthButton)
        }

        buttons.forEach { it as MaterialButton
            handleButton(it, { sendByMotionDown(it.text.toString()) }, {sendByMotionUp(it.text.toString()) })
        }

        handleGenerateQrCodeButton()
        buildAdvertisingSettings()
    }

    private fun buildAdvertisingSettings() {
        checkBluetoothPermission()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter.isMultipleAdvertisementSupported.not()) {
            Toast.makeText(this, "Multiple advertisement not supported", Toast.LENGTH_LONG).show()
            buttons.forEach {
                it.isEnabled = false
            }
        }

        val advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val pUuid = ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)))
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(pUuid)
            .addServiceData(pUuid, "Data".toByteArray(Charset.forName("UTF-8")))
            .build()

        val advertisingCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising onStartFailure: $errorCode")
                super.onStartFailure(errorCode)
            }
        }

        advertiser.startAdvertising(settings, data, advertisingCallback)
    }

    private fun handleGenerateQrCodeButton() {
        binding.generateQrCodeButton.setOnClickListener {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }

            val point = Point()
            display?.getSize(point)

            val writer = QRCodeWriter()

            try {
                val bitMatrix = writer.encode("tutaj link z info", BarcodeFormat.QR_CODE, 512, 512)
                val widthMatrix = bitMatrix.width
                val heightMatrix = bitMatrix.height
                val bmp = Bitmap.createBitmap(widthMatrix, heightMatrix, Bitmap.Config.RGB_565)

                for (x in 0 until widthMatrix) {
                    for (y in 0 until heightMatrix) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }
                binding.qrCodeImage.setImageBitmap(bmp)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    private fun sendByMotionDown(buttonName: String) {
        println("$buttonName: poszedl klik w dół")
    }

    private fun sendByMotionUp(buttonName: String) {
        println("$buttonName: poszedł klik w górę")
    }

    private fun handleButton(button: View, actionDown: () -> Unit, actionUp: () -> Unit) {
        button.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.performClick()
                    actionDown()
                }
                MotionEvent.ACTION_UP -> {
                    actionUp()
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private val requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // granted
            } else {
                // deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test", "${it.key} = ${it.value}")
            }
        }
}
