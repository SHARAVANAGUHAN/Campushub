from django.urls import path

from .views import (
    StudentTimetableView,
    TeacherTimetableView,
)


urlpatterns = [

    # Student timetable
    path(
        "student/",
        StudentTimetableView.as_view(),
        name="student-timetable"
    ),

    # Teacher timetable
    path(
        "teacher/",
        TeacherTimetableView.as_view(),
        name="teacher-timetable"
    ),

]