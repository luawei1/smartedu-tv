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

    companion object {
        private val SECTION_KEY = stringPreferencesKey("user_section")
        private val GRADE_KEY = stringPreferencesKey("user_grade")
        private val SUBJECT_KEY = stringPreferencesKey("user_subject")
        
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

    suspend fun saveSubject(subject: String) {
        context.dataStore.edit { preferences ->
            preferences[SUBJECT_KEY] = subject
        }
    }
}
