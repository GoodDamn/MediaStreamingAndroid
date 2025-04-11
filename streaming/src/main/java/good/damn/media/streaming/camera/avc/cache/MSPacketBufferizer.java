package good.damn.media.streaming.camera.avc.cache;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class MSPacketBufferizer {

    private static final String TAG = "MSPacketBufferizer";

    public static final int CACHE_PACKET_SIZE = 1024;

    // Think about dynamic timeout
    // which depends from captured frame's packet count
    // if it's near to full frame, wait more than 5000 ms
    // if it's not, may be 200 ms or drop it now

    // Algorithm which targets Key frames
    // if buffer has next combined key frame, it needs to update
    // if buffer has key frame's other frames, it needs to update

    private final MSMFrameQueue[] mQueues = new MSMFrameQueue[
        CACHE_PACKET_SIZE
    ];

    private volatile boolean mIsLocked = false;

    private volatile int mCurrentQueueIndex = 0;

    public MSPacketBufferizer() {
        for (int i = 0; i < CACHE_PACKET_SIZE; i++) {
            mQueues[i] = new MSMFrameQueue();
        }
    }

    public final void unlock() {
        mCurrentQueueIndex = 0;
        mIsLocked = false;
    }

    public final void lock() {
        mIsLocked = true;
    }

    public final void clear() {
        mCurrentQueueIndex = 0;
        for (int i = 0; i < CACHE_PACKET_SIZE; i++) {
            mQueues[i].lastFrameId = -1;
            mQueues[i].queue.clear();
        }
    }

    @NonNull
    public final ConcurrentLinkedDeque<MSFrame> getOrderedQueue() {
        synchronized (mQueues) {
            mCurrentQueueIndex++;
            if (mCurrentQueueIndex >= mQueues.length) {
                mCurrentQueueIndex = 0;
            }

            return mQueues[
                mCurrentQueueIndex
            ].queue;
        }
    }

    @Nullable
    public final MSFrame getFrameById(
        int frameId
    ) {
        try {
            return mQueues[
                hashFrame(frameId)
            ].queue.getFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public final void removeFirstFrameQueueByFrameId(
        int frameId
    ) {
        mQueues[
            hashFrame(frameId)
        ].queue.removeFirst();
    }

    public final void write(
        final int frameId,
        final short packetId,
        final short packetCount,
        final byte[] data
    ) {
        if (packetCount == 0 || mIsLocked) {
            return;
        }

        final int queueId = hashFrame(frameId);

        final MSMFrameQueue frameQueue = mQueues[queueId];
        final ConcurrentLinkedDeque<MSFrame> queue = frameQueue.queue;

        if (frameQueue.lastFrameId > frameId) {
            return;
        }

        if (queue.isEmpty()) {
            if (frameQueue.lastFrameId >= frameId) {
                return;
            }

            addFrame(
                frameQueue,
                frameId,
                packetCount,
                packetId,
                data
            );
            return;
        }

        try {
            if (frameId < queue.getLast().getId()) {
                return;
            }

        } catch (NoSuchElementException e) {
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
                frameQueue,
                frameId,
                packetCount,
                packetId,
                data
            );
            return;
        }

        if (packetId < 0 || packetId >= foundFrame.getPackets().length) {
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

    public final void findFirstMissingPacket(
        @NonNull final MSIOnEachMissedPacket onEachMissedPacket
    ) {
        for (MSMFrameQueue item : mQueues) {
            try {
                final MSFrame frame = item.queue.getFirst();
                final MSPacket[] packets = frame.getPackets();
                for (
                    short packetId = 0;
                    packetId < packets.length;
                    packetId++
                ) {
                    if (packets[packetId] != null) {
                        continue;
                    }

                    onEachMissedPacket.onEachMissedPacket(
                        frame.getId(),
                        packetId
                    );
                }
            } catch (Exception ignored) {}
        }
    }

    private final int hashFrame(
        int frameId
    ) {
        return frameId % CACHE_PACKET_SIZE;
    }

    private final void addFrame(
        final MSMFrameQueue frameQueue,
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

        frame.getPackets()[
          packetId
        ] = new MSPacket(
            packetId,
            data
        );

        frameQueue.queue.addLast(
            frame
        );

        frameQueue.lastFrameId = frameId;
    }

}
