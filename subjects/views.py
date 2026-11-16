from rest_framework import generics

from .models import Subject
from .serializers import SubjectSerializer


class SubjectListCreateView(generics.ListCreateAPIView):

    serializer_class = SubjectSerializer

    def get_queryset(self):

        queryset = Subject.objects.all()

        semester = self.request.query_params.get("semester")

        if semester:
            try:
                semester = int(semester)
                queryset = queryset.filter(
                    semester=semester
                )
            except ValueError:
                queryset = Subject.objects.none()

        return queryset