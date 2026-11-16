from django.db import models
from students.models import Student
from subjects.models import Subject


class Attendance(models.Model):

    student = models.ForeignKey(
        Student,
        on_delete=models.CASCADE,
        related_name="attendance_records"
    )

    subject = models.ForeignKey(
        Subject,
        on_delete=models.CASCADE,
        related_name="attendance_records"
    )

    date = models.DateField()

    attended_classes = models.PositiveIntegerField(default=0)

    total_classes = models.PositiveIntegerField(default=0)

    @property
    def percentage(self):

        if self.total_classes == 0:
            return 0

        return round(
            (
                self.attended_classes
                / self.total_classes
            ) * 100,
            2
        )

    def __str__(self):

        return (
            f"{self.student.student_id} - "
            f"{self.subject.subject_code} - "
            f"{self.date}"
        )

    class Meta:

        constraints = [
            models.UniqueConstraint(
                fields=[
                    "student",
                    "subject",
                    "date"
                ],
                name="unique_student_subject_date"
            )
        ]

        ordering = [
            "-date"
        ]