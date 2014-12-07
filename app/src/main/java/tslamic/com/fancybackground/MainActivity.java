package tslamic.com.fancybackground;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
                .batch(5)
                .animator(new Anim())
                .listener(this)
                .interval(3000)
                .set(R.drawable.square,
                        R.drawable.bg_1,
                        R.drawable.bg_2,
                        R.drawable.ic_launcher,
                        R.drawable.train_pink,
                        R.drawable.blue)
                .start();
    }

    @Override
    public void onStarted(FancyBackground bg) {
        Log.d(TAG, "onStarted");
        Toast.makeText(this, "onStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNew(FancyBackground bg, Drawable current) {
        Log.d(TAG, "onNew");
        Toast.makeText(this, "onNew", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopped(FancyBackground bg) {
        Log.d(TAG, "onStopped");
        Toast.makeText(this, "onStopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopDone(FancyBackground bg) {
        Log.d(TAG, "onLoopDone");
        Toast.makeText(this, "onLoopDone", Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static class Anim implements FancyAnimator {
        @Override
        public void animate(final FancyBackground fb, final ImageView source,
                            final Drawable next) {
            source.animate().alpha(0).setDuration(333)
                    .setListener(new AnimListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            source.setImageDrawable(next);
                            source.animate().alpha(1).setDuration(333);
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

}