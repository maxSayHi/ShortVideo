package learn.bobo.com.video;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TexureActivity extends Activity implements
        TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    @BindView(R.id.tv_camera)
    TextureView mTextureView;
    private Camera mCamera;
    private int numberOfCameras;
    private int defaultCameraId;
    private boolean mHasSuccess = false;
    private Camera.Size mBestPreviewSize = null;
    private boolean mHasSurface = false;
    private int Angle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		mTextureView = new Preview(this);
        setContentView(R.layout.preview_texureview);
        ButterKnife.bind(this);

        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });

    }

    private void autoFocus() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHasSuccess = false;

        // mHandler.postDelayed(new Runnable() {
        // @Override
        // public void run() {
        // autoFocus();
        // }
        // }, 1500);
        mCamera = Camera.open(0);
        setAndLayout();
        //手动开启扫描页面  解决请求权限成功后不跳转的问题
        if (mTextureView.isAvailable()) {
            onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void startPreview() {
        if (mHasSurface && mCamera != null) {
            try {
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
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

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        mHasSurface = true;
        try {
            startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mHasSurface = false;
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    private void setAndLayout() {
        if (mCamera == null)
            return;

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            mBestPreviewSize = getNearestRatioSize(parameters, screenWidth,
                    screenHeight);
            int cameraWidth = mBestPreviewSize.width;
            int cameraHeight = mBestPreviewSize.height;
            parameters.setPreviewSize(cameraWidth, cameraHeight);
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters
                        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            // Rect rect = new Rect();
            // rect.left = (int) ((mIndicatorView.CONTENT_RATIO * -1000) /
            // mIndicatorView.IDCARD_RATIO);
            // rect.top = (int) (mIndicatorView.CONTENT_RATIO * -1000);
            // rect.right = (int) ((mIndicatorView.CONTENT_RATIO * 1000) /
            // mIndicatorView.IDCARD_RATIO);
            // rect.bottom = (int) (mIndicatorView.CONTENT_RATIO * 1000);
            // Camera.Area area = new Camera.Area(rect, 1000);
            // ArrayList<Area> focusAreas = new ArrayList<Area>();
            // focusAreas.add(area);
            // parameters.setFocusAreas(focusAreas);
            Angle = getCameraAngle();
            mCamera.setDisplayOrientation(Angle);
            mCamera.setParameters(parameters);

            float scale = Math.min(screenWidth * 1.0f / mBestPreviewSize.height,
                    screenHeight * 1.0f / mBestPreviewSize.width);
//            int layout_width = (int) (scale * mBestPreviewSize.height);
//            int layout_height = (int) (scale * mBestPreviewSize.width);
            int layout_width = screenWidth;
            int layout_height = screenHeight;
            RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                    layout_width, layout_height);
            layout_params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mTextureView.setLayoutParams(layout_params);
//            mIndicatorView.setLayoutParams(layout_params);
//            mIDCardIndicator.setLayoutParams(layout_params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取照相机旋转角度
     */
    public int getCameraAngle() {
        int rotateAngle = 90;
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        degrees = 90;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotateAngle = (info.orientation + degrees) % 360;
            rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
        } else { // back-facing
            rotateAngle = (info.orientation - degrees + 360) % 360;
        }
        return rotateAngle;
    }
}