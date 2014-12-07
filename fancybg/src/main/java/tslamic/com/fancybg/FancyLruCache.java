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
 * Default, in-memory LRU Bitmap cache.
 */
class FancyLruCache implements FancyCache {

    private final LinkedHashMap<Integer, Bitmap> mCache;
    private final int mMaxSize;
    private int mSize;

    FancyLruCache(Context context) {
        mCache = new LinkedHashMap<Integer, Bitmap>();
        mMaxSize = getDefaultCacheSize(context);
    }

    @Override
    public boolean put(int key, Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("caching null bitmap");
        }

        final int requiredSize = getBitmapSize(bitmap);
        if (requiredSize > mMaxSize) {
            return false;
        }

        while (mSize + requiredSize > mMaxSize && !mCache.isEmpty()) {
            evictNextFrom(mCache.entrySet().iterator());
        }
        mCache.put(key, bitmap);
        mSize += requiredSize;

        return true;
    }

    @Override
    public Bitmap get(int key) {
        return mCache.get(key);
    }

    @Override
    public void clear() {
        final Iterator<Map.Entry<Integer, Bitmap>> iterator =
                mCache.entrySet().iterator();
        while (iterator.hasNext()) {
            evictNextFrom(iterator);
        }
    }

    /*
    * Assumes the iterator contains at least one element.
    */
    private void evictNextFrom(Iterator<Map.Entry<Integer, Bitmap>> iterator) {
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

        return 1024 * 1024 * memory / 7;
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