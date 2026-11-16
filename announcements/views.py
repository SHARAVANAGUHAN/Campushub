from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated

from .models import Announcement
from .serializers import AnnouncementSerializer

from teachers.models import Teacher
from subjects.models import Subject


class TeacherAnnouncementCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        teacher_id = request.data.get("teacher_id")
        title = request.data.get("title")
        message = request.data.get("message")
        subject_id = request.data.get("subject")

        if not teacher_id:
            return Response(
                {"error": "teacher_id is required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        if not title:
            return Response(
                {"error": "title is required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        if not message:
            return Response(
                {"error": "message is required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        try:
            teacher = Teacher.objects.get(
                teacher_id=teacher_id
            )
        except Teacher.DoesNotExist:
            return Response(
                {"error": "Teacher not found"},
                status=status.HTTP_404_NOT_FOUND
            )

        subject = None

        if subject_id:
            try:
                subject = Subject.objects.get(id=subject_id)
            except Subject.DoesNotExist:
                return Response(
                    {"error": "Subject not found"},
                    status=status.HTTP_404_NOT_FOUND
                )

        announcement = Announcement.objects.create(
            title=title,
            message=message,
            subject=subject,
            created_by=teacher,
            is_published=True
        )

        serializer = AnnouncementSerializer(announcement)

        return Response(
            {
                "message": "Announcement created successfully",
                "announcement": serializer.data
            },
            status=status.HTTP_201_CREATED
        )


class TeacherAnnouncementListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        teacher_id = request.query_params.get("teacher_id")

        if not teacher_id:
            return Response(
                {"error": "teacher_id is required"},
                status=status.HTTP_400_BAD_REQUEST
            )

        announcements = Announcement.objects.filter(
            created_by__teacher_id=teacher_id
        )

        serializer = AnnouncementSerializer(
            announcements,
            many=True
        )

        return Response(
            {
                "count": announcements.count(),
                "announcements": serializer.data
            }
        )


class StudentAnnouncementListView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):

        announcements = Announcement.objects.filter(
            is_published=True
        ).select_related(
            "created_by",
            "subject"
        )

        serializer = AnnouncementSerializer(
            announcements,
            many=True
        )

        return Response(
            {
                "count": announcements.count(),
                "announcements": serializer.data
            }
        )


class AnnouncementDetailView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, announcement_id):

        try:
            announcement = Announcement.objects.select_related(
                "created_by",
                "subject"
            ).get(
                id=announcement_id,
                is_published=True
            )

        except Announcement.DoesNotExist:
            return Response(
                {"error": "Announcement not found"},
                status=status.HTTP_404_NOT_FOUND
            )

        serializer = AnnouncementSerializer(announcement)

        return Response(serializer.data)