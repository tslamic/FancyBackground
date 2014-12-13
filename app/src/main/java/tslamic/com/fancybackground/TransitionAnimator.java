package tslamic.com.fancybackground;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import tslamic.com.fancybg.FancyAnimator;
import tslamic.com.fancybg.FancyBackground;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TransitionAnimator implements FancyAnimator {

    private final PropertyValuesHolder mAlpha =
            PropertyValuesHolder.ofFloat("alpha", 0, 1);

    private final PropertyValuesHolder mScaleX =
            PropertyValuesHolder.ofFloat("scaleX", 1.5f, 1f);

    private final PropertyValuesHolder mScaleY =
            PropertyValuesHolder.ofFloat("scaleY", 1.5f, 1f);

    @Override
    public void animate(FancyBackground fb, ImageView source, Drawable next) {
        source.setImageDrawable(next);
        ObjectAnimator
                .ofPropertyValuesHolder(source, mAlpha, mScaleX, mScaleY)
                .setDuration(666)
                .start();
    }

}