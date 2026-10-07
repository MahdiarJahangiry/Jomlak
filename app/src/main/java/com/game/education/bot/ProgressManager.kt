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

class ProgressManager(context: Context) {

    private val file = File(context.filesDir, "jomlak_progress.json")
    private val gson = Gson()

    fun load(): Progress {
        return try {
            if (!file.exists()) return Progress()
            val json = file.readText()
            gson.fromJson(json, Progress::class.java) ?: Progress()
        } catch (e: Exception) {
            Progress()
        }
    }

    fun save(progress: Progress) {
        try {
            file.writeText(gson.toJson(progress))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
