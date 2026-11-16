from datetime import timedelta

from django.db import IntegrityError, transaction
from django.http import FileResponse
from django.shortcuts import get_object_or_404
from django.utils import timezone
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from students.models import Student
from .models import BookLoan, LibraryBook
from .serializers import BookLoanSerializer, LibraryBookSerializer


def role_error(role):
    return Response({"error": f"This action is only available to {role}s."}, status=403)


class LibraryBookListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        if request.user.role not in ("student", "librarian"):
            return role_error("student or librarian")
        books = LibraryBook.objects.filter(is_active=True).prefetch_related("loans")
        return Response(LibraryBookSerializer(books, many=True, context={"request": request}).data)

    def post(self, request):
        if request.user.role != "librarian":
            return role_error("librarian")
        title = request.data.get("title", "").strip()
        if not title:
            return Response({"title": "Title is required."}, status=400)
        try:
            copies = int(request.data.get("total_copies", 1))
        except (TypeError, ValueError):
            return Response({"total_copies": "Enter a valid number."}, status=400)
        if copies < 1 or copies > 10000:
            return Response({"total_copies": "Copies must be between 1 and 10000."}, status=400)
        cover_image = request.FILES.get("cover_image")
        if cover_image and not (cover_image.content_type or "").startswith("image/"):
            return Response({"cover_image": "Choose a valid image file for the book cover."}, status=400)
        book = LibraryBook.objects.create(
            title=title,
            author=request.data.get("author", "").strip(),
            description=request.data.get("description", "").strip(),
            total_copies=copies,
            pdf_file=request.FILES.get("pdf_file"),
            cover_image=cover_image,
            added_by=request.user,
        )
        return Response(LibraryBookSerializer(book, context={"request": request}).data, status=201)


class StudentBookRequestView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        if request.user.role == "student":
            loans = BookLoan.objects.filter(student__student_id=request.user.reference_id)
        elif request.user.role == "librarian":
            requested_status = request.query_params.get("status", BookLoan.Status.PENDING)
            if requested_status not in (BookLoan.Status.PENDING, BookLoan.Status.ALLOTTED):
                return Response({"error": "Unsupported request status."}, status=400)
            loans = BookLoan.objects.filter(status=requested_status)
        else:
            return role_error("student or librarian")
        loans = loans.select_related("student", "book")
        return Response(BookLoanSerializer(loans, many=True).data)

    def post(self, request):
        if request.user.role != "student":
            return role_error("student")
        try:
            book_id = int(request.data.get("book_id"))
            days = int(request.data.get("requested_days"))
        except (TypeError, ValueError):
            return Response({"error": "Select a book and a valid loan period."}, status=400)
        if days < 1 or days > 30:
            return Response({"requested_days": "Choose a period from 1 to 30 days."}, status=400)
        book = get_object_or_404(LibraryBook, id=book_id, is_active=True)
        student = get_object_or_404(Student, student_id=request.user.reference_id)
        if book.available_copies < 1:
            return Response({"error": "This book is currently unavailable."}, status=409)
        try:
            loan = BookLoan.objects.create(book=book, student=student, requested_days=days)
        except IntegrityError:
            return Response({"error": "You already have an open request for this book."}, status=409)
        return Response(BookLoanSerializer(loan).data, status=201)


class BookRequestReviewView(APIView):
    permission_classes = [IsAuthenticated]

    def patch(self, request, request_id):
        if request.user.role != "librarian":
            return role_error("librarian")
        action = request.data.get("action")
        if action not in ("allot", "reject"):
            return Response({"action": "Choose allot or reject."}, status=400)
        with transaction.atomic():
            loan = get_object_or_404(
                BookLoan.objects.select_for_update().select_related("book"),
                id=request_id,
                status=BookLoan.Status.PENDING,
            )
            if action == "allot":
                book = LibraryBook.objects.select_for_update().get(id=loan.book_id)
                if book.available_copies < 1:
                    return Response({"error": "No copies are currently available."}, status=409)
                loan.status = BookLoan.Status.ALLOTTED
                loan.allotted_at = timezone.now()
                loan.due_date = timezone.localdate() + timedelta(days=loan.requested_days)
            else:
                loan.status = BookLoan.Status.REJECTED
            loan.reviewed_by = request.user
            loan.save(update_fields=("status", "allotted_at", "due_date", "reviewed_by"))
        return Response(BookLoanSerializer(loan).data)


class ReturnBookView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request, request_id):
        if request.user.role != "librarian":
            return role_error("librarian")
        loan = get_object_or_404(BookLoan, id=request_id, status=BookLoan.Status.ALLOTTED)
        loan.status = BookLoan.Status.RETURNED
        loan.returned_at = timezone.now()
        loan.reviewed_by = request.user
        loan.save(update_fields=("status", "returned_at", "reviewed_by"))
        return Response(BookLoanSerializer(loan).data)


class AllottedBookPdfView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, book_id):
        if request.user.role != "student":
            return Response({"error": "Only students can read allotted books."}, status=403)
        loan = BookLoan.objects.filter(
            book_id=book_id,
            student__student_id=request.user.reference_id,
            status=BookLoan.Status.ALLOTTED,
            due_date__gte=timezone.localdate(),
        ).select_related("book").first()
        if not loan or not loan.book.pdf_file:
            return Response({"error": "This book has not been allotted to you."}, status=403)
        return FileResponse(loan.book.pdf_file.open("rb"), content_type="application/pdf")
