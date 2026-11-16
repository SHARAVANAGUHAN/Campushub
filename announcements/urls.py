from django.urls import path

from .views import (
    TeacherAnnouncementCreateView,
    TeacherAnnouncementListView,
    StudentAnnouncementListView,
    AnnouncementDetailView,
)


urlpatterns = [

    path(
        "teacher/create/",
        TeacherAnnouncementCreateView.as_view(),
        name="teacher-create-announcement"
    ),

    path(
        "teacher/list/",
        TeacherAnnouncementListView.as_view(),
        name="teacher-announcement-list"
    ),

    path(
        "student/list/",
        StudentAnnouncementListView.as_view(),
        name="student-announcement-list"
    ),

    path(
        "<int:announcement_id>/",
        AnnouncementDetailView.as_view(),
        name="announcement-detail"
    ),

]