# FancyBackground

FancyBackground is an Android library capable of memory-efficiently loading and animating through a set of resource Drawables. 

### How?

It's simple: 

```java
FancyBackground.on(view)
               .set(R.drawable.fst, R.drawable.snd) // and many more
               .inAnimation(R.anim.fade_in)
               .outAnimation(R.anim.fade_out)
               .interval(2500)
               .scale(ImageView.ScaleType.CENTER_CROP)
               .listener(this)
               .start();
```

The above code will start animating drawables on a `view`, showing each for `2500` milliseconds, then crossfading to the next one. Drawables can be as big as the `view` they're animating on - scale describes how to resize or move them around.

### Memory-efficient?

FancyBackground ensures the drawables are subsampled and cached, if need be, with all the heavy lifting done in the background.

So really, all you need is your favorite drawables in the `drawable-nodpi` folder and you're good to go. 

### License

The MIT License (MIT)

Copyright (c) 2015 Tadej Slamic

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.