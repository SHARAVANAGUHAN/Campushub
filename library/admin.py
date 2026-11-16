from django.contrib import admin
from .models import BookLoan, Librarian, LibraryBook

admin.site.register(Librarian)
admin.site.register(LibraryBook)
admin.site.register(BookLoan)
