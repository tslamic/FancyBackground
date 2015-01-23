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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
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
         * Invoked when all the Drawables have been shown and the looping is
         * complete.
         */
        void onLoopDone(FancyBackground bg);

        /**
         * Invoked when FancyBackground is stopped.
         */
        void onStopped(FancyBackground bg);

    }

    /**
     * Creates a new FancyBackground Builder instance.
     *
     * @param view a view where FancyBackground should show Drawables.
     * @return FancyBackground.Builder instance.
     */
    public static Builder on(final View view) {
        if (view == null) {
            throw new NullPointerException("view is null");
        }
        return new Builder(view);
    }

    public static class Builder {

        private ImageView.ScaleType scale = ImageView.ScaleType.FIT_XY;
        private FancyListener listener;
        private Animation outAnimation;
        private Animation inAnimation;
        private long interval = 3000;
        private boolean loop = true;
        private FancyCache cache;
        private int[] drawables;
        private Matrix matrix;

        private final View view;

        /*
         * Private constructor. Use "on" static factory method.
         */
        private Builder(View view) {
            this.view = view;
            cache = new FancyLruCache(view.getContext());
        }

        /**
         * Sets the Drawable resources to be displayed.
         *
         * @param drawables Drawable resources.
         */
        public Builder set(int... drawables) {
            if (null == drawables || drawables.length < 2) {
                throw new IllegalStateException("at least two drawables required");
            }
            this.drawables = drawables;
            return this;
        }

        public Builder inAnimation(Animation animation) {
            if (null == animation) {
                throw new IllegalStateException("in animation is null");
            }
            inAnimation = animation;
            return this;
        }

        public Builder inAnimation(int animation) {
            inAnimation = AnimationUtils.loadAnimation(view.getContext(),
                    animation);
            return this;
        }

        public Builder outAnimation(Animation animation) {
            if (null == animation) {
                throw new IllegalStateException("out animation is null");
            }
            outAnimation = animation;
            return this;
        }

        public Builder outAnimation(int animation) {
            outAnimation = AnimationUtils.loadAnimation(view.getContext(),
                    animation);
            return this;
        }

        /**
         * Determines if the FancyBackground should continuously loop through
         * the Drawables or stop after the first one.
         *
         * @param loop true to loop, false to stop after the first one
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
                throw new IllegalArgumentException("scale is null");
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
         * Sets the {@link tslamic.com.fancybg.FancyCache}. Use null to avoid
         * caching.
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

    public final ImageView.ScaleType scale;
    public final FancyListener listener;
    public final Animation outAnimation;
    public final Animation inAnimation;
    public final FancyCache cache;
    public final long interval;
    public final Matrix matrix;
    public final boolean loop;
    public final View view;

    private final AtomicInteger mIndex = new AtomicInteger(0);
    private final ScheduledExecutorService mExecutor;
    private final BitmapFactory.Options mOptions;
    private final TypedValue mTypedValue;
    private final Resources mResources;
    private final int[] mDrawables;

    private ImageSwitcher mSwitcher;

    /*
     * Private constructor. Use a Builder.
     */
    private FancyBackground(Builder builder) {
        outAnimation = builder.outAnimation;
        inAnimation = builder.inAnimation;
        listener = builder.listener;
        interval = builder.interval;
        cache = builder.cache;
        scale = builder.scale;
        loop = builder.loop;
        view = builder.view;

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mOptions = new BitmapFactory.Options();
        mResources = view.getResources();
        mTypedValue = new TypedValue();
        mDrawables = builder.drawables;
        matrix = builder.matrix;

        view.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        final ViewGroup group = getViewGroup(view);
        mSwitcher = new FancyImageSwitcher(this);
        group.addView(mSwitcher, 0, view.getLayoutParams());
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

    /**
     * Stops the loop and releases the cached resources.
     */
    public void halt() {
        halt(false);
    }

    private void halt(boolean isLoopDone) {
        mExecutor.shutdownNow();
        if (null != cache) {
            cache.clear();
        }
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
            final Message msg = mSwitcher.getHandler().obtainMessage();
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
        Bitmap bitmap = null;

        if (null != cache) {
            bitmap = cache.get(resource);
        }

        if (null != bitmap) {
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
        final boolean hasCache = null != cache;
        Bitmap bitmap = null;

        if (hasCache) {
            bitmap = cache.get(resource);
        }

        if (null == bitmap) {
            final int w = view.getMeasuredWidth();
            final int h = view.getMeasuredHeight();

            mOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(mResources, resource, mOptions);

            mOptions.inSampleSize = getSampleSize(mOptions, w, h);
            mOptions.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeResource(mResources, resource, mOptions);
            if (hasCache) {
                cache.put(resource, bitmap);
            }
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