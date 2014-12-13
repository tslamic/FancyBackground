package tslamic.com.fancybg;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Sends the newly loaded Drawable from a worker thread to the UI thread.
 */
class FancyHandler extends Handler {

    private final WeakReference<FancyImageView> mImageViewRef;

    public FancyHandler(FancyImageView imageView) {
        super(Looper.getMainLooper());
        mImageViewRef = new WeakReference<FancyImageView>(imageView);
    }

    @Override
    public void handleMessage(Message msg) {
        final FancyImageView view = mImageViewRef.get();
        if (view != null) {
            view.fancyAnimate((Drawable) msg.obj);
        }
    }

}