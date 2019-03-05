package com.github.kei9327.webrtc.video.effector.camera;

import android.content.Context;

import com.github.kei9327.webrtc.video.effector.CapturerObserverProxy;
import com.github.kei9327.webrtc.video.effector.RTCVideoEffector;
import com.github.kei9327.webrtc.video.effector.VideoEffectorLogger;

import org.webrtc.Camera1Capturer;
import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;

public class EffectCamera1Capturer extends Camera1Capturer {
    public static final String TAG = EffectCamera1Capturer.class.getSimpleName();

    private CapturerObserverProxy observer;
    private RTCVideoEffector videoEffector;

    public EffectCamera1Capturer(String cameraName,
                                 CameraEventsHandler eventsHandler,
                                 RTCVideoEffector effector) {
        super(cameraName, eventsHandler, false);
        videoEffector = effector;
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper,
                           Context applicationContext,
                           CapturerObserver originalObserver) {
        VideoEffectorLogger.d(TAG, "initialize");
        observer = new CapturerObserverProxy(surfaceTextureHelper,
                originalObserver, videoEffector);
        super.initialize(surfaceTextureHelper, applicationContext, observer);
    }
}
