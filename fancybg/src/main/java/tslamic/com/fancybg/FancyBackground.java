package tslamic.com.fancybg;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FancyBackground {

    public interface FancyListener {

        void onStarted(FancyBackground bg);

        void onNew(FancyBackground bg, Drawable newDrawable);

        void onLoopDone(FancyBackground bg);

        void onStopped(FancyBackground bg);

    }

    public static Builder on(View view) {
        if (view == null) {
            throw new IllegalArgumentException("view is null");
        }
        return new Builder(view);
    }

    public static class Builder {

        private FancyAnimation animation = FancyAnimationHelper.FADE;
        private int cacheSize = FancyLruCache.DEFAULT_SIZE;
        private FancyScale scale = FancyScale.FIT_XY;
        private FancyListener listener;
        private long interval = 3000;
        private boolean loop = true;
        private int[] drawables;

        private final View view;

        public Builder(View view) {
            this.view = view;
        }

        public Builder set(int... drawables) {
            this.drawables = drawables;
            return this;
        }

        public Builder animateWith(FancyAnimation animation) {
            this.animation = animation;
            return this;
        }

        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public Builder interval(long millis) {
            interval = millis;
            return this;
        }

        public Builder listener(FancyListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder scale(FancyScale scale) {
            this.scale = scale;
            return this;
        }

        public Builder cache(int bytes) {
            cacheSize = bytes;
            return this;
        }

        public FancyBackground build() {
            return new FancyBackground(this);
        }

    }

    // Publicly available instance variables
    public final FancyAnimation animation;
    public final FancyListener listener;
    public final FancyScale scale;
    public final long interval;
    public final int cacheSize;
    public final boolean loop;
    public final View view;

    // Private data
    private final BitmapFactory.Options mOptions;
    private final TypedValue mTypedValue;

    private ScheduledExecutorService mExecutor;
    private FancyImageView mFancyImage;
    private final int[] mDrawables;
    private FancyLruCache mCache;
    private int mNextIndex;

    private FancyBackground(Builder builder) {
        animation = builder.animation;
        listener = builder.listener;
        scale = builder.scale;
        interval = builder.interval;
        cacheSize = builder.cacheSize;
        loop = builder.loop;
        view = builder.view;

        mDrawables = builder.drawables;
        mOptions = new BitmapFactory.Options();
        mTypedValue = new TypedValue();

        if (cacheSize > 0) {
            mCache = new FancyLruCache(cacheSize);
        } else if (cacheSize == FancyLruCache.DEFAULT_SIZE) {
            mCache = new FancyLruCache(view.getContext());
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        final ViewGroup group = getViewGroup(view);
        mFancyImage = new FancyImageView(this, view);
        group.addView(mFancyImage, 0, view.getLayoutParams());
        start();
    }

    void halt() {
        shutdownExecutor();
        mCache.evictAll();
        if (null != listener) {
            listener.onStopped(this);
        }
    }

    private void start() {
        final Drawable drawable = getNextDrawable();
        if (null != drawable) {
            mFancyImage.setImageDrawable(drawable);
        }

        if (mDrawables.length > 1) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
            mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            animateToNextBitmap();
                        }
                    });
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
        }

        if (null != listener) {
            listener.onStarted(this);
        }
    }

    private void animateToNextBitmap() {
        final Drawable drawable = getNextDrawable();
        if (drawable != null) {
            if (null != listener) {
                listener.onNew(this, drawable);
            }
            animation.animate(this, mFancyImage, drawable);
        }
    }

    private Drawable getNextDrawable() {
        final int size = mDrawables.length;
        if (mNextIndex >= size && !loop) {
            if (null != listener) {
                listener.onLoopDone(this);
            }
            shutdownExecutor();
            return null;
        }

        final int resource = mDrawables[mNextIndex++ % size];
        final Resources resources = view.getResources();
        resources.getValue(resource, mTypedValue, true);

        final Drawable drawable;
        if (isBitmap(mTypedValue)) {
            drawable = getBitmapDrawable(resources, resource);
        } else {
            drawable = resources.getDrawable(resource);
        }

        return drawable;
    }

    private Drawable getBitmapDrawable(final Resources resources,
                                       final int resource) {
        final boolean hasCache = mCache != null;
        Bitmap bitmap = null;

        if (hasCache) {
            bitmap = mCache.get(resource);
        }

        if (bitmap == null) {
            final int w = view.getMeasuredWidth();
            final int h = view.getMeasuredHeight();

            mOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, resource, mOptions);

            mOptions.inSampleSize = getSampleSize(mOptions, w, h);
            mOptions.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeResource(resources,
                    resource, mOptions);

            if (hasCache) {
                mCache.put(resource, bitmap);
            }
        }

        return new BitmapDrawable(resources, bitmap);
    }

    private void shutdownExecutor() {
        if (null != mExecutor) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    private static boolean isBitmap(final TypedValue value) {
        boolean isBitmap = false;

        if (TypedValue.TYPE_STRING == value.type) {
            final String file = value.string.toString();
            isBitmap = !file.endsWith(".xml");
        }

        return isBitmap;
    }

    private static int getSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth /
                    inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static ViewGroup getViewGroup(View source) {
        final ViewGroup group;

        if (source instanceof ViewGroup) {
            group = (ViewGroup) source;
        } else {
            group = (ViewGroup) source.getParent();
        }

        return group;
    }

}