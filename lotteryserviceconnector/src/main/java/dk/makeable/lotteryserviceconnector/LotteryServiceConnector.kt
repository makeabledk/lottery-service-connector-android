package dk.makeable.lotteryserviceconnector

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import dk.makeable.lotteryserviceconnector.LotteryServiceConnector.Companion.EXTRA_SOTI_HARDWARE_ID
import dk.makeable.lotteryserviceconnector.LotteryServiceConnector.Companion.REQUEST_GET_SOTI_HARDWARE_ID

class LotteryServiceConnector(private val activity: AppCompatActivity) {

    companion object {
        const val REQUEST_REGISTER_CLIENT = 0
        const val REQUEST_UNREGISTER_CLIENT = 1
        const val REQUEST_GET_SOTI_HARDWARE_ID = 2
        const val REQUEST_CHANGE_SUPPORT_SCREEN_MODE = 3

        const val EXTRA_SOTI_HARDWARE_ID = "SOTI_HARDWARE_ID"
        const val EXTRA_SUPPORT_SCREEN_MODE = "SUPPORT_SCREEN_MODE"
    }

    enum class SupportScreenMode {
        Varelotteriet,
        Landbrugslotteriet
    }

    private var sotiHardwareIdCallback: SOTIHardwareIdCallback? = null
    private val internalSOTIHardwareIdCallback = SOTIHardwareIdCallback {
        sotiHardwareIdCallback?.onSOTIHardwareIdReceived(it)
    }

    private val messenger = Messenger(IncomingMessageHandler(internalSOTIHardwareIdCallback))
    private val connection = LotteryServiceConnection()

    private var bound = false
    private var boundService: Messenger? = null

    fun bindService() {
        check(!bound) { "Service already bound" }

        activity.bindService(
            Intent().apply {
                component = ComponentName(
                    "dk.makeable.varelotteriet",
                    "dk.makeable.varelotteriet.service.LotteryService"
                )
            },
            connection,
            Context.BIND_AUTO_CREATE
        )

        bound = true
    }

    fun unbindService() {
        check(bound) { "Service not bound" }

        if (boundService != null) {
            try {
                val message = Message.obtain(null, REQUEST_UNREGISTER_CLIENT)
                message.replyTo = messenger
                boundService!!.send(message)
            } catch (e: Exception) {
                Log.e("LotteryServiceConnector", "Failed to unregister client", e)
            }
        }

        sotiHardwareIdCallback = null
        activity.unbindService(connection)
        bound = false
    }

    fun changeSupportScreenMode(mode: SupportScreenMode) {
        check(boundService != null) { "Service not bound" }

        val message = Message.obtain(null, REQUEST_CHANGE_SUPPORT_SCREEN_MODE)
        message.data = bundleOf(EXTRA_SUPPORT_SCREEN_MODE to mode.name)
        boundService!!.send(message)
    }

    fun getSupportScreenSOTIHardwareId(callback: SOTIHardwareIdCallback) {
        check(boundService != null) { "Service not bound" }

        sotiHardwareIdCallback = callback
        val message = Message.obtain(null, REQUEST_GET_SOTI_HARDWARE_ID)
        message.replyTo = messenger
        boundService!!.send(message)
    }

    internal fun onSOTIHardwareIdReceived(sotiHardwareId: String) {
        sotiHardwareIdCallback?.onSOTIHardwareIdReceived(sotiHardwareId)
    }

    private inner class LotteryServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundService = Messenger(service)
            val message = Message.obtain(null, REQUEST_REGISTER_CLIENT)
            message.replyTo = messenger
            boundService!!.send(message)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
        }
    }
}

internal class IncomingMessageHandler(private val sotiHardwareIdCallback: SOTIHardwareIdCallback) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)

        when (msg.what) {
            REQUEST_GET_SOTI_HARDWARE_ID -> {
                val sotiHardwareId = msg.data.getString(EXTRA_SOTI_HARDWARE_ID)
                sotiHardwareIdCallback.onSOTIHardwareIdReceived(sotiHardwareId!!)
            }

            else -> {
                Log.e("LotteryServiceConnector", "Unknown message received: $msg")
                super.handleMessage(msg)
            }
        }
    }
}

fun interface SOTIHardwareIdCallback {
    fun onSOTIHardwareIdReceived(sotiHardwareId: String)
}
