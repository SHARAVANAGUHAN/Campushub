from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static


urlpatterns = [

    path(
        "admin/",
        admin.site.urls
    ),

    path(
        "api/auth/",
        include("accounts.urls")
    ),

    path(
        "api/departments/",
        include("departments.urls")
    ),

    path(
        "api/subjects/",
        include("subjects.urls")
    ),

    path(
        "api/students/",
        include("students.urls")
    ),

    path(
        "api/attendance/",
        include("attendance.urls")
    ),

    path(
        "api/assignments/",
        include("assignments.urls")
    ),

    path(
        "api/timetable/",
        include("timetable.urls")
    ),
    path
    ("api/leave/",
     include("leave.urls")),
     path("api/announcements/", include("announcements.urls")),
     path("api/library/", include("library.urls")),

]


# =========================================================
# MEDIA FILES - DEVELOPMENT
# =========================================================

if settings.DEBUG:

    urlpatterns += static(
        settings.MEDIA_URL,
        document_root=settings.MEDIA_ROOT
    )
