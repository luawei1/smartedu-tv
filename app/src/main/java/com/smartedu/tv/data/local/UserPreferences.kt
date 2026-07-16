package com.smartedu.tv.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    suspend fun saveSubject(subject: String) {
        context.dataStore.edit { preferences ->
            preferences[SUBJECT_KEY] = subject
        }
    }

    companion object {
        private val SECTION_KEY = stringPreferencesKey("user_section")
        private val GRADE_KEY = stringPreferencesKey("user_grade")
        private val SUBJECT_KEY = stringPreferencesKey("user_subject")
        private val HISTORY_KEY = stringPreferencesKey("user_history") // JSON array of course history
        
        // 默认配置
        const val DEFAULT_SECTION = "小学"
        const val DEFAULT_GRADE = "一年级"
        const val DEFAULT_SUBJECT = "语文"
    }

    val sectionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SECTION_KEY] ?: DEFAULT_SECTION
    }

    val gradeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GRADE_KEY] ?: DEFAULT_GRADE
    }

    val subjectFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUBJECT_KEY] ?: DEFAULT_SUBJECT
    }

    suspend fun saveSection(section: String) {
        context.dataStore.edit { preferences ->
            preferences[SECTION_KEY] = section
        }
    }

    suspend fun saveGrade(grade: String) {
        context.dataStore.edit { preferences ->
            preferences[GRADE_KEY] = grade
        }
    }

    // 历史记录格式：[{"id":"xxx", "title":"xxx", "coverUrl":"xxx"}, ...]
    val historyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HISTORY_KEY] ?: "[]"
    }

    suspend fun saveToHistory(courseId: String, title: String, coverUrl: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[HISTORY_KEY] ?: "[]"
            try {
                val array = org.json.JSONArray(currentJson)
                val newArray = org.json.JSONArray()
                
                // 将新项放在最前面
                val newItem = org.json.JSONObject()
                newItem.put("id", courseId)
                newItem.put("title", title)
                newItem.put("coverUrl", coverUrl)
                newArray.put(newItem)
                
                // 复制旧项（去重，保留最多 10 个）
                var count = 1
                for (i in 0 until array.length()) {
                    if (count >= 10) break
                    val item = array.getJSONObject(i)
                    if (item.getString("id") != courseId) {
                        newArray.put(item)
                        count++
                    }
                }
                
                preferences[HISTORY_KEY] = newArray.toString()
            } catch (e: Exception) {
                // If parsing fails, reset
                preferences[HISTORY_KEY] = "[{\"id\":\"$courseId\",\"title\":\"$title\",\"coverUrl\":\"$coverUrl\"}]"
            }
        }
    }
}
