package good.damn.editor.mediastreaming.camera.avc.cache;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class MSPacketBufferizer {

    private static final String TAG = "MSPacketBufferizer";

    private static final int CACHE_PACKET_SIZE = 4096;
    private static final int TIMEOUT_PACKET_MS = 20;
    // Think about dynamic timeout
    // which depends from captured frame's packet count
    // if it's near to full frame, wait more than 5000 ms
    // if it's not, may be 200 ms or drop it now

    @Nullable
    public MSListenerOnGetOrderedFrame onGetOrderedFrame;

    // Algorithm which targets Key frames
    // if buffer has next combined key frame, it needs to update
    // if buffer has key frame's other frames, it needs to update

    private final ConcurrentLinkedDeque<
        MSFrame
    >[] mQueues = new ConcurrentLinkedDeque[
        CACHE_PACKET_SIZE
    ];

    private long mCurrentTime = 0L;
    private long mCapturedTime = 0L;

    private volatile int mCurrentQueueIndex = 0;

    public MSPacketBufferizer() {
        for (int i = 0; i < CACHE_PACKET_SIZE; i++) {
            mQueues[i] = new ConcurrentLinkedDeque<>();
        }
    }

    public final void orderPacket() {
        synchronized (mQueues) {
            mCapturedTime = System.currentTimeMillis();
            mCurrentTime = mCapturedTime;

            mCurrentQueueIndex++;
            if (mCurrentQueueIndex >= mQueues.length) {
                mCurrentQueueIndex = 0;
            }

            @NonNull
            final ConcurrentLinkedDeque<
                MSFrame
            > queue = mQueues[
                mCurrentQueueIndex
            ];

            if (queue.isEmpty()) {
                return;
            }

            @NonNull
            MSFrame frame = queue.getFirst();

            int currentPacketSize = frame.getPacketsAdded();
            while (
                mCurrentTime - mCapturedTime < TIMEOUT_PACKET_MS
            ) {
                mCurrentTime = System.currentTimeMillis();

                if (frame.getPacketsAdded() > currentPacketSize) {
                    currentPacketSize = frame.getPacketsAdded();
                    mCapturedTime = mCurrentTime;
                }

                // Waiting when frame will be combined
                // if it's not, drop it because of timeout
                if (currentPacketSize >= frame.getPackets().length) {
                    if (onGetOrderedFrame != null) {
                        onGetOrderedFrame.onGetOrderedFrame(
                            frame
                        );
                    }
                    break;
                }
            }

            queue.removeFirst();

        }
    }

    public final void write(
        final int frameId,
        final short packetId,
        final short packetCount,
        final byte[] data
    ) {
        if (packetCount == 0) {
            return;
        }

        final int queueId = frameId % CACHE_PACKET_SIZE;

        final ConcurrentLinkedDeque<
            MSFrame
        > queue = mQueues[
            queueId
        ];

        if (queue.isEmpty()) {
            addFrame(
                queue,
                frameId,
                packetCount,
                packetId,
                data
            );
            return;
        }

        if (frameId < queue.getLast().getId()) {
            return;
        }

        @Nullable
        MSFrame foundFrame = null;
        for (MSFrame frame: queue) {
            if (frame.getId() == frameId) {
                foundFrame = frame;
                break;
            }
        }

        if (foundFrame == null) {
            addFrame(
                queue,
                frameId,
                packetCount,
                packetId,
                data
            );
            return;
        }

        if (foundFrame.getPackets()[packetId] != null) {
            return;
        }

        foundFrame.getPackets()[
            packetId
        ] = new MSPacket(
            packetId,
            data
        );

        foundFrame.setPacketsAdded(
            (short) (foundFrame.getPacketsAdded() + 1)
        );
    }


    private void addFrame(
        final ConcurrentLinkedDeque<MSFrame> queue,
        final int frameId,
        final short packetCount,
        final short packetId,
        final byte[] data
    ) {
        MSFrame frame = new MSFrame(
          frameId,
          new MSPacket[
            packetCount
            ],
          (short) 1
        );

        Log.d(TAG, "addFrame: " + packetCount + " FRAME_ID:" + frameId);

        frame.getPackets()[
          packetId
        ] = new MSPacket(
          packetId,
          data
        );

        queue.add(
          frame
        );
    }

}
