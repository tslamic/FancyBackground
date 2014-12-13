package tslamic.com.fancybg;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * ImageView responsible for showing FancyBackground drawables.
 */
@SuppressLint("ViewConstructor")
class FancyImageView extends ImageView {

    private final FancyBackground mFancyBackground;
    private final View mSource;

    FancyImageView(FancyBackground fancyBackground, View source) {
        super(source.getContext());
        mFancyBackground = fancyBackground;
        mSource = source;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int w = mSource.getMeasuredWidth();
        final int h = mSource.getMeasuredHeight();
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFancyBackground.halt();
    }

    private void invokeOnNew() {
        final FancyBackground.FancyListener listener = mFancyBackground.listener;
        if (listener != null) {
            listener.onNew(mFancyBackground);
        }
    }

    void fancyAnimate(Drawable drawable) {
        final FancyAnimator animator = mFancyBackground.animator;
        if (animator == FancyAnimator.NONE) {
            setImageDrawable(drawable);
        } else {
            animator.animate(mFancyBackground, this, drawable);
        }
        invokeOnNew();
    }

}