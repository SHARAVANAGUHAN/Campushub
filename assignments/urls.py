from django.urls import path

from .views import (
    TeacherCreateAssignmentView,
    TeacherAssignmentListView,
    TeacherAssignmentSubmissionOverviewView,
    TeacherAssignmentSubmissionsView,

    StudentAssignmentListView,
    StudentAssignmentDetailView,
    StudentAssignmentSubmissionView,
    StudentAssignmentSubmitView,
)


urlpatterns = [

    # =====================================================
    # TEACHER
    # =====================================================

    path(
        "teacher/create/",
        TeacherCreateAssignmentView.as_view(),
        name="teacher-create-assignment"
    ),

    path(
        "teacher/list/",
        TeacherAssignmentListView.as_view(),
        name="teacher-assignment-list"
    ),

    path(
        "teacher/submissions/",
        TeacherAssignmentSubmissionOverviewView.as_view(),
        name="teacher-assignment-submission-overview"
    ),

    path(
        "teacher/<int:assignment_id>/submissions/",
        TeacherAssignmentSubmissionsView.as_view(),
        name="teacher-assignment-submissions"
    ),


    # =====================================================
    # STUDENT
    # =====================================================

    path(
        "student/list/",
        StudentAssignmentListView.as_view(),
        name="student-assignment-list"
    ),

    path(
        "student/<int:assignment_id>/",
        StudentAssignmentDetailView.as_view(),
        name="student-assignment-detail"
    ),

    path(
        "student/<int:assignment_id>/submission/",
        StudentAssignmentSubmissionView.as_view(),
        name="student-assignment-submission"
    ),

    path(
        "student/<int:assignment_id>/submit/",
        StudentAssignmentSubmitView.as_view(),
        name="student-submit-assignment"
    ),

]