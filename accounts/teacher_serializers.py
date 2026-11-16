from rest_framework import serializers
from django.contrib.auth import get_user_model
from teachers.models import Teacher

User = get_user_model()


class TeacherSignupSerializer(serializers.Serializer):
    invitation_code = serializers.CharField(max_length=20)
    teacher_id = serializers.CharField(max_length=20)
    full_name = serializers.CharField(max_length=100)
    email = serializers.EmailField()
    secret_pin = serializers.CharField(max_length=20)
    password = serializers.CharField(write_only=True)

    def validate(self, attrs):

        if attrs["invitation_code"] != "TEACHER2026":
            raise serializers.ValidationError(
                {"invitation_code": "Invalid invitation code."}
            )

        try:
            teacher = Teacher.objects.get(teacher_id=attrs["teacher_id"])
        except Teacher.DoesNotExist:
            raise serializers.ValidationError(
                {"teacher_id": "Invalid Teacher ID."}
            )

        if teacher.full_name != attrs["full_name"]:
            raise serializers.ValidationError(
                {"full_name": "Full name does not match."}
            )

        if teacher.email != attrs["email"]:
            raise serializers.ValidationError(
                {"email": "Email does not match."}
            )

        if teacher.secret_pin != attrs["secret_pin"]:
            raise serializers.ValidationError(
                {"secret_pin": "Secret PIN does not match."}
            )

        if teacher.is_registered:
            raise serializers.ValidationError(
                {"teacher_id": "Teacher already registered."}
            )
        if User.objects.filter(
           reference_id=teacher.teacher_id
            ).exists():
         raise serializers.ValidationError(
        {
            "teacher_id":
            "An account already exists for this Teacher ID."
        }
    )
        attrs["teacher"] = teacher
        return attrs

    def save(self):
        teacher = self.validated_data["teacher"]

        user = User.objects.create_user(
            username=teacher.teacher_id,
            password=self.validated_data["password"],
            role="teacher",
            reference_id=teacher.teacher_id,
        )

        teacher.is_registered = True
        teacher.save()

        return user