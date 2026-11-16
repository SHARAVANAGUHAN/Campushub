from django.db import transaction


from rest_framework.views import APIView

from rest_framework.response import Response

from rest_framework.permissions import IsAuthenticated

from rest_framework import status


from students.models import Student

from subjects.models import Subject

from attendance.models import Attendance


from .serializers import MarkAttendanceSerializer


# =========================================================
# TEACHER - MARK ATTENDANCE
# =========================================================


class TeacherAttendanceView(APIView):

    permission_classes = [IsAuthenticated]


    def post(self, request):

        # =====================================================
        # CHECK TEACHER
        # =====================================================

        if request.user.role != "teacher":

            return Response(
                {
                    "error": "Teacher access required."
                },
                status=status.HTTP_403_FORBIDDEN
            )


        # =====================================================
        # VALIDATE REQUEST
        # =====================================================

        serializer = MarkAttendanceSerializer(
            data=request.data
        )


        if not serializer.is_valid():

            return Response(
                serializer.errors,
                status=status.HTTP_400_BAD_REQUEST
            )


        subject_id = serializer.validated_data["subject_id"]

        attendance_date = serializer.validated_data["date"]

        students_data = serializer.validated_data["students"]


        # =====================================================
        # GET SUBJECT
        # =====================================================

        try:

            subject = Subject.objects.get(
                id=subject_id
            )

        except Subject.DoesNotExist:

            return Response(
                {
                    "error": "Subject not found."
                },
                status=status.HTTP_404_NOT_FOUND
            )


        # =====================================================
        # SAVE ATTENDANCE
        # =====================================================

        created_count = 0

        updated_count = 0


        with transaction.atomic():

            for student_data in students_data:

                student_id = student_data["student_id"]

                present = student_data["present"]


                # =================================================
                # GET STUDENT
                # =================================================

                try:

                    student = Student.objects.get(
                        student_id=student_id
                    )

                except Student.DoesNotExist:

                    return Response(
                        {
                            "error":
                                f"Student {student_id} not found."
                        },
                        status=status.HTTP_404_NOT_FOUND
                    )


                # =================================================
                # GET OR CREATE ATTENDANCE RECORD
                # =================================================

                attendance, created = (
                    Attendance.objects.get_or_create(

                        student=student,

                        subject=subject,

                        date=attendance_date,

                        defaults={

                            "attended_classes":
                                1 if present else 0,

                            "total_classes": 1,

                        }
                    )
                )


                # =================================================
                # FIRST CLASS FOR THIS DAY
                # =================================================

                if created:

                    created_count += 1


                # =================================================
                # ANOTHER CLASS
                # =================================================

                else:

                    # ---------------------------------------------
                    # ADD ONE MORE CLASS
                    # ---------------------------------------------

                    attendance.total_classes += 1


                    # ---------------------------------------------
                    # IF PRESENT, ADD ONE TO ATTENDED CLASSES
                    # ---------------------------------------------

                    if present:

                        attendance.attended_classes += 1


                    # ---------------------------------------------
                    # SAVE UPDATED COUNTS
                    # ---------------------------------------------

                    attendance.save(
                        update_fields=[
                            "attended_classes",
                            "total_classes",
                        ]
                    )


                    updated_count += 1


        # =====================================================
        # RESPONSE
        # =====================================================

        return Response(
            {
                "message":
                    "Attendance saved successfully.",

                "subject":
                    subject.subject_name,

                "subject_code":
                    subject.subject_code,

                "date":
                    str(attendance_date),

                "created":
                    created_count,

                "updated":
                    updated_count,

            },
            status=status.HTTP_200_OK
        )


# =========================================================
# TEACHER - GET STUDENTS
# =========================================================


