package com.example.campushub.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class CampusHubPrefs {

    private static final String PREF_NAME = "CampusHubPrefs";

    private final SharedPreferences preferences;

    public CampusHubPrefs(Context context) {

        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    // =====================================================
    // ACCESS TOKEN
    // =====================================================

    public void saveAccessToken(String token) {

        preferences.edit()
                .putString("access_token", token)
                .apply();
    }

    public String getAccessToken() {

        return preferences.getString(
                "access_token",
                ""
        );
    }


    // =====================================================
    // REFRESH TOKEN
    // =====================================================

    public void saveRefreshToken(String token) {

        preferences.edit()
                .putString("refresh_token", token)
                .apply();
    }

    public String getRefreshToken() {

        return preferences.getString(
                "refresh_token",
                ""
        );
    }


    // =====================================================
    // STUDENT ID
    // =====================================================

    public void saveStudentId(String studentId) {

        preferences.edit()
                .putString("student_id", studentId)
                .apply();
    }

    public String getStudentId() {

        return preferences.getString(
                "student_id",
                ""
        );
    }


    // =====================================================
    // TEACHER ID
    // =====================================================

    public void saveTeacherId(String teacherId) {

        preferences.edit()
                .putString("teacher_id", teacherId)
                .apply();
    }

    public String getTeacherId() {

        return preferences.getString(
                "teacher_id",
                ""
        );
    }


    // =====================================================
    // ROLE
    // =====================================================

    public void saveRole(String role) {

        preferences.edit()
                .putString("role", role)
                .apply();
    }

    public String getRole() {

        return preferences.getString(
                "role",
                ""
        );
    }


    // =====================================================
    // REMEMBER ME
    // =====================================================

    public void saveRememberMe(boolean rememberMe) {

        preferences.edit()
                .putBoolean(
                        "remember_me",
                        rememberMe
                )
                .apply();
    }

    public boolean getRememberMe() {

        return preferences.getBoolean(
                "remember_me",
                false
        );
    }


    // =====================================================
    // TEACHER REMEMBER ME
    // =====================================================

    public void saveRememberMeTeacher(boolean rememberMe) {

        preferences.edit()
                .putBoolean(
                        "remember_me_teacher",
                        rememberMe
                )
                .apply();
    }

    public boolean getRememberMeTeacher() {

        return preferences.getBoolean(
                "remember_me_teacher",
                false
        );
    }


    // =====================================================
    // CLEAR ALL
    // =====================================================

    public void clear() {

        preferences.edit()
                .clear()
                .apply();
    }
}