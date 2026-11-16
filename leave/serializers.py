from rest_framework import serializers

from leave.models import LeaveApplication
from students.models import Student
from teachers.models import Teacher
from departments.models import Department


class LeaveDepartmentSerializer(serializers.ModelSerializer):

    class Meta:
        model = Department
        fields = [
            "id",
            "department_name",
            "department_code",
        ]


class LeaveTeacherSerializer(serializers.ModelSerializer):

    class Meta:
        model = Teacher
        fields = [
            "id",
            "teacher_id",
            "full_name",
            "department",
        ]


class LeaveApplicationSerializer(serializers.ModelSerializer):

    student_name = serializers.CharField(
        source="student.full_name",
        read_only=True
    )

    student_id = serializers.CharField(
        source="student.student_id",
        read_only=True
    )

    student_department = serializers.CharField(
        source="student.course_code",
        read_only=True
    )

    student_year = serializers.CharField(
        source="student.current_year_text",
        read_only=True
    )

    student_semesters = serializers.SerializerMethodField()

    student_section = serializers.CharField(
        source="student.section",
        read_only=True
    )

    department_name = serializers.CharField(
        source="department.department_name",
        read_only=True
    )

    department_code = serializers.CharField(
        source="department.department_code",
        read_only=True
    )

    teacher_name = serializers.CharField(
        source="class_teacher.full_name",
        read_only=True
    )

    teacher_id = serializers.CharField(
        source="class_teacher.teacher_id",
        read_only=True
    )

    attachment_url = serializers.SerializerMethodField()

    total_days = serializers.IntegerField(
        read_only=True
    )

    class Meta:

        model = LeaveApplication

        fields = [
            "id",

            # Student details
            "student",
            "student_name",
            "student_id",
            "student_department",
            "student_year",
            "student_semesters",
            "student_section",

            # Selected department
            "department",
            "department_name",
            "department_code",

            # Selected teacher
            "class_teacher",
            "teacher_name",
            "teacher_id",

            # Leave details
            "from_date",
            "to_date",
            "total_days",
            "reason",
            "attachment",
            "attachment_url",

            # Status
            "status",
            "teacher_remarks",

            # Timestamps
            "applied_at",
            "reviewed_at",
            "updated_at",
        ]

        read_only_fields = [
            "id",
            "student",
            "student_name",
            "student_id",
            "student_department",
            "student_year",
            "student_semesters",
            "student_section",
            "department_name",
            "department_code",
            "teacher_name",
            "teacher_id",
            "total_days",
            "status",
            "teacher_remarks",
            "applied_at",
            "reviewed_at",
            "updated_at",
            "attachment_url",
        ]

    def get_student_semesters(self, obj):

        return obj.student.current_semesters

    def get_attachment_url(self, obj):

        if not obj.attachment:
            return None

        request = self.context.get("request")

        if request:
            return request.build_absolute_uri(
                obj.attachment.url
            )

        return obj.attachment.url