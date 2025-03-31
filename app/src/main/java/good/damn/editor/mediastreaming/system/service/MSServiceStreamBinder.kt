package good.damn.editor.mediastreaming.system.service

import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import good.damn.media.streaming.audio.stream.MSStreamAudioInput
import good.damn.media.streaming.camera.MSManagerCamera
import good.damn.media.streaming.camera.MSStreamCameraInput
import good.damn.media.streaming.camera.models.MSCameraModelID
import good.damn.media.streaming.extensions.toInetAddress
import good.damn.media.streaming.network.server.udp.MSReceiverCameraFrameRestore
import good.damn.media.streaming.network.server.udp.MSServerUDP
import java.net.InetAddress

class MSServiceStreamBinder: Binder()