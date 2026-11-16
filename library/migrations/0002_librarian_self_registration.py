from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [("library", "0001_initial")]

    operations = [
        migrations.AlterField(
            model_name="librarian",
            name="secret_pin",
            field=models.CharField(blank=True, default="", max_length=20),
        ),
        migrations.AlterField(
            model_name="librarian",
            name="invitation_code",
            field=models.CharField(blank=True, default="", max_length=20),
        ),
    ]
