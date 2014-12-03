package tslamic.com.fancybg;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public interface FancyAnimation {

    void animate(FancyBackground fb, ImageView source, Drawable next);

}