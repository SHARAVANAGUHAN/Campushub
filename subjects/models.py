from django.db import models
from departments.models import Department


class Subject(models.Model):
    subject_name = models.CharField(max_length=100)
    subject_code = models.CharField(max_length=20, unique=True)
    semester = models.PositiveIntegerField()

    department = models.ForeignKey(
        Department,
        on_delete=models.CASCADE,
        related_name="subjects"
    )

    def __str__(self):
        return self.subject_name