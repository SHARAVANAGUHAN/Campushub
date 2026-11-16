from datetime import datetime

from django.utils import timezone
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from leave.models import LeaveApplication
from leave.serializers import (
    LeaveApplicationSerializer,
    LeaveDepartmentSerializer,
    LeaveTeacherSerializer,
)
from students.models import Student
from teachers.models import Teacher
from departments.models import Department


def get_reference_id(user):
    return getattr(user, "reference_id", None)


def get_student_from_request(request):
    reference_id = get_reference_id(request.user)
    if not reference_id:
        return None, Response(
            {"error": "Student reference ID not found."},
            status=status.HTTP_400_BAD_REQUEST,
        )
    try:
        student = Student.objects.get(student_id=reference_id)
    except Student.DoesNotExist:
        return None, Response(
            {"error": "Student record not found."},
            status=status.HTTP_404_NOT_FOUND,
        )
    return student, None


def get_teacher_from_request(request):
    reference_id = get_reference_id(request.user)
    if not reference_id:
        return None, Response(
            {"error": "Teacher reference ID not found."},
            status=status.HTTP_400_BAD_REQUEST,
        )
    try:
        teacher = Teacher.objects.get(teacher_id=reference_id)
    except Teacher.DoesNotExist:
        return None, Response(
            {"error": "Teacher record not found."},
            status=status.HTTP_404_NOT_FOUND,
        )
    return teacher, None


class StudentLeaveDepartmentListView(APIView):
    def get(self, request):
        departments = Department.objects.all().order_by("department_name")
        serializer = LeaveDepartmentSerializer(departments, many=True)
        return Response(serializer.data)


