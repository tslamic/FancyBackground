package tslamic.fancybg;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Build;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple in-memory LRU Bitmap cache.
 */
public class FancyLruCache implements FancyCache {

    private static final int DEFAULT_CACHE_PERCENTAGE = 25;

    private final LinkedHashMap<Integer, Bitmap> mCache;
    private final int mMaxSize;
    private int mSize;

    /**
     * Constructs a new instance targeting ~25% of the available heap.
     */
    public FancyLruCache(Context context) {
        this(context, DEFAULT_CACHE_PERCENTAGE);
    }

    /**
     * Constructs a new instance.
     *
     * @param cachePercentage integer value between 1 and 80 (inclusive),
     *                        denoting the percentage of available heap to
     *                        target as cache.
     */
    public FancyLruCache(Context context, int cachePercentage) {
        if (cachePercentage < 1 || cachePercentage > 80) {
            throw new IllegalArgumentException("cache percentage must be " +
                    "between 1 and 80");
        }
        mCache = new LinkedHashMap<Integer, Bitmap>();
        mMaxSize = getDefaultCacheSize(context, cachePercentage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean put(int key, Bitmap bitmap) {
        if (null == bitmap) {
            throw new IllegalArgumentException("caching null bitmap");
        }

        final int requiredSize = getBitmapSize(bitmap);
        if (requiredSize > mMaxSize) {
            return false;
        }

        while (!mCache.isEmpty() && (mSize + requiredSize) > mMaxSize) {
            evict(false);
        }
        mCache.put(key, bitmap);
        mSize += requiredSize;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bitmap get(int key) {
        return mCache.get(key);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public int getMaxSize() {
        return mMaxSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return mSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        evict(true);
    }

    private void evict(final boolean all) {
        final Iterator<Map.Entry<Integer, Bitmap>> iterator =
                mCache.entrySet().iterator();
        if (iterator.hasNext()) {
            evictBitmap(iterator);
        }
        if (all) {
            while (iterator.hasNext()) {
                evictBitmap(iterator);
            }
        }
    }

    private void evictBitmap(Iterator<Map.Entry<Integer, Bitmap>> iterator) {
        final Bitmap bitmap = iterator.next().getValue();
        mSize -= getBitmapSize(bitmap);
        bitmap.recycle();
        iterator.remove();
    }

    private static int getDefaultCacheSize(Context context, int percent) {
        final ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        final int memory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            memory = getDefaultCacheSizeHoneycomb(context, manager);
        } else {
            memory = manager.getMemoryClass();
        }

        final float percentAsFloat = percent / 100.0f;
        return (int) ((1024 * 1024 * memory) * percentAsFloat);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static int getDefaultCacheSizeHoneycomb(Context context,
                                                    ActivityManager manager) {
        final int flags = context.getApplicationInfo().flags;
        final boolean isLarge = (flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;

        final int memory;
        if (isLarge) {
            memory = manager.getLargeMemoryClass();
        } else {
            memory = manager.getMemoryClass();
        }

        return memory;
    }

    private static int getBitmapSize(Bitmap bitmap) {
        final int bytes;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            bytes = bitmap.getByteCount();
        } else {
            bytes = bitmap.getRowBytes() * bitmap.getHeight();
        }

        return bytes;
    }

}