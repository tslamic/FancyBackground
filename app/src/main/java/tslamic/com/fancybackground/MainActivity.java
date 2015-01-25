package tslamic.com.fancybackground;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import tslamic.com.fancybg.FancyBackground;
import tslamic.com.fancybg.FancyLruCache;


public class MainActivity extends ActionBarActivity implements FancyBackground.FancyListener {

    private static final String TAG = "FancyBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View view = findViewById(R.id.view);
        FancyBackground
                .on(view)
                .listener(this)
                .interval(3000)
                .set(R.drawable.fancy_bg_0, R.drawable.fancy_bg_1, R.drawable.fancy_bg_2)
                .inAnimation(android.R.anim.fade_in)
                .outAnimation(android.R.anim.fade_out)
                .cache(new FancyLruCache(this, .79f))
                .scale(ImageView.ScaleType.FIT_XY)
                .start();

//        final View colors = findViewById(R.id.colors);
//        FancyBackground
//                .on(colors)
//                .interval(1500)
//                .set(R.drawable.easy_blue, R.drawable.red, R.drawable.blue)
//                .start();
    }

    @Override
    public void onStarted(FancyBackground bg) {
        Log.d(TAG, "onStarted");
    }

    @Override
    public void onNew(FancyBackground bg) {
        Log.d(TAG, "onNew");
    }

    @Override
    public void onStopped(FancyBackground bg) {
        Log.d(TAG, "onStopped");
    }

    @Override
    public void onLoopDone(FancyBackground bg) {
        Log.d(TAG, "onLoopDone");
    }

    private Animation inAnim() {
        final AnimationSet set = new AnimationSet(false);

        final Animation fade = new AlphaAnimation(1, 0);
        set.addAnimation(fade);

        final Animation shrink = new ScaleAnimation(1, 0, 1, 0);
        set.addAnimation(shrink);

        set.setDuration(500);
        return set;
    }

}