class StudentLeaveTeacherListView(APIView):
    def get(self, request):
        department_id = request.query_params.get("department_id")
        if not department_id:
            return Response(
                {"error": "department_id is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        try:
            department = Department.objects.get(id=department_id)
        except Department.DoesNotExist:
            return Response(
                {"error": "Department not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        teachers = Teacher.objects.filter(
            department__iexact=department.department_code
        ).order_by("full_name")

        if not teachers.exists():
            teachers = Teacher.objects.filter(
                department__iexact=department.department_name
            ).order_by("full_name")

        serializer = LeaveTeacherSerializer(teachers, many=True)
        return Response(serializer.data)


class StudentApplyLeaveView(APIView):
    def post(self, request):
        student, error_response = get_student_from_request(request)
        if error_response:
            return error_response

        department_id = request.data.get("department")
        teacher_id = request.data.get("class_teacher")
        from_date = request.data.get("from_date")
        to_date = request.data.get("to_date")
        reason = request.data.get("reason")

        if not department_id:
            return Response(
                {"error": "Department is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        if not teacher_id:
            return Response(
                {"error": "Class teacher is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        if not from_date:
            return Response(
                {"error": "From date is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        if not to_date:
            return Response(
                {"error": "To date is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )
        if not reason or not reason.strip():
            return Response(
                {"error": "Reason is required."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        # Convert Android's YYYY-MM-DD strings into real Python date objects.
        try:
            from_date = datetime.strptime(from_date, "%Y-%m-%d").date()
            to_date = datetime.strptime(to_date, "%Y-%m-%d").date()
        except (TypeError, ValueError):
            return Response(
                {"error": "Invalid date format. Use YYYY-MM-DD."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        if to_date < from_date:
            return Response(
                {"error": "To date cannot be before from date."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        try:
            department = Department.objects.get(id=department_id)
        except Department.DoesNotExist:
            return Response(
                {"error": "Selected department not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        try:
            teacher = Teacher.objects.get(teacher_id=teacher_id)
        except Teacher.DoesNotExist:
            return Response(
                {"error": "Selected teacher not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        teacher_department = (teacher.department or "").strip().lower()
        department_code = (department.department_code or "").strip().lower()
        department_name = (department.department_name or "").strip().lower()

        if teacher_department != department_code and teacher_department != department_name:
            return Response(
                {"error": "Selected teacher does not belong to the selected department."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        application = LeaveApplication.objects.create(
            student=student,
            department=department,
            class_teacher=teacher,
            from_date=from_date,
            to_date=to_date,
            reason=reason.strip(),
            attachment=request.FILES.get("attachment"),
            status="pending",
        )

        serializer = LeaveApplicationSerializer(
            application,
            context={"request": request},
        )

        return Response(
            {
                "message": "Leave application submitted successfully.",
                "application": serializer.data,
            },
            status=status.HTTP_201_CREATED,
        )


class StudentLeaveApplicationListView(APIView):
    def get(self, request):
        student, error_response = get_student_from_request(request)
        if error_response:
            return error_response

        applications = LeaveApplication.objects.filter(
            student=student
        ).select_related(
            "student", "department", "class_teacher"
        )

        serializer = LeaveApplicationSerializer(
            applications,
            many=True,
            context={"request": request},
        )

        return Response(
            {
                "count": applications.count(),
                "applications": serializer.data,
            }
        )


class StudentLeaveApplicationDetailView(APIView):
    def get(self, request, application_id):
        student, error_response = get_student_from_request(request)
        if error_response:
            return error_response

        try:
            application = LeaveApplication.objects.select_related(
                "student", "department", "class_teacher"
            ).get(id=application_id, student=student)
        except LeaveApplication.DoesNotExist:
            return Response(
                {"error": "Leave application not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        serializer = LeaveApplicationSerializer(
            application,
            context={"request": request},
        )
        return Response(serializer.data)


class TeacherLeaveApplicationListView(APIView):
    def get(self, request):
        teacher, error_response = get_teacher_from_request(request)
        if error_response:
            return error_response

        applications = LeaveApplication.objects.filter(
            class_teacher=teacher
        ).select_related(
            "student", "department", "class_teacher"
        )

        serializer = LeaveApplicationSerializer(
            applications,
            many=True,
            context={"request": request},
        )

        return Response(
            {
                "count": applications.count(),
                "applications": serializer.data,
            }
        )


class TeacherLeaveApplicationDetailView(APIView):
    def get(self, request, application_id):
        teacher, error_response = get_teacher_from_request(request)
        if error_response:
            return error_response

        try:
            application = LeaveApplication.objects.select_related(
                "student", "department", "class_teacher"
            ).get(id=application_id, class_teacher=teacher)
        except LeaveApplication.DoesNotExist:
            return Response(
                {"error": "Leave application not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        serializer = LeaveApplicationSerializer(
            application,
            context={"request": request},
        )
        return Response(serializer.data)


class TeacherApproveLeaveView(APIView):
    def post(self, request, application_id):
        teacher, error_response = get_teacher_from_request(request)
        if error_response:
            return error_response

        try:
            application = LeaveApplication.objects.get(
                id=application_id,
                class_teacher=teacher,
            )
        except LeaveApplication.DoesNotExist:
            return Response(
                {"error": "Leave application not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        if application.status != "pending":
            return Response(
                {"error": "This leave application has already been processed."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        application.status = "approved"
        application.teacher_remarks = request.data.get(
            "teacher_remarks", ""
        ).strip()
        application.reviewed_at = timezone.now()
        application.save()

        serializer = LeaveApplicationSerializer(
            application,
            context={"request": request},
        )

        return Response(
            {
                "message": "Leave application approved.",
                "application": serializer.data,
            }
        )


class TeacherRejectLeaveView(APIView):
    def post(self, request, application_id):
        teacher, error_response = get_teacher_from_request(request)
        if error_response:
            return error_response

        try:
            application = LeaveApplication.objects.get(
                id=application_id,
                class_teacher=teacher,
            )
        except LeaveApplication.DoesNotExist:
            return Response(
                {"error": "Leave application not found."},
                status=status.HTTP_404_NOT_FOUND,
            )

        if application.status != "pending":
            return Response(
                {"error": "This leave application has already been processed."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        teacher_remarks = request.data.get(
            "teacher_remarks", ""
        ).strip()

        if not teacher_remarks:
            return Response(
                {"error": "Please provide a reason for rejecting the leave application."},
                status=status.HTTP_400_BAD_REQUEST,
            )

        application.status = "rejected"
        application.teacher_remarks = teacher_remarks
        application.reviewed_at = timezone.now()
        application.save()

        serializer = LeaveApplicationSerializer(
            application,
            context={"request": request},
        )

        return Response(
            {
                "message": "Leave application rejected.",
                "application": serializer.data,
            }
        )
