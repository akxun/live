package com.xiaofeng.audiorecord;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements IAudioRecord, OnClickListener{
    private String TAG = MainActivity.class.getName();
    private Button startBtn, stopBtn, quitBtn;
    private AudioRecorder mAudioRecorder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);
        quitBtn = (Button) findViewById(R.id.quit);
        quitBtn.setOnClickListener(this);

        mAudioRecorder = new AudioRecorder(this);
    }



    @Override
    public void onAudioParameter(int sampleBit, int sampleRate, int channel) {
        Log.i(TAG, "sampleBit:" + sampleBit + " sampleRate:" + sampleRate + " channel:" + channel);
        initWav(sampleBit, sampleRate, channel);
    }

    @Override
    public void onAudioPcm(byte[] pcm, long pts) {
        Log.i(TAG, "onAudioPcm() len:" + pcm.length);
        onPcm(pcm);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                if (mAudioRecorder != null) {
                    mAudioRecorder.start();
                }
                break;
            case R.id.stop:
                if (mAudioRecorder != null) {
                    mAudioRecorder.pause();
                }
                break;
            case R.id.quit:
                if (mAudioRecorder != null) {
                    mAudioRecorder.release();
                }
                break;
            default:
                break;
        }
    }

    static {
        System.loadLibrary("xiaofeng");
    }

    native private void initWav(int sampleBit, int sampleRate, int channel);
    native private void onPcm(byte[] pcm);

}
