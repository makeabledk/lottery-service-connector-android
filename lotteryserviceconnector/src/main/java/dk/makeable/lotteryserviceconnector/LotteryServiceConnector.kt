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
        const val REQUEST_GET_SOTI_HARDWARE_ID = 0
        const val REQUEST_CHANGE_SUPPORT_SCREEN_MODE = 1

        const val EXTRA_SOTI_HARDWARE_ID = "SOTI_HARDWARE_ID"
        const val EXTRA_SUPPORT_SCREEN_MODE = "SUPPORT_SCREEN_MODE"
    }

    private var serviceConnectedCallback: ServiceConnectedCallback? = null
    private var sotiHardwareIdCallback: SOTIHardwareIdCallback? = null
    private val internalSOTIHardwareIdCallback = SOTIHardwareIdCallback {
        sotiHardwareIdCallback?.onSOTIHardwareIdReceived(it)
    }

    private val messenger = Messenger(IncomingMessageHandler(internalSOTIHardwareIdCallback))
    private val connection = LotteryServiceConnection()

    private var bound = false
    private var connectedService: Messenger? = null

    @Throws(ServiceAlreadyBound::class, FailedToBindService::class)
    fun bindService(callback: ServiceConnectedCallback) {
        if (bound)
            throw ServiceAlreadyBound()

        try {
            serviceConnectedCallback = callback
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
        } catch (e: Exception) {
            throw FailedToBindService(e)
        }
    }

    @Throws(ServiceNotBound::class, FailedToUnBindService::class)
    fun unbindService() {
        if (!bound)
            throw ServiceNotBound()

        try {
            sotiHardwareIdCallback = null
            serviceConnectedCallback = null
            activity.unbindService(connection)
        } catch (e: Exception) {
            throw FailedToUnBindService(e)
        } finally {
            bound = false
        }
    }

    private fun changeSupportScreenMode(mode: String) {
        if (connectedService == null)
            throw ServiceNotConnected()

        try {
            val message = Message.obtain(null, REQUEST_CHANGE_SUPPORT_SCREEN_MODE)
            message.data = bundleOf(EXTRA_SUPPORT_SCREEN_MODE to mode)
            connectedService!!.send(message)
        } catch (e: Exception) {
            throw FailedToSendChangeModeRequestToService(e)
        }
    }

    @Throws(ServiceNotConnected::class, FailedToSendChangeModeRequestToService::class)
    fun showLLOnSupportScreen() {
        changeSupportScreenMode("LL")
    }

    @Throws(ServiceNotConnected::class, FailedToSendChangeModeRequestToService::class)
    fun showVLOnSupportScreen() {
        changeSupportScreenMode("VL")
    }

    @Throws(ServiceNotConnected::class, FailedToSendSOTIHardwareIDRequestToService::class)
    fun getSupportScreenSOTIHardwareId(callback: SOTIHardwareIdCallback) {
        if (connectedService == null)
            throw ServiceNotConnected()

        try {
            sotiHardwareIdCallback = callback
            val message = Message.obtain(null, REQUEST_GET_SOTI_HARDWARE_ID)
            message.replyTo = messenger
            connectedService!!.send(message)
        } catch (e: Exception) {
            throw FailedToSendSOTIHardwareIDRequestToService(e)
        }
    }

    private inner class LotteryServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            connectedService = Messenger(service)
            serviceConnectedCallback?.onServiceConnected()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectedService = null
            serviceConnectedCallback = null
        }
    }
}

internal class IncomingMessageHandler(private val sotiHardwareIdCallback: SOTIHardwareIdCallback) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            REQUEST_GET_SOTI_HARDWARE_ID -> {
                val sotiHardwareId = msg.data.getString(EXTRA_SOTI_HARDWARE_ID)
                if (sotiHardwareId == null)
                    Log.e("LotteryServiceConnector", "SOTI hardware id was null when receiving repose from service")
                else
                    sotiHardwareIdCallback.onSOTIHardwareIdReceived(sotiHardwareId)
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

fun interface ServiceConnectedCallback {
    fun onServiceConnected()
}