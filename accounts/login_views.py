from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken

from .login_serializers import StudentLoginSerializer


class StudentLoginView(APIView):
    def post(self, request):

        print("REQUEST DATA:", request.data)

        serializer = StudentLoginSerializer(data=request.data)

        print("SERIALIZER VALID:", serializer.is_valid())
        print("SERIALIZER ERRORS:", serializer.errors)

        if serializer.is_valid():
            user = serializer.validated_data["user"]

            refresh = RefreshToken.for_user(user)

            return Response(
                {
                    "access": str(refresh.access_token),
                    "refresh": str(refresh),
                    "role": user.role,
                },
                status=status.HTTP_200_OK,
            )

        return Response(
            serializer.errors,
            status=status.HTTP_400_BAD_REQUEST,
        )