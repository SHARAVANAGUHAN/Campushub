from django.urls import path
from .views import (
    AllottedBookPdfView,
    BookRequestReviewView,
    LibraryBookListView,
    ReturnBookView,
    StudentBookRequestView,
)

urlpatterns = [
    path("books/", LibraryBookListView.as_view(), name="library-books"),
    path("books/<int:book_id>/pdf/", AllottedBookPdfView.as_view(), name="library-book-pdf"),
    path("requests/", StudentBookRequestView.as_view(), name="library-requests"),
    path("requests/<int:request_id>/review/", BookRequestReviewView.as_view(), name="library-request-review"),
    path("requests/<int:request_id>/return/", ReturnBookView.as_view(), name="library-return-book"),
]
