package com.xiaofeng.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by xiaofeng on 16-6-25.
 */
public class AudioRecorder {
    // 输出log时用
    private String TAG = AudioRecorder.class.getName();

    // 音频的获取源 mic采集
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 音频采样的三个重要参数, 采样位数 采样率 声道数
    private static int sampleBit = AudioFormat.ENCODING_PCM_16BIT;  // 每个采样点16bit存放及2个字节
    private static int sampleRate = 44100;  // 每秒采样44100个样本
    private static int channel = AudioFormat.CHANNEL_IN_MONO; // MONO单声道 STEREO双声道

    private static int INIT = 0x00;
    private static int START = 0x01;
    private static int PAUSE = 0x02;
    private static int RELEASE = 0x03;
    private int recordStatus = INIT;

    private AudioRecord mAudioRecord = null;
    private IAudioRecord listener = null;
    private AudioRecordThread recordThread = null;

    public AudioRecorder(IAudioRecord listener) {
        this.listener = listener;
    }

    public void start() {
        Log.i(TAG, "start()");
        if (recordThread == null) {
            recordThread = new AudioRecordThread();
            recordThread.start();
        }
        recordStatus = START;
    }

    public void pause() {
        Log.i(TAG, "pause() status:" + recordStatus);
        if (recordStatus == START) {
            recordStatus = PAUSE;
            if (mAudioRecord != null) {
                mAudioRecord.stop();
            }
        }else if (recordStatus == PAUSE) {
            recordStatus = START;
            if (mAudioRecord != null) {
                mAudioRecord.startRecording();
            }
        }
    }

    public void release() {
        Log.i(TAG, "release()");
        if (recordStatus == START) {
            recordStatus = RELEASE;
            if (recordThread != null) {
                try {
                    recordThread.join();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recordThread = null;
            }
        }
    }

    // 每次录音的大小为一帧（16bit采样时），如果是8bit，则是2帧
    // 16bit单声道44100采样时一帧音频的大小为4096字节
    private class AudioRecordThread extends Thread {
        public void run() {
            Log.i(TAG, "run()");
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, sampleBit);
            int temp_channel = channel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
            Log.i(TAG, "bufferSize:" + bufferSize + " ,4096 * channel:" + 4096 * temp_channel);
            bufferSize = Math.max(bufferSize, 4096 * temp_channel);


            mAudioRecord = new AudioRecord(audioSource, sampleRate, channel, sampleBit, bufferSize);
            if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "initialize audioRecord failed");
                return;
            }
            if (listener != null) {
                listener.onAudioParameter(16, 44100, 1);
            }

            byte[] audioBuffer = new byte[bufferSize];
            mAudioRecord.startRecording();

            while (true) {
                if (recordStatus == PAUSE) {
                    continue;
                }
                if (recordStatus == RELEASE) {
                    break;
                }
                int size = mAudioRecord.read(audioBuffer, 0, audioBuffer.length);
                if (size <= 0) {
                    Log.e(TAG, "no mic data!");
                    continue;
                }

                byte[] pcm = new byte[size];
                System.arraycopy(audioBuffer, 0, pcm, 0, size);
                if (listener != null) {
                    listener.onAudioPcm(pcm, 0);
                }
            }

            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
            Log.e(TAG, "AudioRecordThread quit!");
        }
    }

}
