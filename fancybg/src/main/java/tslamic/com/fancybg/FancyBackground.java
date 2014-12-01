package tslamic.com.fancybg;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
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
        return new Builder(view);
    }

    public static class Builder {

        private final View view;

        private FancyAnimation animation = FancyAnimation.FADE;
        private FancyScale scale = FancyScale.FIT_XY;
        private long interval = 2500;
        private FancyListener listener;
        private boolean loop = true;
        private int[] drawables;

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

        public Builder interval(long duration) {
            this.interval = duration;
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

        public FancyBackground build() {
            return new FancyBackground(this);
        }

    }

    public final FancyAnimation animation;
    public final FancyListener listener;
    public final FancyScale scale;
    public final long interval;
    public final boolean loop;
    public final View view;

    private final BitmapFactory.Options mOptions;
    private final TypedValue mTypedValue;

    private ScheduledExecutorService mExecutor;
    private FancyImageView mFancyImage;
    private final int[] mDrawables;
    private int mNextIndex;

    private static Bitmap sCacheBitmap;

    private FancyBackground(Builder builder) {
        animation = builder.animation;
        listener = builder.listener;
        scale = builder.scale;
        interval = builder.interval;
        loop = builder.loop;
        view = builder.view;

        mDrawables = builder.drawables;
        mOptions = new BitmapFactory.Options();
        mTypedValue = new TypedValue();

        view.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    void halt() {
        if (null != listener) {
            listener.onStopped(this);
        }
        shutdownExecutor();
    }

    private void init() {
        final ViewGroup group = getViewGroup(view);
        mFancyImage = new FancyImageView(this, view);
        group.addView(mFancyImage, 0, view.getLayoutParams());
        start();
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
            if (null != listener) listener.onNew(this, drawable);
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

        mNextIndex = (mNextIndex + 1) % size;
        final int resource = mDrawables[mNextIndex];

        final Resources resources = view.getResources();
        resources.getValue(resource, mTypedValue, true);

        final String file = mTypedValue.string.toString();
        ensureDrawable(resource, file);

        final Drawable drawable;
        if (file.endsWith(".xml")) {
            drawable = resources.getDrawable(resource);
        } else {
            drawable = getBitmapDrawable(view, mOptions, resource);
        }

        return drawable;
    }

    private static Drawable getBitmapDrawable(View source,
                                              BitmapFactory.Options options,
                                              int resource) {
//        if (null != sCacheBitmap) {
//            sCacheBitmap.recycle();
//        }

        final Resources resources = source.getResources();

        final int w = source.getMeasuredWidth();
        final int h = source.getMeasuredHeight();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resource, options);

        options.inSampleSize = getSampleSize(options, w, h);
        options.inJustDecodeBounds = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(resources,
                resource, options);

        final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
        bitmap.recycle();
        return drawable;


//        sCacheBitmap = BitmapFactory.decodeResource(resources,
//                resource, options);

        //return new BitmapDrawable(resources, bitmap);
    }

    private void shutdownExecutor() {
        if (null != mExecutor) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    private static void ensureDrawable(int resource, String filename) {
        if (TextUtils.isEmpty(filename)) {
            throw new IllegalArgumentException("resource not a Drawable: " +
                    resource);
        }
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