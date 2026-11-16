from rest_framework import serializers
from django.utils import timezone
from .models import BookLoan, LibraryBook


class LibraryBookSerializer(serializers.ModelSerializer):
    available_copies = serializers.IntegerField(read_only=True)
    pdf_available = serializers.SerializerMethodField()
    can_read = serializers.SerializerMethodField()
    current_request_status = serializers.SerializerMethodField()
    pdf_url = serializers.SerializerMethodField()
    cover_url = serializers.SerializerMethodField()
    current_due_date = serializers.SerializerMethodField()
    pending_requests = serializers.SerializerMethodField()

    class Meta:
        model = LibraryBook
        fields = (
            "id", "title", "author", "description", "total_copies",
            "available_copies", "pdf_available", "can_read",
            "current_request_status", "current_due_date", "pdf_url", "cover_url", "pending_requests",
        )

    def _active_loan(self, book):
        user = self.context["request"].user
        if user.role != "student":
            return None
        return book.loans.filter(
            student__student_id=user.reference_id,
            status=BookLoan.Status.ALLOTTED,
            due_date__gte=timezone.localdate(),
        ).first()

    def get_pdf_available(self, book):
        return bool(book.pdf_file)

    def get_can_read(self, book):
        return bool(book.pdf_file and self._active_loan(book))

    def get_current_request_status(self, book):
        if self.context["request"].user.role != "student":
            return None
        loan = book.loans.filter(
            student__student_id=self.context["request"].user.reference_id
        ).exclude(status=BookLoan.Status.RETURNED).first()
        return loan.status if loan else None

    def get_pdf_url(self, book):
        if self.get_can_read(book):
            request = self.context["request"]
            return request.build_absolute_uri(f"/api/library/books/{book.id}/pdf/")
        return None

    def get_cover_url(self, book):
        if not book.cover_image:
            return None
        request = self.context.get("request")
        return request.build_absolute_uri(book.cover_image.url) if request else book.cover_image.url

    def get_current_due_date(self, book):
        loan = self._active_loan(book)
        return loan.due_date.isoformat() if loan and loan.due_date else None

    def get_pending_requests(self, book):
        if self.context["request"].user.role == "librarian":
            return book.loans.filter(status=BookLoan.Status.PENDING).count()
        return 0


class BookLoanSerializer(serializers.ModelSerializer):
    book_title = serializers.CharField(source="book.title", read_only=True)
    student_name = serializers.CharField(source="student.full_name", read_only=True)
    student_id = serializers.CharField(source="student.student_id", read_only=True)
    student_email = serializers.EmailField(source="student.email", read_only=True)

    class Meta:
        model = BookLoan
        fields = (
            "id", "book", "book_title", "student_name", "student_id",
            "student_email", "requested_days", "status", "requested_at",
            "allotted_at", "due_date",
        )
        read_only_fields = fields
