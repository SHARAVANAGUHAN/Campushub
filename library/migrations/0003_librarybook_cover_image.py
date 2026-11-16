from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [("library", "0002_librarian_self_registration")]

    operations = [
        migrations.AddField(
            model_name="librarybook",
            name="cover_image",
            field=models.FileField(blank=True, null=True, upload_to="library/covers/"),
        ),
    ]
