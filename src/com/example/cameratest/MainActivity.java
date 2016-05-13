package com.example.cameratest;

import java.io.IOException;

import com.example.kakalibtest.R;
import com.google.zxing.client.android.camera.CameraManager;

import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private SurfaceView previewView;
	private CameraManager cameraManager;
	private boolean hasSurface;

	public void onResume() {
		super.onResume();
		cameraManager = new CameraManager(getApplicationContext());
		final SurfaceHolder surfaceHolder = previewView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCameraAndStartPreview(surfaceHolder);
		} else {
			surfaceHolder.addCallback(surfaceHolderCallback);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

	}

	Camera theCamera;

	@Override
	protected void onPause() {
		super.onPause();
		closeCameraDriver();
	}

	private void closeCameraDriver() {
		if (cameraManager != null && cameraManager.isOpen()) {
			cameraManager.stopPreview();
			cameraManager.closeDriver();
		}
	}

	private void setStatus(String textString, int color) {
		TextView textView = (TextView) findViewById(R.id.status);
		textView.setTextColor(color);
		textView.setText(textString);
	}

	private void initCameraAndStartPreview(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager == null) {
			cameraManager = new CameraManager(this);
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
		if (cameraManager.isOpen()) {
			try {
				cameraManager.startPreviewRetry();
			} catch (Exception e) {
				decodeResultHandler.showOpenCameraErrorDialog();
			}
		}
		startReceptFramePreviewCallback();
	}

	void startReceptFramePreviewCallback() {
		if (cameraManager != null && cameraManager.isOpen()) {
			requestOneFramePreviewCallback();
		}
	}

	SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (!hasSurface) {
				hasSurface = true;
			}
			initCameraAndStartPreview(holder);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			hasSurface = false;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		}
	};

	private void requestOneFramePreviewCallback() {
		cameraManager.requestPreviewFrame(previewCallback);
	}

}
