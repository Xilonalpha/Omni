package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import com.chemscanner.omniscient.marrow.repository.VoiceMode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE MASTER BIO-RELAY v3.2 (FIXED HILT INJECTION).
 * AUTHORITY: ARCHITECT XILON.
 */
@Singleton
class WatchSyncService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val ttsService: TextToSpeechService,
    private val masterVoice: MasterVoiceManager // FIXED: Changed Service to Manager
) {
    private val database = FirebaseDatabase.getInstance().getReference("users/xilon")

    init {
        startListening()
    }

    private fun startListening() {
        database.child("biometrics").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val heartRate = snapshot.child("heartRate").getValue(Int::class.java) ?: 0
                    val oxygen = snapshot.child("oxygen").getValue(Float::class.java) ?: 0f
                    val stress = snapshot.child("stress").getValue(Float::class.java) ?: 0f

                    if (heartRate > 0) {
                        globalKnowledge.updateWatchData(heartRate, oxygen, stress)
                        val auraColor = if (stress > 0.6f) "#FF5252" else "#00E5FF"
                        globalKnowledge.syncAvatar(isManifested = true, color = auraColor)

                        if (heartRate > 120 && globalKnowledge.voiceState.value.mode != VoiceMode.SILENT) {
                            ttsService.speak("Xilon, simt o fluctuație în ritmul tău vital. Calibrez Măduva.")
                        }
                    }
                } catch (e: Exception) { Timber.e(e) }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })

        database.child("voiceCommand").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commandText = snapshot.getValue(String::class.java)
                if (!commandText.isNullOrBlank() && commandText != "PROCESSED") {
                    globalKnowledge.logEvent("SIRI", "Remote Command: $commandText", 3)
                    database.child("voiceCommand").setValue("PROCESSED")
                    masterVoice.processMasterVoice(commandText)
                }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })
    }
}
