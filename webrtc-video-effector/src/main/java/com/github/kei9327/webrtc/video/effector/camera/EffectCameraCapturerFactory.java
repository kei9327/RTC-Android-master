package com.github.kei9327.webrtc.video.effector.camera;

import com.github.kei9327.webrtc.video.effector.RTCVideoEffector;

import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;

public class EffectCameraCapturerFactory {

    public static CameraVideoCapturer create(RTCVideoEffector effector) {

        CameraVideoCapturer videoCapturer = null;
        videoCapturer = EffectCameraCapturerFactory.createCapturer(
                new EffectCamera1Enumerator(effector));
        return videoCapturer;
    }

    public static CameraVideoCapturer createCapturer(CameraEnumerator enumerator) {

        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }
}
