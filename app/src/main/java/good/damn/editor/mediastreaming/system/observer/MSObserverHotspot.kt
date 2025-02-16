package good.damn.editor.mediastreaming.system.observer

import android.content.Context
import android.net.wifi.WifiManager
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost
import good.damn.media.streaming.extensions.setIntegerOnPosition
import java.nio.ByteOrder

@Deprecated("dhcpInfo of WifiManager class is deprecated")
class MSObserverHotspot(
    context: Context
): MSObserverBase<
    MSListenerOnGetHotspotHost
    >(context) {

    companion object {
        private const val TAG = "HotspotService"
    }

    private val mWifiManager = context.applicationContext.getSystemService(
        Context.WIFI_SERVICE
    ) as WifiManager

    override fun start() {

        val dhcp = mWifiManager.dhcpInfo
        val ipDhcp = dhcp.gateway

        if (ipDhcp == 0) {
            delegate?.onGetHotspotIP(
                "0.0.0.0"
            )
            return
        }

        val ip = ByteArray(4)

        ip.setIntegerOnPosition(
            if (ByteOrder.nativeOrder().equals(
                ByteOrder.LITTLE_ENDIAN
            )) Integer.reverseBytes(
                ipDhcp
            ) else ipDhcp,
            pos = 0
        )

        delegate?.onGetHotspotIP(
            "${ip[0]}.${ip[1]}.${ip[2]}.${ip[3]}"
        )
    }

}