from django.urls import path

from .views import (
    StudentLeaveDepartmentListView,
    StudentLeaveTeacherListView,
    StudentApplyLeaveView,
    StudentLeaveApplicationListView,
    StudentLeaveApplicationDetailView,
    TeacherLeaveApplicationListView,
    TeacherLeaveApplicationDetailView,
    TeacherApproveLeaveView,
    TeacherRejectLeaveView,
)


urlpatterns = [

    # =====================================================
    # STUDENT
    # =====================================================

    path(
        "student/departments/",
        StudentLeaveDepartmentListView.as_view(),
        name="student-leave-departments"
    ),

    path(
        "student/teachers/",
        StudentLeaveTeacherListView.as_view(),
        name="student-leave-teachers"
    ),

    path(
        "student/apply/",
        StudentApplyLeaveView.as_view(),
        name="student-apply-leave"
    ),

    path(
        "student/applications/",
        StudentLeaveApplicationListView.as_view(),
        name="student-leave-applications"
    ),

    path(
        "student/applications/<int:application_id>/",
        StudentLeaveApplicationDetailView.as_view(),
        name="student-leave-application-detail"
    ),

    # =====================================================
    # TEACHER
    # =====================================================

    path(
        "teacher/applications/",
        TeacherLeaveApplicationListView.as_view(),
        name="teacher-leave-applications"
    ),

    path(
        "teacher/applications/<int:application_id>/",
        TeacherLeaveApplicationDetailView.as_view(),
        name="teacher-leave-application-detail"
    ),

    path(
        "teacher/applications/<int:application_id>/approve/",
        TeacherApproveLeaveView.as_view(),
        name="teacher-approve-leave"
    ),

    path(
        "teacher/applications/<int:application_id>/reject/",
        TeacherRejectLeaveView.as_view(),
        name="teacher-reject-leave"
    ),
]