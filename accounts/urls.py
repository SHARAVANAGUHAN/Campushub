from django.urls import path
from .views import StudentSignupView

urlpatterns = [
    path(
        "student/signup/",
        StudentSignupView.as_view(),
        name="student-signup",
    ),
]
from .login_views import StudentLoginView
urlpatterns += [
    path(
        "student/login/",
        StudentLoginView.as_view(),
        name="student-login",
    ),
]

from .librarian_views import LibrarianSignupView, LibrarianLoginView, LibrarianProfileView

urlpatterns += [
    path("librarian/signup/", LibrarianSignupView.as_view(), name="librarian-signup"),
    path("librarian/login/", LibrarianLoginView.as_view(), name="librarian-login"),
    path("librarian/profile/", LibrarianProfileView.as_view(), name="librarian-profile"),
]
from .teacher_views import TeacherSignupView
urlpatterns += [
    path(
        "teacher/signup/",
        TeacherSignupView.as_view(),
        name="teacher-signup",
    ),
]
from .teacher_login_views import TeacherLoginView

urlpatterns += [
    path(
        "teacher/login/",
        TeacherLoginView.as_view(),
        name="teacher-login",
    ),
]
from .student_dashboard_views import StudentDashboardView

urlpatterns += [
    path(
        "student/dashboard/",
        StudentDashboardView.as_view(),
        name="student-dashboard",
    ),
]
from .teacher_profile_views import TeacherProfileView

urlpatterns += [
    path(
        "teacher/profile/",
        TeacherProfileView.as_view(),
        name="teacher-profile",
    ),
]
from .password_reset_views import (
    ForgotPasswordView,
    VerifyPasswordOTPView,
    ResetPasswordView,
)

urlpatterns += [
    path(
        "password/forgot/",
        ForgotPasswordView.as_view(),
        name="password-forgot"
    ),
    path(
        "password/verify-otp/",
        VerifyPasswordOTPView.as_view(),
        name="password-verify-otp"
    ),
    path(
        "password/reset/",
        ResetPasswordView.as_view(),
        name="password-reset"
    ),
]
