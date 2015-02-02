package tslamic.fancybg.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import tslamic.fancybackground.R;
import tslamic.fancybg.FancyBackground;


public class MainActivity extends Activity implements FancyBackground.FancyListener {

    private static final String TAG = "FancyBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View view = findViewById(R.id.parent);

        final int[] drawables = {
                R.drawable.fbg_fst,
                R.drawable.fbg_snd,
                R.drawable.fbg_trd
        };

        FancyBackground.on(view)
                .set(drawables)
                .inAnimation(R.anim.fade_in)
                .outAnimation(R.anim.fade_out)
                .interval(2500)
                .scale(ImageView.ScaleType.CENTER_CROP)
                .listener(this)
                .start();
    }

    @Override
    public void onStarted(FancyBackground bg) {
        Log.d(TAG, "Started FancyBackground.");
    }

    @Override
    public void onNew(FancyBackground bg) {
        Log.d(TAG, "New pic loaded.");
    }

    @Override
    public void onStopped(FancyBackground bg) {
        Log.d(TAG, "Stopped FancyBackground.");
    }

    @Override
    public void onLoopDone(FancyBackground bg) {
        Log.d(TAG, "Loop complete.");
    }

}