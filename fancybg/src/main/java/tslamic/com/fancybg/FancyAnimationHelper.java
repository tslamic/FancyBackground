package tslamic.com.fancybg;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public enum FancyAnimationHelper implements FancyAnimation {

    FADE {
        @Override
        public void animate(final FancyBackground fb,
                            final ImageView source,
                            final Drawable next) {
            source.animate().alpha(0).setDuration(333)
                    .setListener(new FancyAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            source.setImageDrawable(next);
                            source.animate().alpha(1).setDuration(333);
                        }
                    });
        }
    };

    private static class FancyAnimatorListener implements Animator.AnimatorListener {
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