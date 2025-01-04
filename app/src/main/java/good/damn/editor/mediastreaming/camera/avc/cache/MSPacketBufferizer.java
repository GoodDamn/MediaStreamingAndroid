package good.damn.editor.mediastreaming.camera.avc.cache;

import androidx.annotation.Nullable;

public final class MSPacketBufferizer {

    private static final String TAG = "MSPacketBufferizer";

    private static final int CACHE_PACKET = 128;

    private final MSPacket[] mPackets =
        new MSPacket[CACHE_PACKET];

    private int mCurrentPacketCycle;
    private int mTempPacketCycle;

    private int minRangePacket;

    public final void write(
        final int packetId,
        final byte[] data,
        final MSListenerOnGetOrderedPacket onGetOrderedPacket
    ) {
        final int bufferId = packetId % CACHE_PACKET;
        mTempPacketCycle = packetId / CACHE_PACKET;

        if (mTempPacketCycle > mCurrentPacketCycle) {
            @Nullable
            MSPacket frame;
            for (short i = 0; i < mPackets.length; i++) {
                frame = mPackets[i];
                if (frame == null ||
                    frame.getId() < minRangePacket
                ) {
                    mPackets[i] = null;
                    continue;
                }

                onGetOrderedPacket.onGetOrderedPacket(
                    frame
                );
            }

            mCurrentPacketCycle = mTempPacketCycle;
            minRangePacket += CACHE_PACKET;
        }

        @Nullable
        final MSPacket frame = mPackets[
            bufferId
        ];

        if (frame == null ||
            mCurrentPacketCycle >= frame.getId() / CACHE_PACKET
        ) {
            mPackets[
                bufferId
            ] = new MSPacket(
                packetId,
                data
            );
        }

    }

}
