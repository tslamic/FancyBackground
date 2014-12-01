package tslamic.com.fancybg;

import android.view.View;
import android.widget.ImageView;

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
        setMeasuredDimension(mSource.getMeasuredWidth(),
                mSource.getMeasuredHeight());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFancyBackground.halt();
    }

    void setScale(FancyScale scale) {
        final ImageView.ScaleType imageScale =
                ImageView.ScaleType.valueOf(scale.name());
        setScaleType(imageScale);
    }

}