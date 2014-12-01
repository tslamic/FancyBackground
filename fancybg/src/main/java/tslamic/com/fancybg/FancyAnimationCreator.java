package tslamic.com.fancybg;

import android.graphics.drawable.Drawable;

public interface FancyAnimationCreator {

    void animate(FancyBackground fb, FancyImageView source, Drawable next);

}