package com.example.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class RealAudioEngine(private val context: Context) {

    private val _micLevel = MutableStateFlow(0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _isRecordingActive = MutableStateFlow(false)
    val isRecordingActive: StateFlow<Boolean> = _isRecordingActive.asStateFlow()

    private val _isMicrophoneDetected = MutableStateFlow<Boolean?>(null)
    val isMicrophoneDetected: StateFlow<Boolean?> = _isMicrophoneDetected.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Synchronized
    fun startMicrophoneTest() {
        if (!hasRecordPermission()) {
            _isMicrophoneDetected.value = false
            return
        }

        stopMicrophoneTest()

        try {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = if (minBufferSize > 0) minBufferSize * 2 else 4096

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                _isMicrophoneDetected.value = false
                return
            }

            audioRecord?.startRecording()
            _isRecordingActive.value = true
            _isMicrophoneDetected.value = true

            recordJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                var detectionAccumulator = 0
                while (isActive && _isRecordingActive.value) {
                    val record = audioRecord ?: break
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var maxAmp = 0
                        for (i in 0 until read) {
                            val amp = abs(buffer[i].toInt())
                            if (amp > maxAmp) {
                                maxAmp = amp
                            }
                        }
                        // Normalize 0..32767 to 0.0..1.0
                        val normalized = (maxAmp / 18000f).coerceIn(0f, 1f)
                        _micLevel.value = normalized
                        if (maxAmp > 300) {
                            detectionAccumulator++
                            if (detectionAccumulator > 3) {
                                _isMicrophoneDetected.value = true
                            }
                        }
                    } else {
                        delay(50)
                    }
                }
            }
        } catch (e: Exception) {
            _isMicrophoneDetected.value = false
            stopMicrophoneTest()
        }
    }

    @Synchronized
    fun stopMicrophoneTest() {
        recordJob?.cancel()
        recordJob = null
        _isRecordingActive.value = false
        _micLevel.value = 0f
        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    fun release() {
        stopMicrophoneTest()
    }
}