class AttendanceStudentListView(APIView):

    permission_classes = [IsAuthenticated]


    def get(self, request):

        # =====================================================
        # CHECK TEACHER
        # =====================================================

        if request.user.role != "teacher":

            return Response(
                {
                    "error": "Teacher access required."
                },
                status=status.HTTP_403_FORBIDDEN
            )


        # =====================================================
        # GET FILTERS
        # =====================================================

        course = request.query_params.get("course")

        year = request.query_params.get("year")

        semester = request.query_params.get("semester")


        # =====================================================
        # CHECK REQUIRED PARAMETERS
        # =====================================================

        if not course or not year or not semester:

            return Response(
                {
                    "error":
                        "course, year and semester are required."
                },
                status=status.HTTP_400_BAD_REQUEST
            )


        # =====================================================
        # CONVERT YEAR AND SEMESTER
        # =====================================================

        try:

            year = int(year)

            semester = int(semester)

        except ValueError:

            return Response(
                {
                    "error":
                        "Year and semester must be numbers."
                },
                status=status.HTTP_400_BAD_REQUEST
            )


        # =====================================================
        # VALIDATE YEAR
        # =====================================================

        if year not in [1, 2, 3]:

            return Response(
                {
                    "error":
                        "Year must be 1, 2 or 3."
                },
                status=status.HTTP_400_BAD_REQUEST
            )


        # =====================================================
        # VALIDATE SEMESTER
        # =====================================================

        if semester not in [1, 2, 3, 4, 5, 6]:

            return Response(
                {
                    "error":
                        "Semester must be between 1 and 6."
                },
                status=status.HTTP_400_BAD_REQUEST
            )


        # =====================================================
        # YEAR - SEMESTER MAPPING
        # =====================================================

        semester_mapping = {

            1: [1, 2],

            2: [3, 4],

            3: [5, 6],

        }


        # =====================================================
        # CHECK WHETHER SEMESTER BELONGS TO YEAR
        # =====================================================

        if semester not in semester_mapping[year]:

            return Response(
                {
                    "error":
                        f"Semester {semester} does not "
                        f"belong to Year {year}."
                },
                status=status.HTTP_400_BAD_REQUEST
            )


        # =====================================================
        # FIND MATCHING STUDENTS
        # =====================================================

        students = Student.objects.all()

        matching_students = []


        for student in students:

            # -------------------------------------------------
            # COURSE
            # -------------------------------------------------

            if student.course_code != course.upper():

                continue


            # -------------------------------------------------
            # YEAR
            # -------------------------------------------------

            if student.current_year != year:

                continue


            # -------------------------------------------------
            # SEMESTER
            # -------------------------------------------------

            if semester not in student.current_semesters:

                continue


            # -------------------------------------------------
            # ADD STUDENT
            # -------------------------------------------------

            matching_students.append(
                {
                    "student_id":
                        student.student_id,

                    "full_name":
                        student.full_name,
                }
            )


        # =====================================================
        # RESPONSE
        # =====================================================

        return Response(
            {
                "course":
                    course.upper(),

                "year":
                    year,

                "semester":
                    semester,

                "count":
                    len(matching_students),

                "students":
                    matching_students,

            },
            status=status.HTTP_200_OK
        )


# =========================================================
# STUDENT - VIEW ATTENDANCE
# =========================================================


class StudentAttendanceListView(APIView):

    permission_classes = [IsAuthenticated]


    def get(self, request):

        # =====================================================
        # CHECK STUDENT
        # =====================================================

        if request.user.role != "student":

            return Response(
                {
                    "error":
                        "Student access required."
                },
                status=status.HTTP_403_FORBIDDEN
            )


        # =====================================================
        # FIND STUDENT
        # =====================================================

        try:

            student = Student.objects.get(
                student_id=request.user.reference_id
            )

        except Student.DoesNotExist:

            return Response(
                {
                    "error":
                        "Student record not found."
                },
                status=status.HTTP_404_NOT_FOUND
            )


        # =====================================================
        # GET ATTENDANCE
        # =====================================================

        records = Attendance.objects.filter(
            student=student
        ).select_related(
            "subject"
        )


        attendance_data = []


        for record in records:

            attendance_data.append(
                {
                    "subject_code":
                        record.subject.subject_code,

                    "subject_name":
                        record.subject.subject_name,

                    "semester":
                        record.subject.semester,

                    "date":
                        str(record.date),

                    "attended_classes":
                        record.attended_classes,

                    "total_classes":
                        record.total_classes,

                    "percentage":
                        record.percentage,

                    "status":
                        "Present"
                        if record.attended_classes > 0
                        else "Absent",
                }
            )


        # =====================================================
        # RESPONSE
        # =====================================================

        return Response(
            {
                "student_id":
                    student.student_id,

                "attendance":
                    attendance_data,
            },
            status=status.HTTP_200_OK
        )