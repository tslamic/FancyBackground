package tslamic.fancybg;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

/**
 * Responsible for animating set Drawables from
 * {@link tslamic.fancybg.FancyBackground}.
 */
@SuppressLint("ViewConstructor")
class FancyImageSwitcher extends ImageSwitcher {

    private final FancyBackground mFancyBg;
    private final Handler mHandler;

    FancyImageSwitcher(final FancyBackground fancyBg) {
        super(fancyBg.view.getContext());

        mFancyBg = fancyBg;
        mHandler = getFancyHandler();

        setFactory(getFancyFactory());
        setInAnimation(mFancyBg.inAnimation);
        setOutAnimation(mFancyBg.outAnimation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getNextView().measure(widthMeasureSpec, heightMeasureSpec);
        final int w = mFancyBg.view.getMeasuredWidth();
        final int h = mFancyBg.view.getMeasuredHeight();
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFancyBg.halt();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        final FancyBackground.FancyListener listener = mFancyBg.listener;
        if (null != listener) {
            listener.onNew(mFancyBg);
        }
    }

    @Override
    public final Handler getHandler() {
        return mHandler;
    }

    private Handler getFancyHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                final Drawable drawable = (Drawable) msg.obj;
                setImageDrawable(drawable);
            }
        };
    }

    private ViewFactory getFancyFactory() {
        return new ViewFactory() {
            @Override
            public View makeView() {
                final View source = mFancyBg.view;
                final ImageView view = new ImageView(source.getContext());
                view.setLayoutParams(source.getLayoutParams());
                view.setScaleType(mFancyBg.scale);

                final Matrix matrix = mFancyBg.matrix;
                if (null != matrix) {
                    view.setImageMatrix(matrix);
                }

                return view;
            }
        };
    }

}