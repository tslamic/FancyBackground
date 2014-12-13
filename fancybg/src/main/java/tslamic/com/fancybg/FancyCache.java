package tslamic.com.fancybg;

import android.graphics.Bitmap;

/**
 * A simple caching interface.
 * <p/>
 * By default, FancyBackground implements a simple in-memory LRU cache taking
 * up to 15% per-application memory of the current device.
 *
 * @see tslamic.com.fancybg.FancyLruCache
 */
public interface FancyCache {

    /**
     * Provides the default FancyBackground cache.
     */
    final FancyCache DEFAULT = null;

    /**
     * Ensures no Bitmap cache is used.
     */
    final FancyCache NO_CACHE = new FancyCache() {
        @Override
        public Bitmap get(int key) {
            return null;
        }

        @Override
        public boolean put(int key, Bitmap bitmap) {
            return false;
        }

        @Override
        public void clear() {
            // Do nothing.
        }
    };

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
     * Clears the cache, evicting and recycling the Bitmaps.
     */
    void clear();

}