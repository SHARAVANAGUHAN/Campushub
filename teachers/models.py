from django.db import models


class Teacher(models.Model):
    teacher_id = models.CharField(max_length=20, unique=True)
    full_name = models.CharField(max_length=100)
    email = models.EmailField(unique=True)
    department = models.CharField(max_length=100)
    secret_pin = models.CharField(max_length=20)
    is_registered = models.BooleanField(default=False)

    def __str__(self):
        return self.full_name