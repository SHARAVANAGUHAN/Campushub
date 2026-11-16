from django.contrib.auth import authenticate, get_user_model
from rest_framework import serializers, status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken

from library.models import Librarian

User = get_user_model()


class LibrarianSignupSerializer(serializers.Serializer):
    librarian_id = serializers.CharField(max_length=20)
    full_name = serializers.CharField(max_length=100)
    email = serializers.EmailField()
    password = serializers.CharField(min_length=8, write_only=True)

    def validate(self, attrs):
        librarian = Librarian.objects.filter(librarian_id=attrs["librarian_id"]).first()
        if (
            User.objects.filter(reference_id=attrs["librarian_id"]).exists()
            or User.objects.filter(username=attrs["librarian_id"]).exists()
            or (librarian and librarian.is_registered)
        ):
            raise serializers.ValidationError({"librarian_id": "This librarian account is already registered."})
        if librarian and librarian.full_name.strip().casefold() != attrs["full_name"].strip().casefold():
            raise serializers.ValidationError({"full_name": "This ID is already assigned to another librarian."})
        if librarian and librarian.email.casefold() != attrs["email"].casefold():
            raise serializers.ValidationError({"email": "This ID is already assigned to another librarian."})
        email_matches = Librarian.objects.filter(email__iexact=attrs["email"])
        if librarian:
            email_matches = email_matches.exclude(pk=librarian.pk)
        if email_matches.exists():
            raise serializers.ValidationError({"email": "An account already uses this email address."})
        attrs["librarian_record"] = librarian
        return attrs

    def create(self, validated_data):
        librarian = validated_data.pop("librarian_record", None)
        if librarian is None:
            librarian = Librarian.objects.create(
                librarian_id=validated_data["librarian_id"],
                full_name=validated_data["full_name"].strip(),
                email=validated_data["email"],
                is_registered=False,
            )
        user = User.objects.create_user(
            username=librarian.librarian_id,
            password=validated_data["password"],
            email=librarian.email,
            role="librarian",
            reference_id=librarian.librarian_id,
        )
        librarian.is_registered = True
        librarian.save(update_fields=("is_registered",))
        return user


class LibrarianSignupView(APIView):
    def post(self, request):
        serializer = LibrarianSignupSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        user = serializer.save()
        refresh = RefreshToken.for_user(user)
        return Response({
            "message": "Librarian account created successfully.",
            "access": str(refresh.access_token),
            "refresh": str(refresh),
            "role": user.role,
        }, status=status.HTTP_201_CREATED)


class LibrarianLoginView(APIView):
    def post(self, request):
        librarian_id = request.data.get("librarian_id")
        user = authenticate(username=librarian_id, password=request.data.get("password"))
        if not user or user.role != "librarian":
            return Response({"error": "Invalid librarian ID or password."}, status=status.HTTP_401_UNAUTHORIZED)
        refresh = RefreshToken.for_user(user)
        return Response({"access": str(refresh.access_token), "refresh": str(refresh), "role": user.role})


class LibrarianProfileView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        if request.user.role != "librarian":
            return Response({"error": "Librarian access required."}, status=status.HTTP_403_FORBIDDEN)
        librarian = Librarian.objects.filter(librarian_id=request.user.reference_id).first()
        if librarian is None:
            return Response({"error": "Librarian profile not found."}, status=status.HTTP_404_NOT_FOUND)
        return Response({
            "librarian_id": librarian.librarian_id,
            "full_name": librarian.full_name,
            "email": librarian.email,
        })
