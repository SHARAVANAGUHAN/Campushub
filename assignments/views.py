from datetime import datetime







from django.db.models import Count



from django.utils import timezone







from rest_framework.views import APIView



from rest_framework.response import Response



from rest_framework import status







from assignments.models import Assignment, AssignmentSubmission



from assignments.serializers import (



    AssignmentSerializer,



    AssignmentSubmissionSerializer



)







from students.models import Student



from subjects.models import Subject



from teachers.models import Teacher











# =========================================================



# GET REFERENCE ID



# =========================================================







def get_reference_id(user):







    return getattr(



        user,



        "reference_id",



        None



    )











# =========================================================



# TEACHER - CREATE ASSIGNMENT



# =========================================================







class TeacherCreateAssignmentView(APIView):







    def post(self, request):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Teacher reference ID not found."



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



                    "error":



                        "Teacher record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # GET SUBJECT



        # -------------------------------------------------







        subject_id = request.data.get(



            "subject"



        )







        if not subject_id:







            return Response(



                {



                    "error":



                        "Subject is required."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        try:







            subject = Subject.objects.get(



                id=subject_id



            )







        except Subject.DoesNotExist:







            return Response(



                {



                    "error":



                        "Subject not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # CREATE ASSIGNMENT



        # -------------------------------------------------







        serializer = AssignmentSerializer(



            data=request.data



        )











        if serializer.is_valid():







            assignment = serializer.save(



                created_by=teacher,



                subject=subject



            )











            return Response(



                {



                    "message":



                        "Assignment created successfully.",







                    "assignment":



                        AssignmentSerializer(



                            assignment



                        ).data



                },



                status=status.HTTP_201_CREATED



            )











        return Response(



            serializer.errors,



            status=status.HTTP_400_BAD_REQUEST



        )











# =========================================================



# TEACHER - ASSIGNMENT LIST



# =========================================================







class TeacherAssignmentListView(APIView):







    def get(self, request):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Teacher reference ID not found."



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



                    "error":



                        "Teacher record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # GET ASSIGNMENTS



        # -------------------------------------------------







        assignments = Assignment.objects.filter(



            created_by=teacher



        ).select_related(



            "subject",



            "created_by"



        )











        return Response(



            {



                "count":



                    assignments.count(),







                "assignments":



                    AssignmentSerializer(



                        assignments,



                        many=True



                    ).data



            }



        )











# =========================================================



# TEACHER - ASSIGNMENT SUBMISSION OVERVIEW



# =========================================================







class TeacherAssignmentSubmissionOverviewView(APIView):







    def get(self, request):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Teacher reference ID not found."



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



                    "error":



                        "Teacher record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # GET TEACHER ASSIGNMENTS



        # -------------------------------------------------







        assignments = (



            Assignment.objects



            .filter(



                created_by=teacher



            )



            .select_related(



                "subject",



                "created_by"



            )



            .annotate(



                submission_count=Count(



                    "submissions"



                )



            )



        )











        result = []











        # -------------------------------------------------



        # BUILD OVERVIEW



        # -------------------------------------------------







        for assignment in assignments:







            submissions = (



                AssignmentSubmission.objects



                .filter(



                    assignment=assignment



                )



            )











            result.append(



                {



                    "id":



                        assignment.id,







                    "title":



                        assignment.title,







                    "subject_name":



                        assignment.subject.subject_name,







                    "subject_code":



                        assignment.subject.subject_code,







                    "due_date":



                        assignment.due_date,







                    "due_time":



                        assignment.due_time,







                    "max_marks":



                        assignment.max_marks,







                    "is_published":



                        assignment.is_published,







                    "submission_count":



                        submissions.count(),







                    "submitted_count":



                        submissions.filter(



                            status="submitted"



                        ).count(),







                    "late_count":



                        submissions.filter(



                            status="late"



                        ).count(),







                    "graded_count":



                        submissions.filter(



                            status="graded"



                        ).count(),







                    "returned_count":



                        submissions.filter(



                            status="returned"



                        ).count(),



                }



            )











        return Response(



            {



                "count":



                    len(result),







                "assignments":



                    result



            }



        )











# =========================================================



# TEACHER - VIEW STUDENT SUBMISSIONS



# =========================================================







class TeacherAssignmentSubmissionsView(APIView):







    def get(



            self,



            request,



            assignment_id



    ):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Teacher reference ID not found."



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



                    "error":



                        "Teacher record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # FIND ASSIGNMENT



        # -------------------------------------------------







        try:







            assignment = (



                Assignment.objects



                .select_related(



                    "subject",



                    "created_by"



                )



                .get(



                    id=assignment_id,



                    created_by=teacher



                )



            )







        except Assignment.DoesNotExist:







            return Response(



                {



                    "error":



                        "Assignment not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # GET SUBMISSIONS



        # -------------------------------------------------







        submissions = (



            AssignmentSubmission.objects



            .filter(



                assignment=assignment



            )



            .select_related(



                "student",



                "assignment",



                "assignment__subject"



            )



            .order_by(



                "-submitted_at"



            )



        )











        # -------------------------------------------------



        # SERIALIZE



        # -------------------------------------------------







        serializer = AssignmentSubmissionSerializer(



            submissions,



            many=True,



            context={



                "request": request



            }



        )











        return Response(



            {



                "assignment": {







                    "id":



                        assignment.id,







                    "title":



                        assignment.title,







                    "subject_name":



                        assignment.subject.subject_name,







                    "subject_code":



                        assignment.subject.subject_code,







                    "max_marks":



                        assignment.max_marks,







                    "due_date":



                        assignment.due_date,







                    "due_time":



                        assignment.due_time,



                },







                "count":



                    submissions.count(),







                "submissions":



                    serializer.data



            }



        )











# =========================================================



# STUDENT - ASSIGNMENT LIST



# =========================================================







class StudentAssignmentListView(APIView):

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
        # STUDENT COURSE + SEMESTER
        # -------------------------------------------------

        course_code = student.course_code

        current_semesters = student.current_semesters

        # -------------------------------------------------
        # GET PUBLISHED ASSIGNMENTS
        # -------------------------------------------------

        assignments = Assignment.objects.filter(
            subject__department__department_code__iexact=course_code,
            subject__semester__in=current_semesters,
            is_published=True
        ).select_related(
            "subject",
            "created_by"
        )

        # -------------------------------------------------
        # FIND SUBMISSIONS FOR THIS STUDENT
        # -------------------------------------------------

        submissions = AssignmentSubmission.objects.filter(
            student=student,
            assignment__in=assignments
        ).select_related(
            "assignment"
        )

        # Create a quick lookup:
        # assignment_id -> submission
        submission_map = {
            submission.assignment_id: submission
            for submission in submissions
        }

        # -------------------------------------------------
        # CALCULATE COUNTS
        # -------------------------------------------------

        total_assignments = assignments.count()

        submitted_count = len(submission_map)

        pending_count = max(
            0,
            total_assignments - submitted_count
        )

        # -------------------------------------------------
        # BUILD ASSIGNMENT RESPONSE
        # -------------------------------------------------

        assignment_data = AssignmentSerializer(
            assignments,
            many=True
        ).data

        for assignment_item in assignment_data:

            assignment_id = assignment_item["id"]

            submission = submission_map.get(
                assignment_id
            )

            if submission:

                assignment_item["submitted"] = True

                assignment_item["submission_status"] = (
                    submission.status
                )

                assignment_item["submitted_at"] = (
                    submission.submitted_at
                )

                assignment_item["marks_obtained"] = (
                    submission.marks_obtained
                )

                assignment_item["feedback"] = (
                    submission.feedback
                )

            else:

                assignment_item["submitted"] = False

                assignment_item["submission_status"] = None

                assignment_item["submitted_at"] = None

                assignment_item["marks_obtained"] = None

                assignment_item["feedback"] = ""

        # -------------------------------------------------
        # RESPONSE
        # -------------------------------------------------

        return Response(
            {
                "count": total_assignments,
                "pending_count": pending_count,
                "submitted_count": submitted_count,
                "assignments": assignment_data
            }
        )


# STUDENT - ASSIGNMENT DETAIL



# =========================================================







class StudentAssignmentDetailView(APIView):







    def get(



            self,



            request,



            assignment_id



    ):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Student reference ID not found."



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



                    "error":



                        "Student record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # FIND ASSIGNMENT



        # -------------------------------------------------







        try:







            assignment = (



                Assignment.objects



                .select_related(



                    "subject",



                    "created_by",



                    "subject__department"



                )



                .get(



                    id=assignment_id,



                    is_published=True



                )



            )







        except Assignment.DoesNotExist:







            return Response(



                {



                    "error":



                        "Assignment not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # CHECK COURSE



        # -------------------------------------------------







        department_code = (



            assignment



            .subject



            .department



            .department_code



        )











        if not department_code:







            return Response(



                {



                    "error":



                        "Assignment department is not configured."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        if (



            department_code.lower()



            != student.course_code.lower()



        ):







            return Response(



                {



                    "error":



                        "You do not have access to this assignment."



                },



                status=status.HTTP_403_FORBIDDEN



            )











        # -------------------------------------------------



        # CHECK SEMESTER



        # -------------------------------------------------







        if (



            assignment.subject.semester



            not in student.current_semesters



        ):







            return Response(



                {



                    "error":



                        "You do not have access to this assignment."



                },



                status=status.HTTP_403_FORBIDDEN



            )











        # -------------------------------------------------



        # RETURN ASSIGNMENT



        # -------------------------------------------------







        return Response(



            AssignmentSerializer(



                assignment



            ).data



        )











# =========================================================



# STUDENT - CHECK SUBMISSION



# =========================================================







class StudentAssignmentSubmissionView(APIView):







    def get(



            self,



            request,



            assignment_id



    ):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Student reference ID not found."



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



                    "error":



                        "Student record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # -------------------------------------------------



        # FIND SUBMISSION



        # -------------------------------------------------







        try:







            submission = (



                AssignmentSubmission.objects



                .select_related(



                    "assignment",



                    "student",



                    "assignment__subject"



                )



                .get(



                    assignment_id=assignment_id,



                    student=student



                )



            )







        except AssignmentSubmission.DoesNotExist:







            return Response(



                {



                    "submitted":



                        False,







                    "submission":



                        None



                }



            )











        # -------------------------------------------------



        # RETURN SUBMISSION



        # -------------------------------------------------







        return Response(



            {



                "submitted":



                    True,







                "submission":



                    AssignmentSubmissionSerializer(



                        submission,



                        context={



                            "request": request



                        }



                    ).data



            }



        )











# =========================================================



# STUDENT - SUBMIT ASSIGNMENT



# =========================================================







class StudentAssignmentSubmitView(APIView):







    def post(



            self,



            request,



            assignment_id



    ):







        reference_id = get_reference_id(



            request.user



        )







        if not reference_id:







            return Response(



                {



                    "error":



                        "Student reference ID not found."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        # =================================================



        # FIND STUDENT



        # =================================================







        try:







            student = Student.objects.get(



                student_id=reference_id



            )







        except Student.DoesNotExist:







            return Response(



                {



                    "error":



                        "Student record not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # =================================================



        # FIND ASSIGNMENT



        # =================================================







        try:







            assignment = (



                Assignment.objects



                .select_related(



                    "subject",



                    "subject__department"



                )



                .get(



                    id=assignment_id,



                    is_published=True



                )



            )







        except Assignment.DoesNotExist:







            return Response(



                {



                    "error":



                        "Assignment not found."



                },



                status=status.HTTP_404_NOT_FOUND



            )











        # =================================================



        # CHECK COURSE



        # =================================================







        department_code = (



            assignment



            .subject



            .department



            .department_code



        )











        if not department_code:







            return Response(



                {



                    "error":



                        "Assignment department is not configured."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        if (



            department_code.lower()



            != student.course_code.lower()



        ):







            return Response(



                {



                    "error":



                        "You do not have access to this assignment."



                },



                status=status.HTTP_403_FORBIDDEN



            )











        # =================================================



        # CHECK SEMESTER



        # =================================================







        if (



            assignment.subject.semester



            not in student.current_semesters



        ):







            return Response(



                {



                    "error":



                        "You do not have access to this assignment."



                },



                status=status.HTTP_403_FORBIDDEN



            )











        # =================================================



        # GET UPLOADED FILE



        # =================================================







        attachment = request.FILES.get(



            "attachment"



        )











        if not attachment:







            return Response(



                {



                    "error":



                        "Please select a file to submit."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        # =================================================



        # EXISTING SUBMISSION



        # =================================================







        submission, created = (



            AssignmentSubmission.objects.get_or_create(







                assignment=assignment,







                student=student



            )



        )











        # =================================================



        # DON'T ALLOW EDIT AFTER GRADING



        # =================================================







        if (



            not created



            and submission.status



            in [



                "graded",



                "returned"



            ]



        ):







            return Response(



                {



                    "error":



                        "This assignment has already been graded."



                },



                status=status.HTTP_400_BAD_REQUEST



            )











        # =================================================



        # SAVE FILE



        # =================================================







        submission.attachment = attachment







        submission.submitted_at = (



            timezone.now()



        )











        # =================================================



        # DETERMINE LATE STATUS



        # =================================================







        current_time = (



            timezone.localtime()



        )







        due_date = (



            assignment.due_date



        )







        due_time = (



            assignment.due_time



        )











        if due_time:







            due_datetime = (



                timezone.make_aware(



                    datetime.combine(



                        due_date,



                        due_time



                    )



                )



            )







        else:







            due_datetime = (



                timezone.make_aware(



                    datetime.combine(



                        due_date,



                        datetime.max.time()



                    )



                )



            )











        # =================================================



        # STATUS



        # =================================================







        if current_time > due_datetime:







            submission.status = "late"







        else:







            submission.status = "submitted"











        # =================================================



        # SAVE SUBMISSION



        # =================================================







        submission.save()











        # =================================================



        # RESPONSE



        # =================================================







        return Response(



            {



                "message":



                    "Assignment submitted successfully.",







                "submitted":



                    True,







                "submission":



                    AssignmentSubmissionSerializer(



                        submission,



                        context={



                            "request": request



                        }



                    ).data



            },



            status=status.HTTP_201_CREATED



        )