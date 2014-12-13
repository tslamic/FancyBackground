package tslamic.com.fancybackground;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import tslamic.com.fancybg.FancyAnimator;
import tslamic.com.fancybg.FancyBackground;


public class MainActivity extends ActionBarActivity implements FancyBackground.FancyListener {

    private static final String TAG = "FANCYBG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View view = findViewById(R.id.view);
        FancyBackground
                .on(view)
                .animator(new Anim())
                .listener(this)
                .interval(3000)
                .set(R.drawable.fancy_bg_0, R.drawable.fancy_bg_1, R.drawable.fancy_bg_2)
                .loop(false)
                .start();
    }

    @Override
    public void onStarted(FancyBackground bg) {
        checkIfMainThread();
        Log.d(TAG, "onStarted");
    }

    @Override
    public void onNew(FancyBackground bg) {
        checkIfMainThread();
        Log.d(TAG, "onNew");
    }

    @Override
    public void onStopped(FancyBackground bg) {
        checkIfMainThread();
        Log.d(TAG, "onStopped");
    }

    @Override
    public void onLoopDone(FancyBackground bg) {
        checkIfMainThread();
        Log.d(TAG, "onLoopDone");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static class Anim implements FancyAnimator {
        @Override
        public void animate(final FancyBackground fb, final ImageView source,
                            final Drawable next) {
            final AccelerateInterpolator interpolator =
                    new AccelerateInterpolator();
            source.animate()
                    .setInterpolator(interpolator)
                    .alpha(0)
                    .setDuration(123)
                    .setListener(new AnimListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            source.setImageDrawable(next);
                            source.animate().alpha(1).setDuration(123)
                                    .setInterpolator(interpolator);
                        }
                    });

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class AnimListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private void checkIfMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AssertionError("not on main thread");
        }
    }

}