package com.example.aeye.ui.listener

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TextToSpeechManager {

    private var mTextToSpeech : TextToSpeech ?= null
    private var isLoaded : Boolean = false

    fun init(context : Context){
        try {
            mTextToSpeech = TextToSpeech(context, onInitListener)
        } catch (e : Exception){
            e.printStackTrace()
        }
    }

    private val onInitListener = TextToSpeech.OnInitListener{
        if (it == TextToSpeech.SUCCESS){
            val result : Int = mTextToSpeech!!.setLanguage(Locale.KOREA)
            isLoaded = true

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("error", "This language is not supported")
        }
        else {
            Log.e("error", "Initialization failed")
        }
    }

    fun shutDown() {
        mTextToSpeech!!.stop()
        mTextToSpeech!!.shutdown()
    }

    fun addQueue(info : CharSequence){
        if (isLoaded) {
            mTextToSpeech!!.setSpeechRate(0.6F)
            mTextToSpeech!!.setPitch(0.6F)
            mTextToSpeech!!.speak(info, TextToSpeech.QUEUE_ADD, null, "id1")
        }
        else{
            Log.e("error", "TTS not initialized")
        }
    }

    fun initQueue(info : CharSequence){
        if (isLoaded) {
            mTextToSpeech!!.setSpeechRate(1F)
            mTextToSpeech!!.setPitch(0.6F)
            mTextToSpeech!!.speak(info, TextToSpeech.QUEUE_FLUSH, null, "id1")
        }
        else{
            Log.e("error", "TTS not initialized")
        }
    }

    fun runTTS() {
        mTextToSpeech!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                //ignore
            }

            override fun onDone(utteranceId: String?) {

            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                //ignore
            }

        })
    }
}