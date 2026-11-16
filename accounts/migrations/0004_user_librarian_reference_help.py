from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [("accounts", "0003_user_librarian_role")]

    operations = [
        migrations.AlterField(
            model_name="user",
            name="reference_id",
            field=models.CharField(
                help_text="Student ID, Teacher ID, or Librarian ID",
                max_length=20,
                unique=True,
            ),
        ),
    ]
