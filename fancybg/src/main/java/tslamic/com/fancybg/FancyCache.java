package tslamic.com.fancybg;

import android.graphics.Bitmap;

/**
 * Defines a caching interface.
 */
public interface FancyCache {

    final FancyCache DEFAULT = null;

    Bitmap get(int key);

    boolean put(int key, Bitmap bitmap);

    void clear();

}