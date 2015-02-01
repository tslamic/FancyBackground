package tslamic.com.fancybackground;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import tslamic.fancybg.FancyBackground;


public class MainActivity extends Activity implements FancyBackground.FancyListener {

    private static final String TAG = "FancyBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View view = findViewById(R.id.parent);
        FancyBackground
                .on(view)
                .listener(this)
                .interval(2500)
                .set(R.drawable.fbg_fst, R.drawable.fbg_snd, R.drawable.fbg_trd)
                .inAnimation(R.anim.fade_in)
                .outAnimation(R.anim.fade_out)
                .scale(ImageView.ScaleType.CENTER_CROP)
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