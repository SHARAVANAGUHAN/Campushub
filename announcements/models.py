from django.db import models
from teachers.models import Teacher
from subjects.models import Subject


class Announcement(models.Model):

    title = models.CharField(max_length=200)

    message = models.TextField()

    subject = models.ForeignKey(
        Subject,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="announcements"
    )

    created_by = models.ForeignKey(
        Teacher,
        on_delete=models.CASCADE,
        related_name="announcements"
    )

    is_published = models.BooleanField(default=True)

    created_at = models.DateTimeField(auto_now_add=True)

    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title

    class Meta:
        ordering = ["-created_at"]