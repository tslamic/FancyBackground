package tslamic.fancybg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import junit.framework.Assert;

public class FancyLruCacheTest extends AndroidTestCase {

    private FancyLruCache mCache;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mCache = new FancyLruCache(getContext());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mCache = null;
    }

    public void testSanity() throws Exception {
        Assert.assertNotNull(mCache);
        Assert.assertTrue(mCache.getMaxSize() > 0);
        Assert.assertEquals(mCache.getSize(), 0);
    }

    public void testPutGetClear() throws Exception {
        final Bitmap b = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher);
        final int size = b.getRowBytes() * b.getHeight();
        final int max = mCache.getMaxSize() / size;

        int i;
        for (i = 0; i < max; i++) {
            Assert.assertTrue(mCache.put(i, b));
            Assert.assertEquals(mCache.getSize(), (i + 1) * size);
        }
        i += 1;
        mCache.put(i, b);
        Assert.assertNull(mCache.get(0));

        mCache.clear();
        Assert.assertEquals(mCache.getSize(), 0);
        Assert.assertTrue(b.isRecycled());
    }

}