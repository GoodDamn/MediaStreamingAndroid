package good.damn.editor.mediastreaming.system.observer

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresApi
import good.damn.editor.mediastreaming.system.interfaces.MSListenerOnGetHotspotHost

@RequiresApi(30)
class MSObserverHotspotApi30(
    context: Context
): MSObserverBase<
    MSListenerOnGetHotspotHost
    >(context) {

    private val mConnectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    private val mNetworkRequest = NetworkRequest.Builder()
        .addTransportType(
            NetworkCapabilities.TRANSPORT_WIFI
        ).build()

    private val mCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(
            network: Network,
            link: LinkProperties
        ) {
            val dhcp = link.linkAddresses
            delegate?.onGetHotspotIP(
                dhcp.toString()
            )
            super.onLinkPropertiesChanged(network, link)
        }
    }

    override fun start() {
        mConnectivityManager.requestNetwork(
            mNetworkRequest,
            mCallback
        )

        mConnectivityManager.registerNetworkCallback(
            mNetworkRequest,
            mCallback
        )
    }
}