package tslamic.com.fancybg;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Defines the transition of background drawables.
 */
public interface FancyAnimator {

    final FancyAnimator NONE = null;

    /**
     * Sets the next Drawable as the content of the ImageView source.
     */
    void animate(FancyBackground fb, ImageView source, Drawable next);

}