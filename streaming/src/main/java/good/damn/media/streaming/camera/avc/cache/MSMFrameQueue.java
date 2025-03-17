package good.damn.media.streaming.camera.avc.cache;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class MSMFrameQueue {
    public final ConcurrentLinkedDeque<
        MSFrame
    > queue = new ConcurrentLinkedDeque<>();

    public int lastFrameId = -1;
}
