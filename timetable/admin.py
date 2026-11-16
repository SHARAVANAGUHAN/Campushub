from django.contrib import admin

from .models import Timetable


@admin.register(Timetable)
class TimetableAdmin(admin.ModelAdmin):

    list_display = (
        "day",
        "period_number",
        "subject",
        "teacher",
        "start_time",
        "end_time",
        "room",
        "section",
        "is_active",
    )

    list_filter = (
        "day",
        "is_active",
        "section",
    )

    search_fields = (
        "subject__subject_name",
        "subject__subject_code",
        "teacher__full_name",
        "room",
        "section",
    )

    ordering = (
        "day",
        "period_number",
        "start_time",
    )