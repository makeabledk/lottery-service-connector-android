package dk.makeable.lotteryserviceconnector

sealed class LotteryServiceConnectorException(override val message: String? = null, override val cause: Throwable? = null) : Exception(message, cause)

class ServiceNotBound() : LotteryServiceConnectorException("Service not bound. You must call bindService() before calling this method")
class ServiceAlreadyBound() : LotteryServiceConnectorException("Service already bound. You must call unbindService() before calling this method")

class FailedToBindService(cause: Throwable) : LotteryServiceConnectorException(cause = cause)
class FailedToUnBindService(cause: Throwable) : LotteryServiceConnectorException(cause = cause)

class ServiceNotConnected() : LotteryServiceConnectorException("Service not connected. This can happen is you call this method too soon after calling bindService(), as it takes some time to launch the service and connect to it")

class FailedToSendSOTIHardwareIDRequestToService(cause: Throwable) : LotteryServiceConnectorException("Failed to send request to LotteryService to get the SOTI hardware ID", cause)
class FailedToSendChangeModeRequestToService(cause: Throwable) : LotteryServiceConnectorException("Failed to send request to LotteryService to change support screen mode", cause)
