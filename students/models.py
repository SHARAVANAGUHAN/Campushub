from django.db import models
from datetime import date


class Student(models.Model):

    student_id = models.CharField(
        max_length=20,
        unique=True
    )

    full_name = models.CharField(
        max_length=100
    )

    email = models.EmailField(
        unique=True
    )

    dob = models.DateField()

    is_registered = models.BooleanField(
        default=False
    )

    # =====================================================
    # SECTION
    # =====================================================

    section = models.CharField(
        max_length=10,
        default="A"
    )

    def __str__(self):
        return self.full_name

    # =====================================================
    # ADMISSION YEAR
    # =====================================================

    @property
    def admission_year(self):

        student_id = self.student_id.strip().lower()

        if len(student_id) < 2:
            return None

        year_code = student_id[:2]

        if not year_code.isdigit():
            return None

        return 2000 + int(year_code)

    # =====================================================
    # COURSE CODE
    # =====================================================

    @property
    def course_code(self):

        student_id = self.student_id.strip().lower()

        if len(student_id) < 6:
            return None

        course_codes = [
            "bcom",
            "bca",
            "bba",
            "bsc",
            "ba",
        ]

        for code in course_codes:

            if student_id.startswith(
                student_id[:2] + code
            ):
                return code.upper()

        return None

    # =====================================================
    # COURSE NAME
    # =====================================================

    @property
    def course_name(self):

        course = self.course_code

        course_names = {

            "BCA":
                "Bachelor of Computer Applications",

            "BCOM":
                "Bachelor of Commerce",

            "BBA":
                "Bachelor of Business Administration",

            "BSC":
                "Bachelor of Science",

            "BA":
                "Bachelor of Arts",
        }

        return course_names.get(
            course,
            course
        )

    # =====================================================
    # CURRENT YEAR
    # =====================================================

    @property
    def current_year(self):

        admission_year = self.admission_year

        if admission_year is None:
            return None

        current_year = date.today().year

        year = (
            current_year
            - admission_year
            + 1
        )

        if year < 1:
            return None

        return year

    # =====================================================
    # CURRENT YEAR TEXT
    # =====================================================

    @property
    def current_year_text(self):

        year = self.current_year

        year_names = {

            1: "1st Year",
            2: "2nd Year",
            3: "3rd Year",
            4: "4th Year",
        }

        return year_names.get(
            year,
            f"{year}th Year"
            if year is not None
            else "Unknown"
        )

    # =====================================================
    # CURRENT SEMESTERS
    # =====================================================

    @property
    def current_semesters(self):

        year = self.current_year

        semester_mapping = {

            1: [1, 2],
            2: [3, 4],
            3: [5, 6],
        }

        return semester_mapping.get(
            year,
            []
        )