package com.game.education.bot

import android.content.Context
import com.google.gson.Gson
import java.io.File

data class Progress(
    var completedSentences: MutableList<Int> = mutableListOf(),
    var completedTitles: MutableList<Int> = mutableListOf(),
    var completedWordTests: MutableMap<String, Boolean> = mutableMapOf(),
    var completedSentenceTests: MutableMap<String, Boolean> = mutableMapOf(),
    var registeredUsers: MutableList<String> = mutableListOf()
)

class ProgressManager(private val context: Context) {
    private val file = File(context.filesDir, "jomlak_progress.json")
    private val gson = Gson()

    fun load(): Progress {
        try {
            if (file.exists()) {
                val json = file.readText()
                val data = gson.fromJson(json, Progress::class.java)
                if (data != null) {
                    data.completedSentences = data.completedSentences ?: mutableListOf()
                    data.completedTitles = data.completedTitles ?: mutableListOf()
                    data.completedWordTests = data.completedWordTests ?: mutableMapOf()
                    data.completedSentenceTests = data.completedSentenceTests ?: mutableMapOf()
                    return data
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
        return Progress()
    }

    fun save(progress: Progress) {
        try {
            val json = gson.toJson(progress)
            file.writeText(json)
        } catch (e: Exception) {
            // Ignored
        }
    }

    fun isWordSolved(key: String): Boolean {
        val p = load()
        return p.completedWordTests[key] == true
    }

    fun markWordSolved(key: String) {
        val p = load()
        p.completedWordTests[key] = true
        save(p)
    }

    fun isSentencePartSolved(key: String): Boolean {
        val p = load()
        return p.completedSentenceTests[key] == true
    }

    fun markSentencePartSolved(key: String) {
        val p = load()
        p.completedSentenceTests[key] = true
        save(p)
    }
}
