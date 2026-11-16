import random
from datetime import timedelta

from django.core.mail import send_mail
from django.utils import timezone
from django.contrib.auth import get_user_model

from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from students.models import Student
from .models import PasswordResetOTP


User = get_user_model()


class ForgotPasswordView(APIView):

    def post(self, request):

        student_id = request.data.get("student_id", "").strip()
        email = request.data.get("email", "").strip().lower()

        if not student_id or not email:
            return Response(
                {
                    "error": "Student ID and email are required."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            student = Student.objects.get(
                student_id=student_id
            )
        except Student.DoesNotExist:
            return Response(
                {
                    "error": "Invalid Student ID or email."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        if student.email.lower() != email:
            return Response(
                {
                    "error": "Invalid Student ID or email."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        if not student.is_registered:
            return Response(
                {
                    "error": "This student account is not registered."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            user = User.objects.get(
                reference_id=student.student_id,
                role="student"
            )
        except User.DoesNotExist:
            return Response(
                {
                    "error": "Student account not found."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # Delete old OTPs
        PasswordResetOTP.objects.filter(
            user=user
        ).delete()

        # Generate 6-digit OTP
        otp = str(random.randint(100000, 999999))

        PasswordResetOTP.objects.create(
            user=user,
            otp=otp
        )

        try:
            send_mail(
                subject="CampusHub Password Reset OTP",
                message=(
                    f"Hello {student.full_name},\n\n"
                    f"Your CampusHub password reset OTP is:\n\n"
                    f"{otp}\n\n"
                    f"This OTP is valid for 10 minutes.\n\n"
                    f"If you did not request a password reset, "
                    f"please ignore this email.\n\n"
                    f"CampusHub"
                ),
                from_email=None,
                recipient_list=[student.email],
                fail_silently=False,
            )

        except Exception as e:
            PasswordResetOTP.objects.filter(
                user=user
            ).delete()

            print("EMAIL ERROR:", e)

            return Response(
                {
                    "error": "Unable to send OTP email."
                },
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )

        return Response(
            {
                "message": "OTP sent successfully."
            },
            status=status.HTTP_200_OK
        )


class VerifyPasswordOTPView(APIView):

    def post(self, request):

        student_id = request.data.get(
            "student_id", ""
        ).strip()

        otp = request.data.get(
            "otp", ""
        ).strip()

        if not student_id or not otp:
            return Response(
                {
                    "error": "Student ID and OTP are required."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            user = User.objects.get(
                reference_id=student_id,
                role="student"
            )
        except User.DoesNotExist:
            return Response(
                {
                    "error": "Invalid OTP."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            reset_otp = PasswordResetOTP.objects.get(
                user=user,
                otp=otp,
                is_verified=False
            )
        except PasswordResetOTP.DoesNotExist:
            return Response(
                {
                    "error": "Invalid OTP."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # OTP expires after 10 minutes
        if timezone.now() > reset_otp.created_at + timedelta(
            minutes=10
        ):
            reset_otp.delete()

            return Response(
                {
                    "error": "OTP has expired."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        reset_otp.is_verified = True
        reset_otp.save()

        return Response(
            {
                "message": "OTP verified successfully."
            },
            status=status.HTTP_200_OK
        )


class ResetPasswordView(APIView):

    def post(self, request):

        student_id = request.data.get(
            "student_id", ""
        ).strip()

        new_password = request.data.get(
            "new_password", ""
        )

        if not student_id or not new_password:
            return Response(
                {
                    "error": "Student ID and new password are required."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        if len(new_password) < 6:
            return Response(
                {
                    "error": "Password must be at least 6 characters."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            user = User.objects.get(
                reference_id=student_id,
                role="student"
            )
        except User.DoesNotExist:
            return Response(
                {
                    "error": "Student account not found."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            reset_otp = PasswordResetOTP.objects.get(
                user=user,
                is_verified=True
            )
        except PasswordResetOTP.DoesNotExist:
            return Response(
                {
                    "error": "Please verify the OTP first."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # Make sure the verified OTP has not expired
        if timezone.now() > reset_otp.created_at + timedelta(
            minutes=10
        ):
            reset_otp.delete()

            return Response(
                {
                    "error": "OTP has expired. Please request a new OTP."
                },
                status=status.HTTP_400_BAD_REQUEST
            )

        # Change password
        user.set_password(new_password)
        user.save()

        # Delete OTP so it cannot be reused
        reset_otp.delete()

        return Response(
            {
                "message": "Password reset successfully."
            },
            status=status.HTTP_200_OK
        )