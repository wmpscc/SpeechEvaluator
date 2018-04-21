package com.wmpscc.speechevaluator;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechUtility;

public class MainActivity extends AppCompatActivity {
    private SpeechEvaluator mSpeechEvaluator;
    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
        }

        @Override
        public void onBeginOfSpeech() {
            System.out.println("开始");
        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(EvaluatorResult evaluatorResult, boolean b) {
            if (b){
                mTextView.setText(evaluatorResult.getResultString());
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            mTextView.setText(speechError.getErrorCode() + "");
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
    private TextView mTextView;
    private String TAG = "bbb";
    private final static String PREFER_NAME = "ise_settings";
    // 评测语种
    private String language;
    // 评测题型
    private String category;
    // 结果等级
    private String result_level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5ad97691");

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        mTextView = findViewById(R.id.code);
        mSpeechEvaluator = SpeechEvaluator.createEvaluator(this, null);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_init:
                initEva();
                mTextView.setText("初始化完成");
                break;
            case R.id.bt_record_begin:
                mTextView.setText("开始");
                int ret = mSpeechEvaluator.startEvaluating("顾名思义", null, mEvaluatorListener);
                if (ret != ErrorCode.SUCCESS) {
					Log.e(TAG, "识别失败,错误码：" + ret);
				} else {
					byte[] audioData = FucUtil.readAudioFile(this,"isetest.wav");
					if(audioData != null) {
						//防止写入音频过早导致失败
						try{
							new Thread().sleep(100);
						}catch (InterruptedException e) {
							Log.d(TAG,"InterruptedException :"+e);
						}
                        mSpeechEvaluator.writeAudio(audioData,0,audioData.length);
                        mSpeechEvaluator.stopEvaluating();
					}
				}
                break;

            case R.id.bt_eva:
                mTextView.setText("结束");
                mSpeechEvaluator.stopEvaluating();

                break;
        }
    }

    private void initEva(){

        SharedPreferences pref = getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
        // 设置评测语言
        language = pref.getString(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置需要评测的类型
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
        // 设置结果等级（中文仅支持complete）
        result_level = pref.getString(SpeechConstant.RESULT_LEVEL, "complete");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        String vad_bos = pref.getString(SpeechConstant.VAD_BOS, "5000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        String vad_eos = pref.getString(SpeechConstant.VAD_EOS, "1800");
        // 语音输入超时时间，即用户最多可以连续说多长时间；
        String speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");

        mSpeechEvaluator.setParameter(SpeechConstant.LANGUAGE, language);
        mSpeechEvaluator.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mSpeechEvaluator.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mSpeechEvaluator.setParameter(SpeechConstant.VAD_BOS, vad_bos);
        mSpeechEvaluator.setParameter(SpeechConstant.VAD_EOS, vad_eos);
        mSpeechEvaluator.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout);
        mSpeechEvaluator.setParameter(SpeechConstant.RESULT_LEVEL, result_level);
        mSpeechEvaluator.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
        mSpeechEvaluator.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechEvaluator.setParameter(SpeechConstant.ISE_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/ise.wav");
        mSpeechEvaluator.setParameter(SpeechConstant.AUDIO_SOURCE,"-1");
    }


}
