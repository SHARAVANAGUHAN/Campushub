from django.contrib.auth import authenticate
from rest_framework import serializers


class StudentLoginSerializer(serializers.Serializer):
    student_id = serializers.CharField()
    password = serializers.CharField(write_only=True)

    def validate(self, attrs):
        user = authenticate(
            username=attrs["student_id"],
            password=attrs["password"],
        )

        if not user:
            raise serializers.ValidationError(
                "Invalid Student ID or Password."
            )

        if user.role != "student":
            raise serializers.ValidationError(
                "This login is only for students."
            )

        attrs["user"] = user
        return attrs