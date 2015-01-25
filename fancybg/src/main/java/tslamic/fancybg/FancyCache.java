package tslamic.fancybg;

import android.graphics.Bitmap;

/**
 * A simple caching interface.
 * By default, FancyBackground uses {@link tslamic.fancybg.FancyLruCache}.
 */
public interface FancyCache {

    /**
     * Retrieves the cached Bitmap or null if not in the cache.
     */
    Bitmap get(int key);

    /**
     * Puts the Bitmap in cache.
     *
     * @return true if successfully cached, false otherwise.
     */
    boolean put(int key, Bitmap bitmap);

    /**
     * Returns this cache max size in bytes.
     */
    int getMaxSize();

    /**
     * Returns this cache current size in bytes.
     */
    int getSize();

    /**
     * Clears the cache, evicting and recycling the Bitmaps.
     */
    void clear();

}