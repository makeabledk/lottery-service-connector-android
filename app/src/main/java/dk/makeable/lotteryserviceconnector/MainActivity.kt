package dk.makeable.lotteryserviceconnector

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private val connector = LotteryServiceConnector(this)
    private var serviceConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        try {
            connector.bindService(callback = {
                showLLAppOnSupportScreen()
                getSotiHwId()
                serviceConnected = true
            })
        } catch (e: Exception) {
            // Handle error
        }
    }

    override fun onResume() {
        super.onResume()

        if (serviceConnected)
            showLLAppOnSupportScreen()
    }

    fun openVarelotteritApp() {
        //open Varelotteriet app for example on button click
        startActivity(Intent().apply {
            component = ComponentName(
                "dk.makeable.varelotteriet",
                "dk.makeable.varelotteriet.MainActivity"
            )
        })
    }

    private fun showLLAppOnSupportScreen() {
        try {
            connector.showLLOnSupportScreen()
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun getSotiHwId() {
        try {
            connector.getSupportScreenSOTIHardwareId(callback = { sotiHwId ->
                // Do something with the SOTI hardware id
            })
        } catch (e: Exception) {
            // Handle error
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            connector.unbindService()
        } catch (e: Exception) {
            // Handle error
        }
    }
}