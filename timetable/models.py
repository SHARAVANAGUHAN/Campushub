from django.db import models

from subjects.models import Subject
from teachers.models import Teacher


class Timetable(models.Model):

    DAY_CHOICES = [
        ("Monday", "Monday"),
        ("Tuesday", "Tuesday"),
        ("Wednesday", "Wednesday"),
        ("Thursday", "Thursday"),
        ("Friday", "Friday"),
        ("Saturday", "Saturday"),
    ]

    day = models.CharField(
        max_length=15,
        choices=DAY_CHOICES
    )

    period_number = models.PositiveIntegerField()

    start_time = models.TimeField()

    end_time = models.TimeField()

    subject = models.ForeignKey(
        Subject,
        on_delete=models.CASCADE,
        related_name="timetable_entries"
    )

    teacher = models.ForeignKey(
        Teacher,
        on_delete=models.CASCADE,
        related_name="timetable_entries"
    )

    room = models.CharField(
        max_length=100,
        blank=True,
        default=""
    )

    section = models.CharField(
        max_length=20,
        blank=True,
        default=""
    )

    is_active = models.BooleanField(
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
            f"{self.day} - "
            f"Period {self.period_number} - "
            f"{self.subject.subject_code}"
        )

    class Meta:
        ordering = [
            "day",
            "period_number",
            "start_time"
        ]