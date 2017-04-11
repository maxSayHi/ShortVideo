package learn.bobo.com.video;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RecordActivity extends Activity {
    private SurfaceView surfaceView;
    private RelativeLayout layout;
    private Button recordbutton;
    private Button stopbutton;
    private MediaRecorder mediaRecorder;
	private Camera mCamera;
	private Preview mPreview;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(176, 144);
        surfaceView.getHolder().setKeepScreenOn(true);
        
        layout = (RelativeLayout) this.findViewById(R.id.layout);
        recordbutton = (Button) this.findViewById(R.id.recordbutton);
        stopbutton = (Button) this.findViewById(R.id.stopbutton);

//		mPreview = new Preview(this);
//		safeCameraOpen(0);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			layout.setVisibility(ViewGroup.VISIBLE);
		}
		return super.onTouchEvent(event);
	}
    
    public void record(View v){
    	switch (v.getId()) {
		case R.id.recordbutton:
			try{
				File videoFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()+ "测试视频.mp4");
				mediaRecorder = new MediaRecorder();
				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mediaRecorder.setVideoSize(960, 544);
				mediaRecorder.setVideoFrameRate(10);
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
				mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
				mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
				mediaRecorder.prepare();
				mediaRecorder.start();
			}catch (Exception e) {
				e.printStackTrace();
			}
			recordbutton.setEnabled(false);
			stopbutton.setEnabled(true);
			break;
		case R.id.stopbutton:
			if(mediaRecorder!=null){
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
			}
			recordbutton.setEnabled(true);
			stopbutton.setEnabled(false);
			break;
		}
    }

	private boolean safeCameraOpen(int id) {
		boolean qOpened = false;
		try {
			releaseCameraAndPreview();
			mCamera = Camera.open(id);
			mPreview.setCamera(mCamera);
			qOpened = (mCamera != null);
		} catch (Exception e) {
			Log.e(getString(R.string.app_name), "failed to open Camera");
			e.printStackTrace();
		}
		return qOpened;
	}

	private void releaseCameraAndPreview() {
		mPreview.setCamera(null);
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	class Preview extends ViewGroup implements SurfaceHolder.Callback {

		SurfaceView mSurfaceView;
		SurfaceHolder mHolder;

		Preview(Context context) {
			super(context);

			mSurfaceView = new SurfaceView(context);
			addView(mSurfaceView);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = mSurfaceView.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder surfaceHolder) {

		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			// Now that the size is known, set up the camera parameters and begin
			// the preview.
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(1080, 1920);
			requestLayout();
			mCamera.setParameters(parameters);

			// Important: Call startPreview() to start updating the preview surface.
			// Preview must be started before you can take a picture.
			mCamera.startPreview();
		}


		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return, so stop the preview.
			if (mCamera != null) {
				// Call stopPreview() to stop updating the preview surface.
				mCamera.stopPreview();
			}
		}

		public void setCamera(Camera camera) {
			if (mCamera == camera) { return; }

			stopPreviewAndFreeCamera();

			mCamera = camera;

			if (mCamera != null) {
				List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
//				mSupportedPreviewSizes = localSizes;
				requestLayout();

				try {
					mCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Important: Call startPreview() to start updating the preview
				// surface. Preview must be started before you can take a picture.
				mCamera.startPreview();
			}
		}
	}

	/**
	 * When this function returns, mCamera will be null.
	 */
	private void stopPreviewAndFreeCamera() {

		if (mCamera != null) {
			// Call stopPreview() to stop updating the preview surface.
			mCamera.stopPreview();

			// Important: Call release() to release the camera for use by other
			// applications. Applications should release the camera immediately
			// during onPause() and re-open() it during onResume()).
			mCamera.release();

			mCamera = null;
		}
	}



}