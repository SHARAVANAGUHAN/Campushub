from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [("accounts", "0002_passwordresetotp")]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="role",
            field=models.CharField(
                choices=[("student", "Student"), ("teacher", "Teacher"), ("librarian", "Librarian"), ("admin", "Admin")],
                max_length=20,
            ),
        ),
    ]
