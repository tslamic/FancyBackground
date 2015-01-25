package tslamic.fancybg;

import android.os.Looper;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageView;

import junit.framework.Assert;

public class FancyBackgroundTest extends AndroidTestCase {

    private View mSource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mSource = new View(getContext());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mSource = null;
    }

    public void testSanity() throws Exception {
        Assert.assertNotNull(mSource);
    }

    public void testViewIsNull() throws Exception {
        NullPointerException npe = null;
        try {
            FancyBackground.on(null);
        } catch (NullPointerException e) {
            npe = e;
        }
        Assert.assertNotNull(npe);
    }

    public void testViewNotNull() throws Exception {
        IllegalStateException ise = null;

        try {
            FancyBackground.on(mSource).start();
        } catch (IllegalStateException e) {
            ise = e;
        }
        Assert.assertNotNull(ise);

        ise = null;
        try {
            FancyBackground.on(mSource).set(R.drawable.ic_launcher).start();
        } catch (IllegalStateException e) {
            ise = e;
        }
        Assert.assertNotNull(ise);
    }

    public void testInterval() throws Exception {
        IllegalArgumentException iae = null;
        try {
            FancyBackground.on(mSource)
                    .set(R.drawable.ic_launcher, R.drawable.ic_launcher)
                    .interval(-1)
                    .start();
        } catch (IllegalArgumentException e) {
            iae = e;
        }
        Assert.assertNotNull(iae);
    }

    public void testScale() throws Exception {
        final ImageView.ScaleType scaleType = null;

        IllegalArgumentException iae = null;
        try {
            FancyBackground.on(mSource)
                    .set(R.drawable.ic_launcher, R.drawable.ic_launcher)
                    .scale(scaleType)
                    .start();
        } catch (IllegalArgumentException e) {
            iae = e;
        }
        Assert.assertNotNull(iae);
    }

    public void testListener() throws Exception {
        final TestListener listener = new TestListener();
        FancyBackground.on(mSource)
                .set(R.drawable.ic_launcher, R.drawable.ic_launcher)
                .interval(1)
                .listener(listener)
                .loop(false)
                .start();
    }

    private static class TestListener implements FancyBackground.FancyListener {

        boolean onStartedInvoked = false;
        boolean onNewInvoked = false;
        boolean onLoopDoneInvoked = false;
        boolean onStoppedInvoked = false;

        @Override
        public void onStarted(FancyBackground bg) {
            assertUiThread();
            onStartedInvoked = true;
        }

        @Override
        public void onNew(FancyBackground bg) {
            assertUiThread();
            onNewInvoked = true;
        }

        @Override
        public void onLoopDone(FancyBackground bg) {
            assertUiThread();
            onLoopDoneInvoked = true;
        }

        @Override
        public void onStopped(FancyBackground bg) {
            assertUiThread();
            onStoppedInvoked = true;

            Assert.assertTrue(onStartedInvoked);
            Assert.assertTrue(onNewInvoked);
            Assert.assertTrue(onLoopDoneInvoked);
            Assert.assertTrue(onStoppedInvoked);
        }

    }

    private static void assertUiThread() {
        Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
    }

}