from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated

from students.models import Student
from subjects.models import Subject
from departments.models import Department
from attendance.models import Attendance
from marks.models import Mark
from assignments.models import Assignment, AssignmentSubmission


class StudentDashboardView(APIView):

    permission_classes = [IsAuthenticated]

    def get(self, request):

        # =====================================================
        # CHECK USER ROLE
        # =====================================================

        if request.user.role != "student":

            return Response(
                {
                    "error": "Student access required"
                },
                status=403
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
                    "error": "Student record not found"
                },
                status=404
            )

        # =====================================================
        # STUDENT INFORMATION
        # =====================================================

        course_code = student.course_code
        course_name = student.course_name
        admission_year = student.admission_year
        current_year = student.current_year
        year_text = student.current_year_text
        current_semesters = student.current_semesters

        # =====================================================
        # DEPARTMENT INFORMATION
        # =====================================================

        department = Department.objects.filter(
            department_code__iexact=course_code
        ).first()

        if department:

            department_name = department.department_name

        else:

            department_name = ""

        # =====================================================
        # CURRENT SUBJECTS
        # =====================================================

        subjects = Subject.objects.filter(
            department__department_code=course_code.lower(),
            semester__in=current_semesters
        ).order_by(
            "semester",
            "subject_code"
        )

        subjects_data = []

        # =====================================================
        # SUBJECT-WISE ATTENDANCE
        # =====================================================

        for subject in subjects:

            attendance_record = Attendance.objects.filter(
                student=student,
                subject=subject
            ).first()

            if attendance_record:

                attended_classes = attendance_record.attended_classes

                total_classes = attendance_record.total_classes

                if total_classes > 0:

                    percentage = round(
                        (
                            attended_classes
                            / total_classes
                        ) * 100,
                        2
                    )

                else:

                    percentage = 0

            else:

                attended_classes = 0
                total_classes = 0
                percentage = 0

            subjects_data.append({

                "subject_id":
                    subject.id,

                "subject_code":
                    subject.subject_code,

                "subject_name":
                    subject.subject_name,

                "semester":
                    subject.semester,

                "attended_classes":
                    attended_classes,

                "total_classes":
                    total_classes,

                "percentage":
                    percentage,

            })

        # =====================================================
        # CURRENT SEMESTER ATTENDANCE
        # =====================================================

        attendance_records = Attendance.objects.filter(
            student=student,
            subject__department__department_code=course_code.lower(),
            subject__semester__in=current_semesters
        )

        total_attended = sum(
            record.attended_classes
            for record in attendance_records
        )

        total_classes = sum(
            record.total_classes
            for record in attendance_records
        )

        # =====================================================
        # OVERALL ATTENDANCE
        # =====================================================

        if total_classes > 0:

            attendance_percentage = round(
                (
                    total_attended
                    / total_classes
                ) * 100,
                2
            )

        else:

            attendance_percentage = 0

        # =====================================================
        # CURRENT SEMESTER MARKS
        # =====================================================

        marks = Mark.objects.filter(
            student=student,
            subject__department__department_code=course_code.lower(),
            subject__semester__in=current_semesters
        ).select_related(
            "subject"
        )

        marks_data = []

        for mark in marks:

            marks_data.append({

                "subject":
                    mark.subject.subject_name,

                "subject_code":
                    mark.subject.subject_code,

                "semester":
                    mark.subject.semester,

                "marks_obtained":
                    float(mark.marks_obtained),

                "max_marks":
                    float(mark.max_marks),

            })

        # =====================================================
        # CURRENT PUBLISHED ASSIGNMENTS
        # =====================================================

        current_assignments = Assignment.objects.filter(
            subject__department__department_code=course_code.lower(),
            subject__semester__in=current_semesters,
            is_published=True
        )

        # =====================================================
        # FIND ASSIGNMENTS ALREADY SUBMITTED
        # =====================================================

        submitted_assignment_ids = AssignmentSubmission.objects.filter(
            student=student,
            assignment__in=current_assignments
        ).values_list(
            "assignment_id",
            flat=True
        )

        # =====================================================
        # PENDING ASSIGNMENTS
        # =====================================================

        pending_assignments = current_assignments.exclude(
            id__in=submitted_assignment_ids
        ).count()

        # =====================================================
        # RESPONSE
        # =====================================================

        return Response({

            # =================================================
            # STUDENT
            # =================================================

            "student": {

                # -----------------------------
                # BASIC INFORMATION
                # -----------------------------

                "student_id":
                    student.student_id,

                "full_name":
                    student.full_name,

                "email":
                    student.email,

                "dob":
                    (
                        student.dob.strftime("%d / %m / %Y")
                        if student.dob
                        else ""
                    ),

                # -----------------------------
                # COURSE INFORMATION
                # -----------------------------

                "course_code":
                    course_code,

                "course":
                    course_name,

                "department":
                    department_name,

                # -----------------------------
                # ACADEMIC INFORMATION
                # -----------------------------

                "admission_year":
                    admission_year,

                "current_year":
                    current_year,

                "year_text":
                    year_text,

                "current_semesters":
                    current_semesters,

            },

            # =================================================
            # CURRENT SUBJECTS
            # =================================================

            "subjects":
                subjects_data,

            # =================================================
            # OVERALL ATTENDANCE
            # =================================================

            "attendance":
                attendance_percentage,

            "attended_classes":
                total_attended,

            "total_classes":
                total_classes,

            "missed_classes":
                max(
                    0,
                    total_classes - total_attended
                ),

            # =================================================
            # ASSIGNMENTS
            # =================================================

            "pending_assignments":
                pending_assignments,

            # =================================================
            # CURRENT SEMESTER MARKS
            # =================================================

            "marks":
                marks_data,

        })