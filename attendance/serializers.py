from rest_framework import serializers

from students.models import Student
from subjects.models import Subject


class AttendanceStudentSerializer(
    serializers.Serializer
):

    student_id = serializers.CharField()

    present = serializers.BooleanField()


class MarkAttendanceSerializer(
    serializers.Serializer
):

    subject_id = serializers.IntegerField()

    date = serializers.DateField()

    students = AttendanceStudentSerializer(
        many=True
    )

    def validate_subject_id(self, value):

        try:

            Subject.objects.get(
                id=value
            )

        except Subject.DoesNotExist:

            raise serializers.ValidationError(
                "Subject not found."
            )

        return value

    def validate_students(self, value):

        if not value:

            raise serializers.ValidationError(
                "Student list cannot be empty."
            )

        student_ids = [
            item["student_id"]
            for item in value
        ]

        students = Student.objects.filter(
            student_id__in=student_ids
        )

        if students.count() != len(
            set(student_ids)
        ):

            raise serializers.ValidationError(
                "One or more students do not exist."
            )

        return value