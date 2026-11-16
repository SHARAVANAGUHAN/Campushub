import django.db.models.deletion
from django.conf import settings
from django.db import migrations, models


class Migration(migrations.Migration):
    initial = True
    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ("students", "0001_initial"),
    ]
    operations = [
        migrations.CreateModel(
            name="Librarian",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("librarian_id", models.CharField(max_length=20, unique=True)),
                ("full_name", models.CharField(max_length=100)),
                ("email", models.EmailField(max_length=254, unique=True)),
                ("secret_pin", models.CharField(max_length=20)),
                ("invitation_code", models.CharField(default="LIBRARY2026", max_length=20)),
                ("is_registered", models.BooleanField(default=False)),
            ],
        ),
        migrations.CreateModel(
            name="LibraryBook",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("title", models.CharField(max_length=200)),
                ("author", models.CharField(blank=True, max_length=120)),
                ("description", models.TextField(blank=True)),
                ("total_copies", models.PositiveIntegerField(default=1)),
                ("pdf_file", models.FileField(blank=True, null=True, upload_to="library/books/")),
                ("created_at", models.DateTimeField(auto_now_add=True)),
                ("is_active", models.BooleanField(default=True)),
                ("added_by", models.ForeignKey(on_delete=django.db.models.deletion.PROTECT, related_name="library_books_added", to=settings.AUTH_USER_MODEL)),
            ],
            options={"ordering": ("title", "id")},
        ),
        migrations.CreateModel(
            name="BookLoan",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("requested_days", models.PositiveSmallIntegerField()),
                ("status", models.CharField(choices=[("pending", "Pending"), ("allotted", "Allotted"), ("rejected", "Rejected"), ("returned", "Returned")], default="pending", max_length=12)),
                ("requested_at", models.DateTimeField(auto_now_add=True)),
                ("allotted_at", models.DateTimeField(blank=True, null=True)),
                ("due_date", models.DateField(blank=True, null=True)),
                ("returned_at", models.DateTimeField(blank=True, null=True)),
                ("book", models.ForeignKey(on_delete=django.db.models.deletion.PROTECT, related_name="loans", to="library.librarybook")),
                ("reviewed_by", models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.PROTECT, related_name="book_loans_reviewed", to=settings.AUTH_USER_MODEL)),
                ("student", models.ForeignKey(on_delete=django.db.models.deletion.PROTECT, related_name="book_loans", to="students.student")),
            ],
            options={"ordering": ("status", "requested_at")},
        ),
        migrations.AddConstraint(
            model_name="bookloan",
            constraint=models.UniqueConstraint(
                condition=models.Q(status__in=("pending", "allotted")),
                fields=("book", "student"),
                name="unique_open_book_request_per_student",
            ),
        ),
    ]
