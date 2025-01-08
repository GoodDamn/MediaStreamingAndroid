package good.damn.editor.mediastreaming.camera.avc.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MSPacketBufferizer {

    private static final String TAG = "MSPacketBufferizer";

    private static final int CACHE_PACKET_SIZE = 1024;
    private static final int TIMEOUT_PACKET_MS = 5000;

    // Algorithm which targets Key frames
    // if buffer has next combined key frame, it needs to update
    // if buffer has key frame's other frames, it needs to update

    private final ConcurrentLinkedDeque<
        MSFrame
    >[] mQueues = new ConcurrentLinkedDeque[
        CACHE_PACKET_SIZE
    ];

    public final void write(
        final int frameId,
        final int packetId,
        final short packetCount,
        final byte[] data,
        final MSListenerOnGetOrderedPacket onGetOrderedPacket
    ) {
        final int queueId = frameId % CACHE_PACKET_SIZE;

        final ConcurrentLinkedDeque<
            MSFrame
        > queue = mQueues[
            queueId
        ];

        if (queue.isEmpty()) {
            queue.add(
                new MSFrame(
                    frameId,
                    new MSPacket[
                        packetCount
                    ],
                    (short) 0
                )
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

}
