package com.xiaofeng.audiorecord;

/**
 * Created by xiaofeng on 16-6-25.
 */
public interface IAudioRecord {
    public void onAudioParameter(int sampleBit, int sampleRate, int channel);
    public void onAudioPcm(byte[] pcm, long pts);
}
