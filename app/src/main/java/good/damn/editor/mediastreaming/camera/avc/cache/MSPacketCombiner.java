package good.damn.editor.mediastreaming.camera.avc.cache;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MSPacketCombiner {

    private static final String TAG = "MSPacketCombiner";

    private static final int CACHE_PACKET = 128;

    private final MSPacketFrame[] mPackets =
        new MSPacketFrame[CACHE_PACKET];

    public final void write(
        final int packetId,
        final short chunkId,
        final short chunkCount,
        final byte[] data,
        final MSListenerOnCombinePacket onCombinePacket
    ) {

        @Nullable
        final MSPacketFrame frame = mPackets[
            packetId % CACHE_PACKET
        ];

        if (frame == null) {
            mPackets.put(
                packetId,
                new MSPacketFrame(
                    new MSPacket[
                        chunkCount
                    ],1
                )
            );
            return;
        }

        @NonNull
        final MSPacket[] chunks = frame
            .getChunks();

        if (chunks[chunkId] == null) {
            chunks[chunkId] = new MSPacket(
                data
            );
            frame.setChunkCountAdded(
                frame.getChunkCountAdded() + 1
            );
        }

        if (frame.getChunkCountAdded() >= chunkCount) {
            onCombinePacket.onCombinePacket(
                packetId,
                frame
            );
        }

    }

}
