from django.urls import path

from .views import (
    TeacherAttendanceView,
    AttendanceStudentListView,
    StudentAttendanceListView,
)


urlpatterns = [

    path(
        "teacher/mark/",
        TeacherAttendanceView.as_view(),
        name="teacher-mark-attendance",
    ),

    path(
        "teacher/students/",
        AttendanceStudentListView.as_view(),
        name="attendance-student-list",
    ),

    path(
        "student/",
        StudentAttendanceListView.as_view(),
        name="student-attendance",
    ),

]