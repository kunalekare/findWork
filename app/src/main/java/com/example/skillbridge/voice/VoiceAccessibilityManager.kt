package com.example.skillbridge.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class VoiceAccessibilityManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _extractedEntities = MutableStateFlow<Map<String, String>>(emptyMap())
    val extractedEntities: StateFlow<Map<String, String>> = _extractedEntities.asStateFlow()

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Attempt Hindi, Marathi or English
            val hiLocale = Locale("hi", "IN")
            val mrLocale = Locale("mr", "IN")
            if (tts?.isLanguageAvailable(hiLocale) == TextToSpeech.LANG_AVAILABLE) {
                tts?.language = hiLocale
            } else if (tts?.isLanguageAvailable(mrLocale) == TextToSpeech.LANG_AVAILABLE) {
                tts?.language = mrLocale
            } else {
                tts?.language = Locale.ENGLISH
            }
        }
    }

    fun speak(text: String, languageCode: String = "hi") {
        _currentPrompt.value = text
        if (isTtsReady && tts != null) {
            try {
                val locale = when (languageCode) {
                    "mr" -> Locale("mr", "IN")
                    "hi" -> Locale("hi", "IN")
                    else -> Locale.ENGLISH
                }
                tts?.language = locale
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "skillbridge_voice_id")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun startListeningSimulation(sampleQuery: String? = null) {
        _isListening.value = true
        _spokenText.value = sampleQuery ?: "Mujhe Dharampeth me urgent electrician chahiye switchboard repair ke liye"
        
        // Entity extraction logic
        val entities = mutableMapOf<String, String>()
        val lower = _spokenText.value.lowercase()
        
        if (lower.contains("electrician") || lower.contains("बिजली") || lower.contains("वायरिंग")) {
            entities["Skill"] = "Electrician"
        } else if (lower.contains("plumber") || lower.contains("नल") || lower.contains("प्लंबर")) {
            entities["Skill"] = "Plumber"
        } else if (lower.contains("cook") || lower.contains("खाना") || lower.contains("रसोइया")) {
            entities["Skill"] = "Cook & Chef"
        } else if (lower.contains("driver") || lower.contains("गाड़ी") || lower.contains("ड्राइवर")) {
            entities["Skill"] = "Driver"
        } else if (lower.contains("design") || lower.contains("डिज़ाइन") || lower.contains("लोगो")) {
            entities["Skill"] = "Graphic Design"
        } else {
            entities["Skill"] = "Local Service"
        }

        if (lower.contains("urgent") || lower.contains("जल्दी") || lower.contains("तातडीने")) {
            entities["Urgency"] = "Immediate (Within 30 mins)"
        } else {
            entities["Urgency"] = "Standard Schedule"
        }

        if (lower.contains("dharampeth")) {
            entities["Location"] = "Dharampeth, Nagpur"
        } else if (lower.contains("sadar")) {
            entities["Location"] = "Sadar, Nagpur"
        } else {
            entities["Location"] = "Nagpur Central"
        }

        entities["Est. Budget"] = "₹450 (Escrow Hold)"

        _extractedEntities.value = entities
    }

    fun stopListening() {
        _isListening.value = false
    }

    fun clear() {
        _isListening.value = false
        _spokenText.value = ""
        _extractedEntities.value = emptyMap()
        _currentPrompt.value = ""
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
