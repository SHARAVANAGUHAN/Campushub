from django.conf import settings
from django.db import models


class Librarian(models.Model):
    librarian_id = models.CharField(max_length=20, unique=True)
    full_name = models.CharField(max_length=100)
    email = models.EmailField(unique=True)
    secret_pin = models.CharField(max_length=20, blank=True, default="")
    invitation_code = models.CharField(max_length=20, blank=True, default="")
    is_registered = models.BooleanField(default=False)

    def __str__(self):
        return f"{self.full_name} ({self.librarian_id})"


class LibraryBook(models.Model):
    title = models.CharField(max_length=200)
    author = models.CharField(max_length=120, blank=True)
    description = models.TextField(blank=True)
    total_copies = models.PositiveIntegerField(default=1)
    pdf_file = models.FileField(upload_to="library/books/", blank=True, null=True)
    cover_image = models.FileField(upload_to="library/covers/", blank=True, null=True)
    added_by = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.PROTECT,
        related_name="library_books_added",
    )
    created_at = models.DateTimeField(auto_now_add=True)
    is_active = models.BooleanField(default=True)

    class Meta:
        ordering = ("title", "id")

    def __str__(self):
        return self.title

    @property
    def available_copies(self):
        allotted = self.loans.filter(status=BookLoan.Status.ALLOTTED).count()
        return max(0, self.total_copies - allotted)


class BookLoan(models.Model):
    class Status(models.TextChoices):
        PENDING = "pending", "Pending"
        ALLOTTED = "allotted", "Allotted"
        REJECTED = "rejected", "Rejected"
        RETURNED = "returned", "Returned"

    book = models.ForeignKey(LibraryBook, on_delete=models.PROTECT, related_name="loans")
    student = models.ForeignKey("students.Student", on_delete=models.PROTECT, related_name="book_loans")
    requested_days = models.PositiveSmallIntegerField()
    status = models.CharField(max_length=12, choices=Status.choices, default=Status.PENDING)
    requested_at = models.DateTimeField(auto_now_add=True)
    allotted_at = models.DateTimeField(blank=True, null=True)
    due_date = models.DateField(blank=True, null=True)
    reviewed_by = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.PROTECT,
        related_name="book_loans_reviewed",
        blank=True,
        null=True,
    )
    returned_at = models.DateTimeField(blank=True, null=True)

    class Meta:
        ordering = ("status", "requested_at")
        constraints = [
            models.UniqueConstraint(
                fields=("book", "student"),
                condition=models.Q(status__in=("pending", "allotted")),
                name="unique_open_book_request_per_student",
            )
        ]

    def __str__(self):
        return f"{self.student.student_id}: {self.book.title} ({self.status})"
