from django.db import models
from students.models import Student
from subjects.models import Subject


class Mark(models.Model):
    student = models.ForeignKey(
        Student,
        on_delete=models.CASCADE,
        related_name="marks"
    )

    subject = models.ForeignKey(
        Subject,
        on_delete=models.CASCADE,
        related_name="marks"
    )

    marks_obtained = models.DecimalField(
        max_digits=5,
        decimal_places=2
    )

    max_marks = models.DecimalField(
        max_digits=5,
        decimal_places=2,
        default=100
    )

    def __str__(self):
        return (
            f"{self.student.student_id} - "
            f"{self.subject.subject_code}"
        )