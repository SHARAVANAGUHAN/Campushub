from rest_framework import serializers
from django.contrib.auth import get_user_model
from students.models import Student

User = get_user_model()


class StudentSignupSerializer(serializers.Serializer):
    student_id = serializers.CharField(max_length=20)
    full_name = serializers.CharField(max_length=100)
    college_email = serializers.EmailField()
    dob = serializers.DateField()
    password = serializers.CharField(write_only=True)

    def validate(self, attrs):

        # Find student in Student table
        try:
            student = Student.objects.get(
                student_id=attrs["student_id"]
            )
        except Student.DoesNotExist:
            raise serializers.ValidationError(
                {"student_id": "Invalid Student ID."}
            )

        # Check full name
        if student.full_name != attrs["full_name"]:
            raise serializers.ValidationError(
                {"full_name": "Full name does not match."}
            )

        # Check email
        if student.email != attrs["college_email"]:
            raise serializers.ValidationError(
                {"college_email": "College email does not match."}
            )

        # Check date of birth
        if student.dob != attrs["dob"]:
            raise serializers.ValidationError(
                {"dob": "Date of birth does not match."}
            )

        # Check Student registration status
        if student.is_registered:
            raise serializers.ValidationError(
                {"student_id": "Student already registered."}
            )

        # IMPORTANT:
        # Check whether a User already exists
        if User.objects.filter(
            reference_id=student.student_id
        ).exists():
            raise serializers.ValidationError(
                {
                    "student_id":
                    "An account already exists for this Student ID."
                }
            )

        attrs["student"] = student
        return attrs

    def save(self):

        student = self.validated_data["student"]

        user = User.objects.create_user(
            username=student.student_id,
            password=self.validated_data["password"],
            role="student",
            reference_id=student.student_id,
        )

        student.is_registered = True
        student.save()

        return user