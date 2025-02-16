package good.damn.media.streaming.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import android.view.Surface
import kotlin.time.measureTime

class MSCameraCapture
: CameraCaptureSession.CaptureCallback() {

    companion object {
        private val TAG = MSCameraCapture::class.simpleName
    }
    
    override fun onCaptureStarted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        timestamp: Long,
        frameNumber: Long
    ) {
        //Log.d(TAG, "onCaptureStarted: $timestamp $frameNumber")
    }

    override fun onReadoutStarted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        timestamp: Long,
        frameNumber: Long
    ) {
        //Log.d(TAG, "onReadoutStarted: $timestamp $frameNumber")
    }

    override fun onCaptureProgressed(
        session: CameraCaptureSession,
        request: CaptureRequest,
        partialResult: CaptureResult
    ) {
        //Log.d(TAG, "onCaptureProgressed: ${partialResult.frameNumber} ${partialResult.sequenceId}")
    }

    override fun onCaptureCompleted(
        session: CameraCaptureSession,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        //Log.d(TAG, "onCaptureCompleted: ${result.frameNumber} ${result.partialResults}")
    }

    override fun onCaptureFailed(
        session: CameraCaptureSession,
        request: CaptureRequest,
        failure: CaptureFailure
    ) {
        //Log.d(TAG, "onCaptureFailed: ${failure.reason}")
    }

    override fun onCaptureSequenceCompleted(
        session: CameraCaptureSession,
        sequenceId: Int,
        frameNumber: Long
    ) {
        //Log.d(TAG, "onCaptureSequenceCompleted: $frameNumber")
    }

    override fun onCaptureSequenceAborted(
        session: CameraCaptureSession,
        sequenceId: Int
    ) {
        //Log.d(TAG, "onCaptureSequenceAborted: $sequenceId")
    }

    override fun onCaptureBufferLost(
        session: CameraCaptureSession,
        request: CaptureRequest,
        target: Surface,
        frameNumber: Long
    ) {
        //Log.d(TAG, "onCaptureBufferLost: $target $frameNumber")
    }
}