package tslamic.com.fancybg;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FancyBackground {

    /**
     * Listens to FancyBackground events.
     */
    public interface FancyListener {

        /**
         * Invoked when the FancyBackground starts displaying Drawables.
         */
        void onStarted(FancyBackground bg);

        /**
         * Invoked every time a new Drawable is loaded.
         */
        void onNew(FancyBackground bg);

        /**
         * Invoked when all the Drawables have been shown.
         */
        void onLoopDone(FancyBackground bg);

        /**
         * Invoked when FancyBackground is stopped.
         */
        void onStopped(FancyBackground bg);

    }

    /**
     * Creates the FancyBackground Builder instance.
     *
     * @param view a view where FancyBackground will be showing Drawables.
     * @return FancyBackground Builder instance.
     * @throws NullPointerException if view is null.
     */
    public static Builder on(final View view) {
        if (view == null) {
            throw new NullPointerException("view is null");
        }
        return new Builder(view);
    }

    public static class Builder {

        private ImageView.ScaleType scale = ImageView.ScaleType.FIT_XY;
        private FancyAnimator animator = FancyAnimator.NONE;
        private FancyCache cache = FancyCache.DEFAULT;
        private FancyListener listener;
        private long interval = 3000;
        private boolean loop = true;
        private int[] drawables;
        private Matrix matrix;

        private final View view;

        private Builder(View view) {
            this.view = view;
        }

        /**
         * Sets the Drawable resources to be displayed.
         *
         * @param drawables Drawable resources.
         */
        public Builder set(int... drawables) {
            if (null == drawables || drawables.length < 2) {
                throw new IllegalArgumentException("at least two drawables " +
                        "required");
            }
            this.drawables = drawables;
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyAnimator}.
         */
        public Builder animator(FancyAnimator animator) {
            this.animator = animator;
            return this;
        }

        /**
         * Determines if the FancyBackground should loop through the
         * Drawables or stop when the last one is reached.
         *
         * @param loop true to loop, false to stop at the last one.
         */
        public Builder loop(boolean loop) {
            this.loop = loop;
            return this;
        }

        /**
         * Sets the millisecond interval a Drawable will be displayed for.
         *
         * @param millis millisecond interval.
         */
        public Builder interval(long millis) {
            if (millis < 0) {
                throw new IllegalArgumentException("negative interval");
            }
            this.interval = millis;
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyBackground.FancyListener}.
         */
        public Builder listener(FancyListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Controls how the Drawables should be resized or moved to match the
         * size of the view FancyBackground will be animating on.
         */
        public Builder scale(ImageView.ScaleType scale) {
            if (scale == null) {
                throw new IllegalArgumentException("scale cannot be null");
            }
            this.scale = scale;
            return this;
        }

        /**
         * Controls how the Drawables should be resized or moved to match the
         * size of the view FancyBackground will be animating on.
         *
         * @param matrix a 3x3 matrix for transforming coordinates.
         */
        public Builder scale(Matrix matrix) {
            this.scale = ImageView.ScaleType.MATRIX;
            this.matrix = matrix;
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyCache}.
         */
        public Builder cache(FancyCache cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Completes the building process and returns a new FancyBackground
         * instance.
         */
        public FancyBackground start() {
            return new FancyBackground(this);
        }

    }

    // Public instance variables
    public final ImageView.ScaleType scale;
    public final FancyAnimator animator;
    public final FancyListener listener;
    public final FancyCache cache;
    public final long interval;
    public final boolean loop;
    public final View view;

    // Private data
    private final AtomicInteger mIndex = new AtomicInteger(0);
    private final ScheduledExecutorService mExecutor;
    private final BitmapFactory.Options mOptions;
    private final TypedValue mTypedValue;
    private final Resources mResources;
    private final int[] mDrawables;
    private final Matrix mMatrix;

    private FancyHandler mHandler;

    private FancyBackground(Builder builder) {
        animator = builder.animator;
        listener = builder.listener;
        scale = builder.scale;
        interval = builder.interval;
        loop = builder.loop;
        view = builder.view;

        if (builder.cache == FancyCache.DEFAULT) {
            cache = new FancyLruCache(view.getContext());
        } else {
            cache = builder.cache;
        }

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mOptions = new BitmapFactory.Options();
        mResources = view.getResources();
        mTypedValue = new TypedValue();
        mDrawables = builder.drawables;
        mMatrix = builder.matrix;

        view.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        final ViewGroup group = getViewGroup(view);

        final FancyImageView bg = new FancyImageView(this, view);
        bg.setScaleType(scale);
        if (mMatrix != null) {
            bg.setImageMatrix(mMatrix);
        }
        mHandler = new FancyHandler(bg);
        group.addView(bg, 0, view.getLayoutParams());

        start();
    }

    private void start() {
        if (null != listener) {
            listener.onStarted(this);
        }
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateDrawable();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }

    public void halt() {
        halt(false);
    }

    private void halt(boolean isLoopDone) {
        mExecutor.shutdownNow();
        cache.clear();
        if (null != listener) {
            if (isLoopDone) {
                listener.onLoopDone(this);
            } else {
                listener.onStopped(this);
            }
        }
    }

    private void updateDrawable() {
        final Drawable drawable = getNext();
        if (null != drawable) {
            final Message msg = mHandler.obtainMessage();
            msg.obj = drawable;
            msg.sendToTarget();
        }
    }

    private Drawable getNext() {
        final Drawable drawable;

        final int size = mDrawables.length;
        if (mIndex.get() >= size && !loop) {
            drawable = null;
            halt(true);
        } else {
            final int index = mIndex.getAndIncrement();
            drawable = getDrawable(mDrawables[index % size]);
        }

        return drawable;
    }

    private Drawable getDrawable(final int resource) {
        Bitmap bitmap = cache.get(resource);
        if (bitmap != null) {
            return new BitmapDrawable(mResources, bitmap);
        }

        final Drawable drawable;
        if (isBitmap(resource)) {
            bitmap = getBitmap(resource);
            drawable = new BitmapDrawable(mResources, bitmap);
        } else {
            drawable = mResources.getDrawable(resource);
        }

        return drawable;
    }

    private Bitmap getBitmap(final int resource) {
        Bitmap bitmap = cache.get(resource);

        if (bitmap == null) {
            final int w = view.getMeasuredWidth();
            final int h = view.getMeasuredHeight();

            mOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(mResources, resource, mOptions);

            mOptions.inSampleSize = getSampleSize(mOptions, w, h);
            mOptions.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeResource(mResources, resource, mOptions);
            cache.put(resource, bitmap);
        }

        return bitmap;
    }

    private boolean isBitmap(final int resource) {
        boolean isBitmap = false;

        mResources.getValue(resource, mTypedValue, true);
        if (TypedValue.TYPE_STRING == mTypedValue.type) {
            final String file = mTypedValue.string.toString();
            if (TextUtils.isEmpty(file)) {
                throw new IllegalArgumentException("not a Drawable: " +
                        mTypedValue.resourceId);
            }
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