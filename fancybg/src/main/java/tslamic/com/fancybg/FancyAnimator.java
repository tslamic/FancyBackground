package tslamic.com.fancybg;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public interface FancyAnimator {

    final FancyAnimator NONE = null;

    void animate(FancyBackground fb, ImageView source, Drawable next);

}