from rest_framework import serializers
from .models import Announcement


class AnnouncementSerializer(serializers.ModelSerializer):
    teacher_name = serializers.CharField(
        source="created_by.full_name",
        read_only=True
    )

    teacher_id = serializers.CharField(
        source="created_by.teacher_id",
        read_only=True
    )

    subject_name = serializers.CharField(
        source="subject.subject_name",
        read_only=True
    )

    subject_code = serializers.CharField(
        source="subject.subject_code",
        read_only=True
    )

    class Meta:
        model = Announcement
        fields = [
            "id",
            "title",
            "message",
            "subject",
            "subject_name",
            "subject_code",
            "created_by",
            "teacher_name",
            "teacher_id",
            "is_published",
            "created_at",
            "updated_at",
        ]
        read_only_fields = [
            "created_by",
            "created_at",
            "updated_at",
        ]