package learn.bobo.com.video;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class PreviewActivity extends Activity {
	private Camera mCamera;
	private Preview mPreview;
	private int numberOfCameras;
	private int defaultCameraId;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mPreview = new Preview(this);
        setContentView(mPreview);

		//找到可用的相机数量
		numberOfCameras = Camera.getNumberOfCameras();

		// 查找默认相机的标识
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
    }

	@Override
	protected void onResume() {
		super.onResume();
		safeCameraOpen(1);
	}

	private boolean safeCameraOpen(int id) {
		boolean qOpened = false;
		try {
			releaseCameraAndPreview();
			Camera camera = Camera.open();
			mPreview.setCamera(camera);
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
}