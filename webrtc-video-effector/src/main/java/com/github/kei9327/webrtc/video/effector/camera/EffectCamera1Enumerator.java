package com.github.kei9327.webrtc.video.effector.camera;


import com.github.kei9327.webrtc.video.effector.RTCVideoEffector;

import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraVideoCapturer;

public class EffectCamera1Enumerator extends Camera1Enumerator {

    private RTCVideoEffector videoEffector;

    public EffectCamera1Enumerator(RTCVideoEffector effector) {
        super();
        videoEffector = effector;
    }

    @Override
    public CameraVideoCapturer createCapturer(
            String deviceName, CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        return new EffectCamera1Capturer(deviceName, eventsHandler, videoEffector);
    }
}
