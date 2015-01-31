package tslamic.fancybg;

import android.graphics.Matrix;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageView;

import junit.framework.Assert;

public class FancyBuilderTest extends AndroidTestCase {

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
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(null);
            }
        });
    }

    public void testScaleIsNull() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                final ImageView.ScaleType scale = null;
                FancyBackground.on(mSource).scale(scale);
            }
        });
    }

    public void testMatrixIsNull() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                final Matrix matrix = null;
                FancyBackground.on(mSource).scale(matrix);
            }
        });
    }

    public void testInAnimationIsNull() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(mSource).inAnimation(null);
            }
        });
    }

    public void testOutAnimationIsNull() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(mSource).outAnimation(null);
            }
        });
    }

    public void testInterval() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(mSource).interval(-1);
            }
        });
    }

    public void testDrawablesNull() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(mSource).set(null).start();
            }
        });
    }

    public void testDrawablesCount() throws Exception {
        assertArgsException(new Runnable() {
            @Override
            public void run() {
                FancyBackground.on(mSource).set(R.drawable.ic_launcher).start();
            }
        });
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

    private static void assertArgsException(Runnable runnable) {
        IllegalArgumentException exception = null;
        try {
            runnable.run();
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        Assert.assertNotNull(exception);
    }

    private static void assertUiThread() {
        Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
    }

}