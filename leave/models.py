from django.db import models

from students.models import Student
from teachers.models import Teacher
from departments.models import Department


class LeaveApplication(models.Model):

    # =====================================================
    # STATUS
    # =====================================================

    STATUS_CHOICES = [
        ("pending", "Pending"),
        ("approved", "Approved"),
        ("rejected", "Rejected"),
    ]

    # =====================================================
    # STUDENT
    # =====================================================

    student = models.ForeignKey(
        Student,
        on_delete=models.CASCADE,
        related_name="leave_applications"
    )

    # =====================================================
    # DEPARTMENT SELECTED BY STUDENT
    # =====================================================

    department = models.ForeignKey(
        Department,
        on_delete=models.PROTECT,
        related_name="leave_applications"
    )

    # =====================================================
    # CLASS TEACHER SELECTED BY STUDENT
    # =====================================================

    class_teacher = models.ForeignKey(
        Teacher,
        on_delete=models.PROTECT,
        related_name="leave_applications"
    )

    # =====================================================
    # LEAVE DATES
    # =====================================================

    from_date = models.DateField()

    to_date = models.DateField()

    # =====================================================
    # REASON
    # =====================================================

    reason = models.TextField()

    # =====================================================
    # OPTIONAL SUPPORTING DOCUMENT
    # =====================================================

    attachment = models.FileField(
        upload_to="leave_attachments/",
        null=True,
        blank=True
    )

    # =====================================================
    # STATUS
    # =====================================================

    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default="pending"
    )

    # =====================================================
    # TEACHER REMARKS
    # =====================================================

    teacher_remarks = models.TextField(
        blank=True,
        default=""
    )

    # =====================================================
    # TIMESTAMPS
    # =====================================================

    applied_at = models.DateTimeField(
        auto_now_add=True
    )

    reviewed_at = models.DateTimeField(
        null=True,
        blank=True
    )

    updated_at = models.DateTimeField(
        auto_now=True
    )

    # =====================================================
    # TOTAL LEAVE DAYS
    # =====================================================

    @property
    def total_days(self):

        return (
            self.to_date - self.from_date
        ).days + 1

    # =====================================================
    # DISPLAY
    # =====================================================

    def __str__(self):

        return (
            f"{self.student.student_id} - "
            f"{self.from_date} to {self.to_date} - "
            f"{self.status}"
        )

    # =====================================================
    # META
    # =====================================================

    class Meta:

        ordering = ["-applied_at"]