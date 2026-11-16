from rest_framework import serializers

from .models import (
    Assignment,
    AssignmentSubmission
)


# =========================================================
# ASSIGNMENT
# =========================================================

class AssignmentSerializer(
    serializers.ModelSerializer
):

    subject_name = serializers.CharField(
        source="subject.subject_name",
        read_only=True
    )

    subject_code = serializers.CharField(
        source="subject.subject_code",
        read_only=True
    )

    teacher_name = serializers.CharField(
        source="created_by.full_name",
        read_only=True
    )

    class Meta:

        model = Assignment

        fields = [

            "id",

            "subject",

            "subject_name",

            "subject_code",

            "created_by",

            "teacher_name",

            "title",

            "description",

            "due_date",

            "due_time",

            "max_marks",

            "is_published",

            "created_at",

            "updated_at",

        ]

        read_only_fields = [

            "id",

            "created_by",

            "created_at",

            "updated_at",

        ]


# =========================================================
# ASSIGNMENT SUBMISSION
# =========================================================

class AssignmentSubmissionSerializer(
    serializers.ModelSerializer
):

    student_name = serializers.CharField(
        source="student.full_name",
        read_only=True
    )

    student_id = serializers.CharField(
        source="student.student_id",
        read_only=True
    )

    assignment_title = serializers.CharField(
        source="assignment.title",
        read_only=True
    )

    subject_name = serializers.CharField(
        source="assignment.subject.subject_name",
        read_only=True
    )

    subject_code = serializers.CharField(
        source="assignment.subject.subject_code",
        read_only=True
    )

    attachment_url = serializers.SerializerMethodField()

    class Meta:

        model = AssignmentSubmission

        fields = [

            "id",

            "assignment",

            "assignment_title",

            "subject_name",

            "subject_code",

            "student",

            "student_name",

            "student_id",

            "attachment",

            "attachment_url",

            "submitted_at",

            "status",

            "marks_obtained",

            "feedback",

            "created_at",

            "updated_at",

        ]

        read_only_fields = [

            "id",

            "student",

            "submitted_at",

            "status",

            "marks_obtained",

            "feedback",

            "created_at",

            "updated_at",

        ]

    def get_attachment_url(
            self,
            obj
    ):

        if not obj.attachment:

            return None

        request = self.context.get(
            "request"
        )

        if request:

            return request.build_absolute_uri(
                obj.attachment.url
            )

        return obj.attachment.url