package tslamic.com.fancybackground;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;

import tslamic.com.fancybg.FancyBackground;
import tslamic.com.fancybg.FancyScale;


public class MainActivity extends ActionBarActivity implements FancyBackground.FancyListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final View view = findViewById(R.id.view);

        FancyBackground
                .on(view)
                .listener(this)
                .scale(FancyScale.CENTER)
                .set(R.drawable.bg_1, R.drawable.bg_2, R.drawable.train_pink, R.drawable.ic_launcher, R.drawable.square)
                .build();
    }

    @Override
    public void onStarted(FancyBackground bg) {
        Toast.makeText(this, "onStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNew(FancyBackground bg, Drawable newDrawable) {
        Toast.makeText(this, "onNew", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopped(FancyBackground bg) {
        Toast.makeText(this, "onStopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopDone(FancyBackground bg) {
        Toast.makeText(this, "onLoopDone", Toast.LENGTH_SHORT).show();
    }

}