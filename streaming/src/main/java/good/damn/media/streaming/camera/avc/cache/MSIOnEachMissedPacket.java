package good.damn.media.streaming.camera.avc.cache;
public interface MSIOnEachMissedPacket {
    public void onEachMissedPacket(
        int frameId,
        short packetId
    );
}
