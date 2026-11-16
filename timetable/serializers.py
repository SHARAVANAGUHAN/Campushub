from rest_framework import serializers

from .models import Timetable


class TimetableSerializer(serializers.ModelSerializer):

    subject_name = serializers.CharField(
        source="subject.subject_name",
        read_only=True
    )

    subject_code = serializers.CharField(
        source="subject.subject_code",
        read_only=True
    )

    teacher_name = serializers.CharField(
        source="teacher.full_name",
        read_only=True
    )

    class Meta:
        model = Timetable

        fields = [
            "id",
            "day",
            "period_number",
            "start_time",
            "end_time",
            "subject",
            "subject_name",
            "subject_code",
            "teacher",
            "teacher_name",
            "room",
            "section",
            "is_active",
            "created_at",
            "updated_at",
        ]

        read_only_fields = [
            "id",
            "created_at",
            "updated_at",
        ]