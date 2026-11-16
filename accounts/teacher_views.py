from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from .teacher_serializers import TeacherSignupSerializer


class TeacherSignupView(APIView):
    def post(self, request):
        serializer = TeacherSignupSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(
                {"message": "Teacher account created successfully."},
                status=status.HTTP_201_CREATED,
            )

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)