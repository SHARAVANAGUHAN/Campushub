from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from timetable.models import Timetable
from timetable.serializers import TimetableSerializer

from students.models import Student
from teachers.models import Teacher


def get_reference_id(user):
    return getattr(user, "reference_id", None)


# =========================================================
# STUDENT - VIEW TIMETABLE
# =========================================================

class StudentTimetableView(APIView):

    def get(self, request):

        reference_id = get_reference_id(request.user)

        if not reference_id:
            return Response(
                {
                    "error": "Student reference ID not found."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # -------------------------------------------------
        # FIND STUDENT
        # -------------------------------------------------

        try:
            student = Student.objects.get(
                student_id=reference_id
            )

        except Student.DoesNotExist:

            return Response(
                {
                    "error": "Student record not found."
                },
                status=status.HTTP_404_NOT_FOUND
            )

        # -------------------------------------------------
        # GET STUDENT COURSE + SEMESTER
        # -------------------------------------------------

        course_code = student.course_code
        current_semesters = student.current_semesters

        # -------------------------------------------------
        # GET TIMETABLE
        # -------------------------------------------------

        timetable = (
            Timetable.objects
            .filter(
                subject__department__department_code__iexact=course_code,
                subject__semester__in=current_semesters,
                is_active=True
            )
            .select_related(
                "subject",
                "teacher"
            )
        )

        serializer = TimetableSerializer(
            timetable,
            many=True
        )

        return Response(
            {
                "count": timetable.count(),
                "timetable": serializer.data
            }
        )


# =========================================================
# TEACHER - VIEW TIMETABLE
# =========================================================

class TeacherTimetableView(APIView):

    def get(self, request):

        reference_id = get_reference_id(request.user)

        if not reference_id:
            return Response(
                {
                    "error": "Teacher reference ID not found."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # -------------------------------------------------
        # FIND TEACHER
        # -------------------------------------------------

        try:
            teacher = Teacher.objects.get(
                teacher_id=reference_id
            )

        except Teacher.DoesNotExist:

            return Response(
                {
                    "error": "Teacher record not found."
                },
                status=status.HTTP_404_NOT_FOUND
            )

        # -------------------------------------------------
        # GET TEACHER TIMETABLE
        # -------------------------------------------------

        timetable = (
            Timetable.objects
            .filter(
                teacher=teacher,
                is_active=True
            )
            .select_related(
                "subject",
                "teacher"
            )
        )

        serializer = TimetableSerializer(
            timetable,
            many=True
        )

        return Response(
            {
                "count": timetable.count(),
                "timetable": serializer.data
            }
        )