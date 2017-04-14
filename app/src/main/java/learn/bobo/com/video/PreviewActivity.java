package learn.bobo.com.video;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewActivity extends Activity {
    @BindView(R.id.sv_camera)
    Preview mPreview;
    private Camera mCamera;
    private int numberOfCameras;
    private int defaultCameraId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		mTextureView = new Preview(this);
        setContentView(R.layout.preview);
        ButterKnife.bind(this);
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

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCameraAndPreview();
            Camera camera = Camera.open();
            mCamera = camera;

            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size mPreviewSize = getNearestRatioSize(parameters, screenWidth,
                    screenHeight);
            mPreview.setPreviewSize(mPreviewSize);

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

    @OnClick(R.id.take_pic)
    public void takePic() {
//        Toast.makeText(this, "拍照", Toast.LENGTH_SHORT).show();
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
//                releaseCameraAndPreview();
                MainActivity.pic = bytes;
//                Intent intent = new Intent();
//                intent.putExtra("pic",bytes);
//                setResult(100,intent);
//                finish();
            }
        });
    }

    public static Camera.Size getNearestRatioSize(Camera.Parameters para,
                                                  final int screenWidth, final int screenHeight) {
        List<Camera.Size> supportedSize = para.getSupportedPreviewSizes();
        for (Camera.Size tmp : supportedSize) {
            if (tmp.width == 1280 && tmp.height == 720) {
                return tmp;
            }
        }
        Collections.sort(supportedSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int diff1 = (((int) ((1000 * (Math.abs(lhs.width
                        / (float) lhs.height - screenWidth
                        / (float) screenHeight))))) << 16)
                        - lhs.width;
                int diff2 = (((int) (1000 * (Math.abs(rhs.width
                        / (float) rhs.height - screenWidth
                        / (float) screenHeight)))) << 16)
                        - rhs.width;

                return diff1 - diff2;
            }
        });

        return supportedSize.get(0);
    }
}