package learn.bobo.com.video;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PreviewActivity extends Activity implements
        TextureView.SurfaceTextureListener, Camera.PreviewCallback{
    private Object mLoadingDialog;
    private Object mDialogNew;
    private String userToken;
    private int REQUEST_PERMISSIONS = 100;
    private int mStatus = 1;//0正常状态  1正在请求权限  3已弹出提示框 4授权失败
    TextureView mTextureView;
    private Camera mCamera;
    private Camera.Size mBestPreviewSize = null;
    private boolean mHasSurface = false;
    private int Angle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userToken = getIntent().getStringExtra("token");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		mPreview = new Preview(this);
        setContentView(R.layout.preview);
        mTextureView = (TextureView) findViewById(R.id.tv_preview);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });

        findViewById(R.id.take_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mStatus == 1) {
            requestBlePermission();
        } else if (mStatus == 3 || mStatus == 4) {
            return;
        } else {
            // mHandler.postDelayed(new Runnable() {
            // @Override
            // public void run() {
            // autoFocus();
            // }
            // }, 1500);
            mCamera = Camera.open();
            setAndLayout();
            //手动开启扫描页面  解决请求权限成功后不跳转的问题
            if (mTextureView.isAvailable()) {
                onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
            }
        }
    }

    /**
     * 解决6.0权限问题
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestBlePermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {//已经授权相机权限
            mStatus = 0;
            onResume();
        } else {//没有授权,申请相机权限
            mStatus = 1;
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //请求权限
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//同意权限
                mStatus = 0;
            } else {
                showPermisionDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            if(mStatus==0){
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
            }
            mCamera = null;
        }
    }

    public void takePic() {
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {

            }
        }, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
//                releaseCameraAndPreview();
//                MainActivity.pic = bytes;
//                Intent intent = new Intent();
//                intent.putExtra("pic",bytes);
//                setResult(100,intent);
//                finish();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading();
                    }
                });
                upload(bytes);

            }
        });
    }

    /**
     * 上传文件到face++
     */
    public void upload(byte[] bytes) {
        MultipartBody.Builder buildernew = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
//                .addFormDataPart("api_key", "Crvr0GLFWF_1NXfmabkp8XCBgMfuLGgN")
//                .addFormDataPart("api_secret", "5OpIbdNZuCve5zcSuqaHjxHtQ7PYjPlG");

        String url = "";
        try {
            Class<?> clazz = Class.forName("com.autochina.kypay.request.UrlConstant");
            Field f = clazz.getDeclaredField("URL_GET_ROAD_CERTIFICATE_CONTENT"); //NoSuchFieldException
            f.setAccessible(true);
            url = (String) f.get(clazz) + "?token=" + userToken; //IllegalAccessException
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Here you can add the fix number of data.
        buildernew.addFormDataPart("image", "image.jpg", RequestBody.create(MultipartBody.FORM, bytes));
        Request request = new Request.Builder()
                .url(url)
                .post(buildernew.build())
                .build();


        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };


        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                hideLoading();

                Intent intent = new Intent();
//                intent.putExtra("data", new RoadCertificateBean());
                setResult(RESULT_OK, intent);
                finish();

                Log.e("MainTestActivity", "okhttp onFailure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                hideLoading();
                String result = response.body().string();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    int code = jsonObject.optInt("code");
                    switch (code) {
                        case 10000:

//                            JSONObject data = jsonObject.getJSONObject("data");
//                            RoadCertificateBean roadCertificateBean = new RoadCertificateBean();
//                            roadCertificateBean.setOwner(data.optString("owner"));
//                            roadCertificateBean.setVehicle_type(data.optString("vehicle_type"));
//                            roadCertificateBean.setEngine_no(data.optString("engine_no"));
//                            roadCertificateBean.setPlate_no(data.optString("plate_no"));
//                            roadCertificateBean.setVin(data.optString("vin"));
//                            roadCertificateBean.setModel(data.optString("model"));
//
//                            Intent intent = new Intent();
//                            intent.putExtra("data",roadCertificateBean);
//                            setResult(RESULT_OK, intent);
//                            Util.cancleToast(PreviewActivity.this);

                            finish();
                            break;
                        case 50001:
                            Class<?> clazz = Class.forName("com.autochina.kypay.request.AbstractBaseControl");
                            Method sendBroadLogin = clazz.getMethod("sendBroadLogin");
                            sendBroadLogin.invoke(clazz);
                            break;
                        default:
//                            intent = new Intent();
//                            intent.putExtra("data", new RoadCertificateBean());
//                            setResult(RESULT_OK, intent);
//                            Util.cancleToast(PreviewActivity.this);
//                            finish();
                            break;
                    }
                    Log.e("MainTestActivity", "okhttp onResponse" + response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 显示加载框
     */
    //在lib中反射使用主项目中的提示
    public void showLoading() {
        try {
            Class<?> clazz = Class.forName("com.autochina.kypay.ui.widgets.LoadingDialog");
            Method show = clazz.getMethod("show");
            Method isShowing = clazz.getMethod("isShowing");
            if (mLoadingDialog == null) {
                mLoadingDialog = clazz.getDeclaredConstructor(Context.class).newInstance(this);
                show.invoke(mLoadingDialog);
            } else if (!"true".equals(isShowing.invoke(mLoadingDialog).toString())) {
                show.invoke(mLoadingDialog);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏加载框
     */
    public void hideLoading() {
        try {
            Class<?> clazz = Class.forName("com.autochina.kypay.ui.widgets.LoadingDialog");
            Method dismiss = clazz.getMethod("dismiss");
            if (mLoadingDialog != null) {
                dismiss.invoke(mLoadingDialog);
                mLoadingDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPermisionDialog() {
        mStatus = 3;
        try {
            Class<?> clazz = Class.forName("com.autochina.kypay.util.DialogUtil");
            Method noTitleDialog = clazz.getMethod("noTitleDialog", Activity.class, String.class, View.OnClickListener.class, String.class, View.OnClickListener.class, String.class);
            if (mDialogNew != null) {
                Method isShowing = mDialogNew.getClass().getMethod("isShowing");
                if ("true".equals(isShowing.invoke(mDialogNew).toString())) {
                    return;
                }
            }
//            String hint = getString(com.megvii.idcardlib.R.string.permission_capture);
//            if ("huawei".equalsIgnoreCase(Build.BRAND)) {
//                hint = getString(com.megvii.idcardlib.R.string.permission_capture_huawei);
//            } else if ("oppo".equalsIgnoreCase(Build.BRAND)) {
//                hint = getString(com.megvii.idcardlib.R.string.permission_captrue_opo);
//            }

//            mDialogNew = noTitleDialog.invoke(clazz, this, hint, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            }, "", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        Method dismiss = mDialogNew.getClass().getMethod("dismiss");
//                        if ("huawei".equalsIgnoreCase(Build.BRAND) || "oppo".equalsIgnoreCase(Build.BRAND)) {
//                            dismiss.invoke(mDialogNew);
//                        } else {
//                            try {
//                                //Open the specific App Info page:
//                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                intent.setData(Uri.parse("package:" + PreviewActivity.this.getPackageName()));
//                                PreviewActivity.this.startActivity(intent);
//                                dismiss.invoke(mDialogNew);
//                                mStatus = 1;
//                            } catch (ActivityNotFoundException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, "知道了");
            Class<?> clazzDialog = Class.forName("com.autochina.kypay.ui.widgets.NoTitleDialog").cast(mDialogNew).getClass();
            Method show = clazzDialog.getMethod("show");
            Method setBtnVisibility = clazzDialog.getMethod("setBtnVisibility");
            setBtnVisibility.invoke(mDialogNew);
            show.invoke(mDialogNew);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
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
//        if (mCamera == null)
//            return;
//
//        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
//
//        try {
//            Camera.Parameters parameters = mCamera.getParameters();
//            mBestPreviewSize = Util.getNearestRatioSize(parameters, screenWidth,
//                    screenHeight);
//            int cameraWidth = mBestPreviewSize.width;
//            int cameraHeight = mBestPreviewSize.height;
//            parameters.setPreviewSize(cameraWidth, cameraHeight);
//            List<String> focusModes = parameters.getSupportedFocusModes();
//            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
//                parameters
//                        .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//            }
//
//            // Rect rect = new Rect();
//            // rect.left = (int) ((mIndicatorView.CONTENT_RATIO * -1000) /
//            // mIndicatorView.IDCARD_RATIO);
//            // rect.top = (int) (mIndicatorView.CONTENT_RATIO * -1000);
//            // rect.right = (int) ((mIndicatorView.CONTENT_RATIO * 1000) /
//            // mIndicatorView.IDCARD_RATIO);
//            // rect.bottom = (int) (mIndicatorView.CONTENT_RATIO * 1000);
//            // Camera.Area area = new Camera.Area(rect, 1000);
//            // ArrayList<Area> focusAreas = new ArrayList<Area>();
//            // focusAreas.add(area);
//            // parameters.setFocusAreas(focusAreas);
//            Angle = getCameraAngle();
//            mCamera.setDisplayOrientation(Angle);
//            parameters.setPictureFormat(PixelFormat.JPEG);//设置照片输出的格式
//            parameters.setJpegQuality(85);
//            parameters.setPictureSize(1280,720);
//            mCamera.setParameters(parameters);
//
//            float scale = Math.min(screenWidth * 1.0f / mBestPreviewSize.height,
//                    screenHeight * 1.0f / mBestPreviewSize.width);
////            int layout_width = (int) (scale * mBestPreviewSize.height);
////            int layout_height = (int) (scale * mBestPreviewSize.width);
//            int layout_width = screenWidth;
//            int layout_height = screenHeight;
//            RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
//                    layout_width, layout_height);
//            layout_params.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mTextureView.setLayoutParams(layout_params);
////            mIndicatorView.setLayoutParams(layout_params);
////            mIDCardIndicator.setLayoutParams(layout_params);
//            mStatus = 0;
//        } catch (Exception e) {
//            showPermisionDialog();
//            e.printStackTrace();
//        }
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

    private void autoFocus() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(null);
        }
    }
}