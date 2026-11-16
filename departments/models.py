from django.db import models


class Department(models.Model):
    department_name = models.CharField(max_length=100, unique=True)
    department_code = models.CharField(max_length=10, unique=True)
    hod_name = models.CharField(max_length=100)

    def __str__(self):
        return self.department_name