package good.damn.editor.mediastreaming.utils;

import android.util.Log;

import java.nio.ByteBuffer;

import good.damn.editor.mediastreaming.network.client.MSClientStreamUDP;
import good.damn.media.gles.gl.textures.GLTexture;

public final class MSUtilsImage {

    private static final String TAG = "MSUtilsImage";

    public static void fromYUVtoARGB(
      final ByteBuffer yBuffer,
      final ByteBuffer uBuffer,
      final ByteBuffer vBuffer,
      final ByteBuffer colorBuffer,
      final MSClientStreamUDP client,
      final int yRowStride,
      final int yPixelStride,
      final int uvRowStride,
      final int uvPixelStride,
      final int imageWidth,
      final int imageHeight
    ) {

        int r,g,b;
        int y, u, v;

        int colorIndex = 0;
        for (int iy = 0; iy < imageHeight; iy++) {
            for (int ix = 0; ix < imageWidth; ix++) {
                int yIndex = (iy * yRowStride) + (ix * yPixelStride);
                y = yBuffer.get(yIndex) & 0xff;

                int uvx = ix / 2;
                int uvy = iy / 2;

                int uvIndex = (uvy * uvRowStride) + (uvx * uvPixelStride);

                u = (uBuffer.get(uvIndex) & 0xff) - 128;
                v = (vBuffer.get(uvIndex) & 0xff) - 128;

                r = (int) (y + 1.3707f * v);
                g = (int) (y - 0.698f * v - 0.33763f * u);
                b = (int) (y + 1.7325f * u);

                r = clamp(r, 0, 255);
                g = clamp(g, 0, 255);
                b = clamp(b, 0, 255);

                colorBuffer.put(
                    colorIndex,
                    (byte) 255
                );

                /*client.sendToStream(
                  (byte) (0xff)
                );

                client.sendToStream(
                  (byte) (r & 0xff)
                );

                client.sendToStream(
                  (byte) (g & 0xff)
                );

                client.sendToStream(
                  (byte) (b & 0xff)
                );*/

                colorIndex++;

                colorBuffer.put(
                  colorIndex, (byte) (r & 0xff)
                );

                colorIndex++;

                colorBuffer.put(
                  colorIndex, (byte) (g & 0xff)
                );

                colorIndex++;

                colorBuffer.put(
                  colorIndex, (byte) (b & 0xff)
                );

                colorIndex++;
            }
        }

    }

    private static int clamp(
        int v,
        int min,
        int max
    ) {
        return Math.max(
            min,
            Math.min(
                max,
                v
            )
        );
    }

}
