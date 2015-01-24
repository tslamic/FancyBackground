package tslamic.com.fancybg;

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
 * Simple in-memory LRU Bitmap cache. Takes ~20% of the application
 * memory.
 */
class FancyLruCache implements FancyCache {

    private final LinkedHashMap<Integer, Bitmap> mCache;
    private final int mMaxSize;
    private int mSize;

    FancyLruCache(Context context) {
        mCache = new LinkedHashMap<Integer, Bitmap>();
        mMaxSize = getDefaultCacheSize(context);
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

    private static int getDefaultCacheSize(Context context) {
        final ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        final int memory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            memory = getDefaultCacheSizeHoneycomb(context, manager);
        } else {
            memory = manager.getMemoryClass();
        }

        return 1024 * 1024 * memory / 5; // ~20% of available memory
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