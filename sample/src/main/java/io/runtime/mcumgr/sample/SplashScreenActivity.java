/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

public class SplashScreenActivity extends Activity {
	private static final int DURATION = 1500;

	public String getFileName(Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			try {
				if (cursor != null && cursor.moveToFirst()) {
					result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			} finally {
				cursor.close();
			}
		}
		if (result == null) {
			result = uri.getPath();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

	public static String getImagePathFromURI(Context context, Uri uri) {
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		String path = null;
		if (cursor != null) {
			cursor.moveToFirst();
			String document_id = cursor.getString(0);
			document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
			cursor.close();

			cursor = context.getContentResolver().query(
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
			if (cursor != null) {
				cursor.moveToFirst();
				path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
				cursor.close();
			}
		}
		Log.d("tag","getImagePathFromURI " + path);
		return path;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		/*
		Intent intent = getIntent();
		String action = intent.getAction();

		if (action.compareTo(Intent.ACTION_VIEW) == 0) {
			String scheme = intent.getScheme();
			ContentResolver resolver = getContentResolver();

			if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0) {
				Uri uri = intent.getData();
				String name = getFileName(uri);
				//String path = getImagePathFromURI(getApplicationContext(), uri);

				Log.v("tag" , "Content intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
//				InputStream input = resolver.openInputStream(uri);
//				String importfilepath = "/sdcard/My Documents/" + name;
//				InputStreamToFile(input, importfilepath);
			}
			else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {
				Uri uri = intent.getData();
				String name = uri.getLastPathSegment();

				Log.v("tag" , "File intent detected: " + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
//				InputStream input = resolver.openInputStream(uri);
//				String importfilepath = "/sdcard/My Documents/" + name;
//				InputStreamToFile(input, importfilepath);
			}
			else if (scheme.compareTo("http") == 0) {
				// TODO Import from HTTP!
			}
			else if (scheme.compareTo("ftp") == 0) {
				// TODO Import from FTP!
			}
		}
*/

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		new Handler().postDelayed(() -> {
			final Intent intent2 = new Intent(this, ScannerActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent2);
			finish();
		}, DURATION);
	}

	@Override
	public void onBackPressed() {
		// We don't want the splash screen to be interrupted
	}
}
