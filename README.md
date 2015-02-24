# FancyBackground

FancyBackground is a tiny Android library designed to animate a set of resource Drawables.  It ensures the drawables are subsampled and cached, if necessary, with heavy lifting done in the background.

Before  | After 
:-----------:|:-----------:
![plain](http://i.imgur.com/7kH0FIN.png?1) | ![fancybg](http://i.imgur.com/Sh4XegD.gif)

Achieving the above is easy:

```
FancyBackground.on(view)
               .set(R.drawable.fst, R.drawable.snd, R.drawable.trd)
               .inAnimation(R.anim.fade_in)
               .outAnimation(R.anim.fade_out)
               .interval(2500)
               .start(); 
```

Don't forget to add the following in your `build.gradle`:

```
dependencies {
	compile 'com.github.tslamic.fancybackground:library:1.0'
}
```

### Builder options

Method name | Description
-----------:|:-----------
`set` | sets the Drawable resources we wish to show/animate
`inAnimation` | specifies the animation used to animate a `View` entering the screen.
`outAnimation` | specifies the animation used to animate a `View` exiting the screen.
`loop` | continuously loop through the Drawables or stop after the first cycle is complete.
`interval` | the millisecond interval a Drawable instance will be displayed for.
`scale` | determines how the Drawables should be resized or moved to match the size of the view we're animating on.
`listener` | receives the `FancyBackground` events (described below)
`cache` | caches loaded bitmaps so we don't have to do it again

`FancyListener` can receive four events: 

- `onStarted` when the FancyBackground is started 
- `onNew` when a new image is set
- `onLoopDone` if looping is set to false and the first cycle is complete
- `onStopped` when the FancyBackground stops.

`FancyCache` enables you to create your own bitmap cache. `FancyLruCache` is the default, targeting ~25% of the available heap and evicting the least recently used bitmap if over capacity. Use `null` to avoid caching.

### An example?

See `app` for a hands-on example.

### How to get it?

Gradle

```
dependencies {
	compile 'com.github.tslamic.fancybackground:library:1.0'
}
```

Maven

```
<dependency>
    <groupId>com.github.tslamic.fancybackground</groupId>
    <artifactId>library</artifactId>
    <version>1.0</version>
</dependency>
```

or download the aar by [clicking here](http://search.maven.org/remotecontent?filepath=com/github/tslamic/fancybackground/library/1.0/library-1.0.aar).

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
