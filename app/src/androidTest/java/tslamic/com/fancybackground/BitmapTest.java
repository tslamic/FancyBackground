package tslamic.com.fancybackground;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BitmapTest extends TestCase {

    public static int calculateInSampleSize(int w, int h, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (h > reqHeight || w > reqWidth) {

            final int halfHeight = h / 2;
            final int halfWidth = w / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int calculateInSampleSizeTadej(int w, int h, int reqWidth, int reqHeight) {
        final double factor = 2 * w / reqWidth;
        return (int) Math.ceil(Math.log(factor) / Math.log(2));
    }

    public void testVals() {
        int sample, tadej;
        for (int i = 1000; i < 10000; i++) {
            for (int j = 100; j < 1000; j++) {
                sample = calculateInSampleSize(i, i, j, j);
                tadej = calculateInSampleSizeTadej(i, i, j, j);
                Assert.assertEquals(sample, tadej);
            }
        }
    }

}