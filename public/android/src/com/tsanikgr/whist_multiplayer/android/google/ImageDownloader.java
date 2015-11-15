package com.tsanikgr.whist_multiplayer.android.google;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.badlogic.gdx.Gdx;
import com.google.android.gms.common.images.ImageManager;
import com.tsanikgr.whist_multiplayer.IImageBytesLoadedInterface;
import com.tsanikgr.whist_multiplayer.android.AndroidLauncher;
import com.tsanikgr.whist_multiplayer.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

class ImageDownloader implements ImageManager.OnImageLoadedListener {
	private final Log log = new Log(this);
	private final AndroidLauncher launcher;
	private final HashMap<Uri,ImageParameters> pendingImages = new HashMap<>();
	class ImageParameters {
		private final int height, width;
		final IImageBytesLoadedInterface listener;
		ImageParameters(IImageBytesLoadedInterface listener, int width, int height) {
			this.listener = listener;
			this.width = width;
			this.height = height;
		}
	}

	ImageDownloader(AndroidLauncher launcher) {
		this.launcher = launcher;
	}

	public void downloadImage(final Uri uri, IImageBytesLoadedInterface listener, int imageWidth, int imageHeight) {
		pendingImages.put(uri, new ImageParameters(listener,imageWidth,imageHeight));
		launcher.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ImageManager.create(launcher).loadImage(ImageDownloader.this, uri);
			}
		});
	}

	@Override
	public void onImageLoaded(final Uri uri, final Drawable drawable, boolean isNotPlaceholder) {
		try {
			if (!isNotPlaceholder) return;
			new Thread(new Runnable() {
				@Override
				public void run() {
					final ImageParameters params = pendingImages.remove(uri);
					BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
					Bitmap bitmap = bitmapDrawable.getBitmap();
					if (params.width != -1 || params.height != -1) {
						int reqWidth, reqHeight;
						if (params.width != -1 && params.height != -1) {
							reqWidth = params.width;
							reqHeight = params.height;
						} else if (params.width != -1){
							reqWidth = params.width;
							reqHeight = bitmap.getHeight() * reqWidth / bitmap.getWidth();
						} else {
							reqHeight = params.height;
							reqWidth = bitmap.getWidth() * reqHeight / bitmap.getHeight();
						}
						bitmap = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
					}
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					final byte[] bytes = stream.toByteArray();
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							params.listener.onImageLoaded(bytes);
						}
					});
				}
			}).start();
		} catch (Exception e) {
			log.e(e).print();
		}
	}
}
