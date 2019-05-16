package com.github.kei9327.webrtc.video.effector;

import android.content.Context;

import com.github.kei9327.webrtc.video.effector.filter.FrameImageFilter;
import com.github.kei9327.webrtc.video.effector.filter.GPUImageFilterWrapper;
import com.github.kei9327.webrtc.video.effector.format.LibYuvBridge;
import com.github.kei9327.webrtc.video.effector.format.YuvByteBufferDumper;
import com.github.kei9327.webrtc.video.effector.format.YuvByteBufferReader;

import org.webrtc.GlUtil;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;

public class RTCVideoEffector {

    public static final String TAG = RTCVideoEffector.class.getSimpleName();
    private GPUImageFilterWrapper filterWrapper;
    private Runnable mPendingRunnable;
    private GPUImageFilter mShaderGroup;
    private boolean isFrist = true;
    private LibYuvBridge libYuvBridge;


    public RTCVideoEffector(Context applicationContext) {
        filterWrapper = new GPUImageFilterWrapper(new GPUImageFilter());
    }

    private VideoEffectorContext context = new VideoEffectorContext();
    //    private List<FrameImageFilter> filters = new ArrayList<>();
    private boolean enabled = true;

    private YuvByteBufferReader yuvBytesReader;
    private YuvByteBufferDumper yuvBytesDumper;

    private SurfaceTextureHelper helper;

    public void init(SurfaceTextureHelper helper) {

        VideoEffectorLogger.d(TAG, "init");

        this.helper = helper;

        libYuvBridge = new LibYuvBridge();

        yuvBytesReader = new YuvByteBufferReader();
        yuvBytesReader.init();

        yuvBytesDumper = new YuvByteBufferDumper();
        yuvBytesDumper.init();

        filterWrapper.init();

        GlUtil.checkNoGLES2Error("RTCVideoEffector.init");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    VideoFrame.I420Buffer processByteBufferFrame(VideoFrame.I420Buffer i420Buffer,
                                                 int rotation, long timestamp) {

        if (!needToProcessFrame()) {
            return i420Buffer;
        }

        // Direct buffer ではない場合スルーする
        // TODO: direct に変換してあげる手もある
        if(!i420Buffer.getDataY().isDirect()
                || !i420Buffer.getDataU().isDirect()
                || !i420Buffer.getDataV().isDirect()) {
            return i420Buffer;
        }

        int width = i420Buffer.getWidth();
        int height = i420Buffer.getHeight();
        int strideY = i420Buffer.getStrideY();
        int strideU = i420Buffer.getStrideU();
        int strideV = i420Buffer.getStrideV();

        context.updateFrameInfo(width, height, rotation, timestamp);

        if (mShaderGroup != null) {
            filterWrapper.updateShader(mShaderGroup, width, height);
            mShaderGroup = null;
        }

        int stepTextureId = yuvBytesReader.read(i420Buffer);

        // ビデオフレームの画像は回転された状態で来ることがある
        // グレースケールやセピアフィルタなど、画像全体に均質にかけるエフェクトでは問題にならないが
        // 座標を指定する必要のあるエフェクトでは、使いにくいものとなる。
        // 비디오 프레임의 이미지는 회전 된 상태로 올 수 있는
        // 그레이 스케일과 세피아 필터 등 이미지 전체에 균일하게 적용되는 효과는 문제가 되지 않지만
        // 좌표를 지정해야 하는 효과는 사용하기 어려울 수 있습니다

        // そのため、場合によっては、フィルタをかける前後で回転の補正を行う必要がある
        // ただし、そのためのtexture間のコピーが二度発生することになる
        // 必要のないときはこの機能は使わないようにon/offできるようにしておきたい
        // 따라서, 경우에 따라 필터링 전후로 회전 보정을 해야 할 수 있습니다
        // 추가로 텍스쳐간 복사가 두번 발생하게 되는 기능이 필요하지 않을 때에를 위해, 관련 기능을 토글처리를 구현해야 할 수 있습니다

        if (context.getFrameInfo().isRotated()) {
            // TODO
        }

        if (filterWrapper.isEnabled()) {
            stepTextureId = filterWrapper.filter(context, stepTextureId);
        }

        if (context.getFrameInfo().isRotated()) {
            // TODO
        }

        return yuvBytesDumper.dump(stepTextureId, width, height, strideY, strideU, strideV);
    }


    public boolean needToProcessFrame() {
        if (!enabled) {
            return false;
        }

        if (filterWrapper != null) {
            if (filterWrapper.isEnabled()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public void dispose() {
        if (helper != null) {
            // This effector is not initialized
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(this.helper.getHandler(), new Runnable() {
            @Override
            public void run() {
                disposeInternal();
            }
        });
    }

    private void disposeInternal() {
        if (filterWrapper != null) {
            filterWrapper.dispose();
        }

        yuvBytesReader.dispose();
        yuvBytesDumper.dispose();
    }

    public void updateShader(final GPUImageFilter group) {
        mShaderGroup = group;
    }
}
