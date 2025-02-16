package good.damn.media.streaming.misc;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import android.os.Handler;

public final class HandlerExecutor implements Executor {

    @NonNull
    private final Handler handler;

    public HandlerExecutor(
      @NonNull Handler handler
    ) {
        this.handler = handler;
    }

    @Override
    public void execute(
      Runnable command
    ) {
        handler.post(
          command
        );
    }
}
