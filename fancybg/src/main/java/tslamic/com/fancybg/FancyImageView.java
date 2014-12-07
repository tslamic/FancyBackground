package tslamic.com.fancybg;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("ViewConstructor")
class FancyImageView extends ImageView {

    private final FancyBackground mFancyBackground;
    private final View mSource;

    FancyImageView(FancyBackground fancyBackground, View source) {
        super(source.getContext());

        mFancyBackground = fancyBackground;
        mSource = source;
        setScale(fancyBackground.scale);
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

    void setScale(FancyScale scale) {
        final ScaleType type = ImageView.ScaleType.valueOf(scale.name());
        setScaleType(type);
    }

    void fancyAnimate(Drawable drawable) {
        final FancyAnimator animator = mFancyBackground.animator;
        if (animator == FancyAnimator.NONE) {
            setImageDrawable(drawable);
        } else {
            animator.animate(mFancyBackground, this, drawable);
        }

        final FancyBackground.FancyListener listener =
                mFancyBackground.listener;
        if (listener != null) {
            listener.onNew(mFancyBackground, drawable);
        }
    }

}