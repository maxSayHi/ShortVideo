package learn.bobo.com.video;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.List;

/**
 * Created by max on 17-4-11.
 */

class Preview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "Preview";
    // 预览画面的SurfaceView
    SurfaceView mSurfaceView;
    // SurfaceHolder接:控制surface的大小和格式， 在surface上编辑像素及监视surace的改变等 。
    SurfaceHolder mHolder;
    // 预览画面的大小
    Size mPreviewSize;
    List<Size> mSupportedPreviewSizes;
    // 相机
    Camera mCamera;

    public Preview(Context context, AttributeSet set) {
        super(context,set);
        mSurfaceView = new SurfaceView(context);
        //添加View
        addView(mSurfaceView);
        //初始化SurfaceHolder
        mHolder = mSurfaceView.getHolder();
        //绑定监听接口
        mHolder.addCallback(this);
        //设置类型
        //SURFACE_TYPE_PUSH_BUFFERS表明该Surface不包含原生数据，Surface用到的数据由其他对象提供，
        //在Camera图像预览中就使用该类型的Surface，有Camera负责提供给预览Surface数据，这样图像预览会比较流畅
        //http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2012/1201/656.html
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // 相机设置
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            // 获取摄像头所支持的所有分辨率
            mSupportedPreviewSizes = mCamera.getParameters()
                    .getSupportedPreviewSizes();
            requestLayout();
        }
    }

    public void switchCamera(Camera camera) {
        setCamera(camera);
        try {
            //设置预览展示
            camera.setPreviewDisplay(mHolder);
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
        //获取相机配置 参数
        Camera.Parameters parameters = camera.getParameters();
        //设置预览大小
        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        requestLayout();
        //给相机设置参数
        camera.setParameters(parameters);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        //摄像头不是所有随便的(w, h)都能够认识的，所以在onMeasure()方法中进行处理
////        final int width = resolveSize(getSuggestedMinimumWidth(),
////                widthMeasureSpec);
////        final int height = resolveSize(getSuggestedMinimumHeight(),
////                heightMeasureSpec);
////        setMeasuredDimension(width, height);
////        if (mSupportedPreviewSizes != null) {
////            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
////                    height);
////        }
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }


            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height
                        / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width
                        / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width,
                        (height + scaledChildHeight) / 2);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    //获取最佳的拍照或录像预览窗口大小
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // 试着找到一个尺寸匹配的纵横比和尺寸
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // 找不到一个匹配的纵横比，进行处理
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //预览大小是已知的，设置相机参数，并开始预览。
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureFormat(PixelFormat.JPEG);//设置照片输出的格式
            parameters.setJpegQuality(85);
            parameters.setPictureSize(1280,720);
            requestLayout();
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Sets the mPreviewSize
     * You can use getPreviewSize() to get the value of mPreviewSize
     */
    public void setPreviewSize(Size previewSize) {
        mPreviewSize = previewSize;
    }
}
