from django.db import models

from students.models import Student
from subjects.models import Subject
from teachers.models import Teacher


class Assignment(models.Model):

    subject = models.ForeignKey(
        Subject,
        on_delete=models.CASCADE,
        related_name="assignments"
    )

    created_by = models.ForeignKey(
        Teacher,
        on_delete=models.CASCADE,
        related_name="created_assignments"
    )

    title = models.CharField(
        max_length=200
    )

    description = models.TextField(
        blank=True,
        default=""
    )

    due_date = models.DateField()

    due_time = models.TimeField(
        null=True,
        blank=True
    )

    max_marks = models.PositiveIntegerField(
        default=100
    )

    is_published = models.BooleanField(
        default=True
    )

    created_at = models.DateTimeField(
        auto_now_add=True
    )

    updated_at = models.DateTimeField(
        auto_now=True
    )

    def __str__(self):

        return (
            f"{self.subject.subject_code} - "
            f"{self.title}"
        )

    class Meta:

        ordering = [
            "-created_at"
        ]


class AssignmentSubmission(models.Model):

    STATUS_CHOICES = [

        (
            "submitted",
            "Submitted"
        ),

        (
            "late",
            "Late"
        ),

        (
            "graded",
            "Graded"
        ),

        (
            "returned",
            "Returned"
        ),

    ]

    assignment = models.ForeignKey(
        Assignment,
        on_delete=models.CASCADE,
        related_name="submissions"
    )

    student = models.ForeignKey(
        Student,
        on_delete=models.CASCADE,
        related_name="assignment_submissions"
    )

    # =====================================================
    # SUBMITTED FILE
    # =====================================================

    attachment = models.FileField(
        upload_to="assignment_submissions/",
        null=True,
        blank=True
    )

    submitted_at = models.DateTimeField(
        null=True,
        blank=True
    )

    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default="submitted"
    )

    marks_obtained = models.PositiveIntegerField(
        null=True,
        blank=True
    )

    feedback = models.TextField(
        blank=True,
        default=""
    )

    created_at = models.DateTimeField(
        auto_now_add=True
    )

    updated_at = models.DateTimeField(
        auto_now=True
    )

    def __str__(self):

        return (
            f"{self.student.student_id} - "
            f"{self.assignment.title}"
        )

    class Meta:

        ordering = [
            "-created_at"
        ]

        constraints = [

            models.UniqueConstraint(
                fields=[
                    "assignment",
                    "student"
                ],
                name="unique_assignment_student_submission"
            )

        ]