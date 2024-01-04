package appCore.common;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;

import java.util.Locale;

public class SpeechUtils {
    private Context context;


    private static final String TAG = "SpeechUtils";
    private static volatile SpeechUtils singleton;

    private TextToSpeech textToSpeech; // TTS对象
    // private boolean isOld;

    public static SpeechUtils getInstance(Context context) {
        if (singleton == null) {
            synchronized (SpeechUtils.class) {
                if (singleton == null) {
                    singleton = new SpeechUtils(context);
                }
            }
        }
        return singleton;
    }

    public SpeechUtils(Context context) {
        this.context = context;
        speakText(null);
    }


    /**
     * 关闭 语音合成
     */
    public void shutdown(){
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    /**
     * 第二个参数queueMode用于指定发音队列模式，两种模式选择
     * （1）TextToSpeech.QUEUE_FLUSH：该模式下在有新任务时候会清除当前语音任务，执行新的语音任务
     * （2）TextToSpeech.QUEUE_ADD：该模式下会把新的语音任务放到语音任务之后，
     * @param text
     */
    public void speakText(String text) {
        if(textToSpeech==null){
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    System.out.println("开始播报");
                    if (i == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(Locale.CHINA);
                        textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                        textToSpeech.setSpeechRate(1.5f);
                    /*if(result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE){
                        Toast.makeText(context, "TTS暂时不支持这种语音的朗读！",
                                Toast.LENGTH_LONG).show();
                    }else {
                        textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                        textToSpeech.setSpeechRate(1.0f);
                    }*/
                    }
                }
            });
        }
        if(!TextUtils.isEmpty(text)){
            textToSpeech.speak(text,TextToSpeech.QUEUE_ADD, null);
        }

    }

}

