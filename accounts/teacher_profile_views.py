from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated

from teachers.models import Teacher


class TeacherProfileView(APIView):

    permission_classes = [IsAuthenticated]

    def get(self, request):

        # =====================================================
        # CHECK USER ROLE
        # =====================================================

        if request.user.role != "teacher":

            return Response(
                {
                    "error": "Teacher access required"
                },
                status=403
            )


        # =====================================================
        # FIND TEACHER
        # =====================================================

        try:

            teacher = Teacher.objects.get(
                teacher_id=request.user.reference_id
            )

        except Teacher.DoesNotExist:

            return Response(
                {
                    "error": "Teacher record not found"
                },
                status=404
            )


        # =====================================================
        # TEACHER PROFILE
        # =====================================================

        return Response({

            "teacher": {

                "teacher_id":
                    teacher.teacher_id,

                "full_name":
                    teacher.full_name,

                "email":
                    teacher.email,

                "department":
                    teacher.department,

            }

        })