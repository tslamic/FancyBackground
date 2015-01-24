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

public class FancyBackground {

    /**
     * Listens to FancyBackground events.
     */
    public interface FancyListener {

        /**
         * Invoked when the {@link tslamic.com.fancybg.FancyBackground} starts.
         */
        void onStarted(FancyBackground bg);

        /**
         * Invoked when a new Drawable is loaded.
         */
        void onNew(FancyBackground bg);

        /**
         * Invoked if looping is set to false and the first loop through
         * the set Drawables is complete.
         */
        void onLoopDone(FancyBackground bg);

        /**
         * Invoked when the {@link tslamic.com.fancybg.FancyBackground}
         * is stopped.
         */
        void onStopped(FancyBackground bg);

    }

    /**
     * Creates a new {@link tslamic.com.fancybg.FancyBackground.Builder}
     * instance.
     *
     * @param view a view where {@link tslamic.com.fancybg.FancyBackground}
     *             should show Drawables.
     * @return {@link tslamic.com.fancybg.FancyBackground.Builder} instance.
     */
    public static Builder on(final View view) {
        if (null == view) {
            throw new IllegalArgumentException("view is null");
        }
        return new Builder(view);
    }

    public static class Builder {

        private final View mView;

        private ImageView.ScaleType mScale = ImageView.ScaleType.FIT_XY;
        private FancyListener mListener;
        private Animation mOutAnimation;
        private Animation mInAnimation;
        private FancyPainter mPainter;
        private long mInterval = 3000;
        private boolean mLoop = true;
        private FancyCache mCache;
        private int[] mDrawables;
        private Matrix mMatrix;

        /*
         * Private constructor. Use "on" static factory method to create an
         * instance.
         */
        private Builder(final View view) {
            mView = view;
            mCache = new FancyLruCache(view.getContext());
        }

        /**
         * Sets the Drawable resources to be displayed.
         *
         * @param drawables Drawable resources.
         */
        public Builder set(final int... drawables) {
            mDrawables = drawables;
            return this;
        }

        /**
         * Specifies the animation used to animate a View that enters the
         * screen.
         */
        public Builder inAnimation(final Animation animation) {
            if (null == animation) {
                throw new IllegalStateException("in animation is null");
            }
            mInAnimation = animation;
            return this;
        }

        /**
         * Specifies the animation used to animate a View that enters the
         * screen.
         */
        public Builder inAnimation(final int animation) {
            mInAnimation = AnimationUtils.loadAnimation(mView.getContext(),
                    animation);
            return this;
        }

        /**
         * Specifies the animation used to animate a View that exit the screen.
         */
        public Builder outAnimation(final Animation animation) {
            if (null == animation) {
                throw new IllegalStateException("out animation is null");
            }
            mOutAnimation = animation;
            return this;
        }

        /**
         * Specifies the animation used to animate a View that exit the screen.
         */
        public Builder outAnimation(final int animation) {
            mOutAnimation = AnimationUtils.loadAnimation(mView.getContext(),
                    animation);
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyPainter}.
         */
        public Builder painter(final FancyPainter painter) {
            mPainter = painter;
            return this;
        }

        /**
         * Determines if the FancyBackground should continuously loop through
         * the Drawables or stop after the first one.
         *
         * @param loop true to loop, false to stop after the first one
         */
        public Builder loop(final boolean loop) {
            mLoop = loop;
            return this;
        }

        /**
         * Sets the millisecond mInterval a Drawable will be displayed for.
         *
         * @param millis millisecond mInterval.
         */
        public Builder interval(final long millis) {
            if (millis < 0) {
                throw new IllegalArgumentException("negative interval");
            }
            mInterval = millis;
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyBackground.FancyListener}.
         */
        public Builder listener(final FancyListener listener) {
            mListener = listener;
            return this;
        }

        /**
         * Controls how the Drawables should be resized or moved to match the
         * size of the view FancyBackground will be animating on.
         */
        public Builder scale(final ImageView.ScaleType scale) {
            if (null == scale) {
                throw new IllegalArgumentException("scale is null");
            }
            mScale = scale;
            return this;
        }

        /**
         * Controls how the Drawables should be resized or moved to match the
         * size of the view FancyBackground will be animating on.
         *
         * @param matrix a 3x3 matrix for transforming coordinates.
         */
        public Builder scale(final Matrix matrix) {
            if (null == matrix) {
                throw new IllegalArgumentException("matrix is null");
            }
            mScale = ImageView.ScaleType.MATRIX;
            mMatrix = matrix;
            return this;
        }

        /**
         * Sets the {@link tslamic.com.fancybg.FancyCache}. Use null to avoid
         * caching.
         */
        public Builder cache(final FancyCache cache) {
            mCache = cache;
            return this;
        }

        /**
         * Completes the building process and returns a new FancyBackground
         * instance.
         */
        public FancyBackground start() {
            if (null == mDrawables || mDrawables.length < 2) {
                throw new IllegalStateException("at least two drawables required");
            }
            return new FancyBackground(this);
        }

    }

    public final ImageView.ScaleType scale;
    public final FancyListener listener;
    public final Animation outAnimation;
    public final Animation inAnimation;
    public final FancyPainter painter;
    public final FancyCache cache;
    public final long interval;
    public final Matrix matrix;
    public final boolean loop;
    public final View view;

    //private final AtomicInteger mIndex = new AtomicInteger(0);
    private final ScheduledExecutorService mExecutor;
    private final BitmapFactory.Options mOptions;
    private final TypedValue mTypedValue;
    private final Resources mResources;
    private final int[] mDrawables;

    private ImageSwitcher mSwitcher;
    private int mIndex;

    /*
     * Private constructor. Use a Builder to create an instance.
     */
    private FancyBackground(Builder builder) {
        outAnimation = builder.mOutAnimation;
        inAnimation = builder.mInAnimation;
        listener = builder.mListener;
        interval = builder.mInterval;
        painter = builder.mPainter;
        cache = builder.mCache;
        scale = builder.mScale;
        loop = builder.mLoop;
        view = builder.mView;

        mExecutor = Executors.newSingleThreadScheduledExecutor();
        mOptions = new BitmapFactory.Options();
        mResources = view.getResources();
        mTypedValue = new TypedValue();
        mDrawables = builder.mDrawables;
        matrix = builder.mMatrix;

        view.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    /*
     * Initializes this FancyBackground. Invoked after the source view has been
     * measured.
     */
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
     * Returns the index of currently shown Drawable resource.
     *
     * @return the index of currently shown Drawable resource.
     */
    public final int getCurrentDrawableIndex() {
        /*
         * Because the mIndex++ is used in the getNext() method,
         * the current index is one less than mIndex.
         */
        return mIndex - 1;
    }

    /**
     * Returns the number of set Drawables.
     *
     * @return the number of set Drawables.
     */
    public final int getDrawablesCount() {
        return mDrawables.length;
    }

    /**
     * Stops the looping and releases the cached resources, if any.
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
        //if (mIndex.get() >= size && !loop) {
        if (mIndex >= size && !loop) {
            drawable = null;
            halt(true);
        } else {
            //final int index = mIndex.getAndIncrement();
            //drawable = getDrawable(mDrawables[index % size]);
            drawable = getDrawable(mDrawables[mIndex++ % size]);
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
        final boolean hasCache = (null != cache);
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
                throw new IllegalArgumentException("not a Drawable id: " +
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