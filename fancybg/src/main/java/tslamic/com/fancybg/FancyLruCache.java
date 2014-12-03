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

class FancyLruCache {

    public static final int DEFAULT_SIZE = 0;
    public static final int NO_CACHE = -1;

    private final LinkedHashMap<Integer, Bitmap> mCache;
    private final int mMaxSize;
    private int mSize;

    FancyLruCache(Context context) {
        this(getDefaultCacheSize(context));
    }

    FancyLruCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("illegal cache size: " + maxSize);
        }
        mCache = new LinkedHashMap<Integer, Bitmap>();
        mMaxSize = maxSize;
    }

    boolean put(int key, Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("trying to cache null bitmap");
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

    Bitmap get(int key) {
        return mCache.get(key);
    }

    private void evictNextFrom(Iterator<Map.Entry<Integer, Bitmap>> iterator) {
        final Bitmap bitmap = iterator.next().getValue();
        mSize -= getBitmapSize(bitmap);
        bitmap.recycle();
        iterator.remove();
    }

    void evictAll() {
        final Iterator<Map.Entry<Integer, Bitmap>> iterator =
                mCache.entrySet().iterator();
        while (iterator.hasNext()) {
            evictNextFrom(iterator);
        }
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