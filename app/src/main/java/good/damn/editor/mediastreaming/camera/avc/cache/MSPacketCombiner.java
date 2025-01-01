package good.damn.editor.mediastreaming.camera.avc.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

public final class MSPacketCombiner {

    private final HashMap<
        Integer,
        MSPacketFrame
    > mPackets = new HashMap<>();

    public final void write(
        final int packetId,
        final short chunkId,
        final short chunkCount,
        final byte[] data,
        final MSListenerOnCombinePacket onCombinePacket
    ) {

        @Nullable
        final MSPacketFrame frame = mPackets.get(
            packetId
        );

        if (frame == null) {
            mPackets.put(
                packetId,
                new MSPacketFrame(
                    new MSPacket[
                        chunkCount
                    ]
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
        }

        if (chunks.length == chunkCount) {
            onCombinePacket.onCombinePacket(
                packetId,
                frame
            );
        }

    }

}
