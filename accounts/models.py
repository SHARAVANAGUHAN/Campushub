from django.conf import settings
from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):

    ROLE_CHOICES = (
        ("student", "Student"),
        ("teacher", "Teacher"),
        ("librarian", "Librarian"),
        ("admin", "Admin"),
    )

    role = models.CharField(
        max_length=20,
        choices=ROLE_CHOICES
    )

    reference_id = models.CharField(
        max_length=20,
        unique=True,
        help_text="Student ID, Teacher ID, or Librarian ID"
    )

    def __str__(self):
        return f"{self.username} ({self.role})"


class PasswordResetOTP(models.Model):

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="password_reset_otps"
    )

    otp = models.CharField(
        max_length=6
    )

    created_at = models.DateTimeField(
        auto_now_add=True
    )

    is_verified = models.BooleanField(
        default=False
    )

    def __str__(self):
        return f"Password reset OTP - {self.user.username}"
