package tslamic.fancybg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import junit.framework.Assert;

public class FancyLruCacheTest extends AndroidTestCase {

    private FancyLruCache mCache;
    private Bitmap mBitmap;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mCache = new FancyLruCache(getContext());
        mBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mCache.clear();
        mCache = null;

        mBitmap.recycle();
        mBitmap = null;
    }

    public void testSanity() throws Exception {
        Assert.assertNotNull(mCache);
        Assert.assertNotNull(mBitmap);
    }

    public void testInitialValues() throws Exception {
        Assert.assertTrue(mCache.getMaxSize() > 0);
        Assert.assertEquals(mCache.getSize(), 0);
    }

    public void testPut() throws Exception {
        final int size = mBitmap.getRowBytes() * mBitmap.getHeight();
        mCache.put(0, mBitmap);

        Assert.assertEquals(mCache.getSize(), size);
    }

    public void testGet() throws Exception {
        mCache.put(0, mBitmap);
        final Bitmap get = mCache.get(0);

        Assert.assertEquals(mBitmap, get);
    }

    public void testClear() throws Exception {
        mCache.put(0, mBitmap);
        mCache.clear();

        Assert.assertNull(mCache.get(0));
        Assert.assertEquals(mCache.getSize(), 0);
        Assert.assertTrue(mBitmap.isRecycled());
    }

}