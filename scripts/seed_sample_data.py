from __future__ import annotations

import os
import uuid
from dataclasses import dataclass
from datetime import date, datetime, time, timedelta, timezone
from zoneinfo import ZoneInfo

import psycopg


DB_HOST = os.getenv("KIACMS_DB_HOST", "192.168.0.10")
DB_PORT = int(os.getenv("KIACMS_DB_PORT", "5432"))
DB_NAME = os.getenv("KIACMS_DB_NAME", "kiacmsdb")
DB_USER = os.getenv("KIACMS_DB_USER", "kwung")
DB_PASSWORD = os.environ["KIACMS_DB_PASSWORD"]
SEOUL = ZoneInfo("Asia/Seoul")
NOW_UTC = datetime.now(timezone.utc)
PASSWORD_HASH = "{bcrypt}$2a$10$iD.C6Fd42uCD6un0Eb6J8O4s12hQpxR08j4sbkUMUq3.SLlLiN1my"
UUID_NAMESPACE = uuid.UUID("3d0ea59b-20ec-4f6a-9e2c-2fa7e6e6d6f9")


@dataclass(frozen=True)
class SeedUser:
    email: str
    name: str
    role_type: str
    phone_number: str
    bio: str


@dataclass(frozen=True)
class CourseSeed:
    code: str
    title: str
    description: str
    track_name: str
    start_date: date
    end_date: date
    max_capacity: int
    weekdays: tuple[int, ...]
    occurrences: int
    start_time: time
    end_time: time
    classroom: str
    instructor_emails: tuple[str, ...]


def seeded_uuid(key: str) -> uuid.UUID:
    return uuid.uuid5(UUID_NAMESPACE, key)


def connect():
    return psycopg.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
    )


def build_student_users() -> list[SeedUser]:
    names = [
        "김민준", "이서준", "박도윤", "최예준", "정하준", "강지호", "조시우", "윤현우", "장민성", "임준호",
        "한도현", "오주원", "서지민", "송예린", "권서연", "황다은", "안유진", "신가은", "전채원", "홍수민",
        "백지우", "문나연", "남소현", "고은채", "배하늘", "류지안", "노지원", "하민서", "진서영", "유다빈",
    ]
    users = []
    for index, name in enumerate(names, start=1):
        users.append(
            SeedUser(
                email=f"seed.student{index:02d}@kiacms.local",
                name=name,
                role_type="STUDENT",
                phone_number=f"010-61{index:02d}-2{index:02d}{(index + 7) % 10}{(index + 3) % 10}",
                bio=f"{name} 수강생 샘플 계정입니다. 백엔드, 웹, 보안 트랙 테스트용 데이터로 사용됩니다.",
            )
        )
    return users


def build_instructor_users() -> list[SeedUser]:
    seeds = [
        ("seed.instructor01@kiacms.local", "박현우", "010-7701-1101", "멀티모달 강의 담당 샘플 강사입니다."),
        ("seed.instructor02@kiacms.local", "이지훈", "010-7702-1102", "리눅스 과정 담당 샘플 강사입니다."),
        ("seed.instructor03@kiacms.local", "최은영", "010-7703-1103", "보안기사 과정 담당 샘플 강사입니다."),
        ("seed.instructor04@kiacms.local", "정수민", "010-7704-1104", "멀티모달 심화 회차를 보조하는 샘플 강사입니다."),
        ("seed.instructor05@kiacms.local", "한지윤", "010-7705-1105", "주말 보안 세션을 보조하는 샘플 강사입니다."),
    ]
    return [
        SeedUser(email=email, name=name, role_type="INSTRUCTOR", phone_number=phone, bio=bio)
        for email, name, phone, bio in seeds
    ]


def build_courses() -> list[CourseSeed]:
    return [
        CourseSeed(
            code="SEED-JAVA-2026",
            title="Java 수업",
            description="Spring Boot 기반 백엔드 개발을 위한 Java 핵심 문법과 API 설계 실습 수업입니다.",
            track_name="백엔드",
            start_date=date(2026, 4, 13),
            end_date=date(2026, 5, 15),
            max_capacity=40,
            weekdays=(0, 2, 4),
            occurrences=15,
            start_time=time(19, 0),
            end_time=time(22, 0),
            classroom="강의실 A",
            instructor_emails=("teacher@kiacms.local",),
        ),
        CourseSeed(
            code="SEED-MULTIMODAL-2026",
            title="멀티모달 수업",
            description="텍스트·이미지·음성을 함께 다루는 멀티모달 AI 서비스 설계와 프롬프트 실습 과정입니다.",
            track_name="AI",
            start_date=date(2026, 4, 13),
            end_date=date(2026, 5, 15),
            max_capacity=35,
            weekdays=(0, 2, 4),
            occurrences=15,
            start_time=time(19, 0),
            end_time=time(22, 0),
            classroom="강의실 B",
            instructor_emails=("seed.instructor01@kiacms.local", "seed.instructor04@kiacms.local"),
        ),
        CourseSeed(
            code="SEED-LINUX-2026",
            title="리눅스 수업",
            description="리눅스 서버 운영, 셸 자동화, 배포/운영 실습을 중심으로 진행하는 인프라 과정입니다.",
            track_name="인프라",
            start_date=date(2026, 4, 14),
            end_date=date(2026, 5, 14),
            max_capacity=30,
            weekdays=(1, 3),
            occurrences=10,
            start_time=time(19, 0),
            end_time=time(22, 0),
            classroom="강의실 C",
            instructor_emails=("seed.instructor02@kiacms.local",),
        ),
        CourseSeed(
            code="SEED-SECURITY-CERT-2026",
            title="보안기사 수업",
            description="정보보안기사 준비를 위한 네트워크/시스템/법규 이론과 기출 문제 풀이 중심 과정입니다.",
            track_name="보안",
            start_date=date(2026, 4, 18),
            end_date=date(2026, 5, 10),
            max_capacity=45,
            weekdays=(5, 6),
            occurrences=8,
            start_time=time(10, 0),
            end_time=time(13, 0),
            classroom="강의실 D",
            instructor_emails=("seed.instructor03@kiacms.local", "seed.instructor05@kiacms.local"),
        ),
    ]


def generate_occurrence_dates(start_date: date, weekdays: tuple[int, ...], occurrences: int) -> list[date]:
    collected: list[date] = []
    current = start_date
    weekday_set = set(weekdays)
    while len(collected) < occurrences:
        if current.weekday() in weekday_set:
            collected.append(current)
        current += timedelta(days=1)
    return collected


def upsert_user(cur, user_id: uuid.UUID, seed_user: SeedUser, reviewed_by_id: uuid.UUID | None):
    cur.execute(
        """
        insert into users (
            id, created_at, updated_at, email, password_hash, name, phone_number, profile_image_url, bio,
            role_type, status, reviewed_by_id, reviewed_at, account_status_reason, deleted_at, last_login_at
        ) values (
            %(id)s, %(now)s, %(now)s, %(email)s, %(password_hash)s, %(name)s, %(phone_number)s, null, %(bio)s,
            %(role_type)s, 'APPROVED', %(reviewed_by_id)s, %(now)s, null, null, null
        )
        on conflict (email) do update set
            updated_at = excluded.updated_at,
            password_hash = excluded.password_hash,
            name = excluded.name,
            phone_number = excluded.phone_number,
            profile_image_url = excluded.profile_image_url,
            bio = excluded.bio,
            role_type = excluded.role_type,
            status = excluded.status,
            reviewed_by_id = excluded.reviewed_by_id,
            reviewed_at = excluded.reviewed_at,
            account_status_reason = excluded.account_status_reason,
            deleted_at = excluded.deleted_at
        returning id
        """,
        {
            "id": user_id,
            "now": NOW_UTC,
            "email": seed_user.email,
            "password_hash": PASSWORD_HASH,
            "name": seed_user.name,
            "phone_number": seed_user.phone_number,
            "bio": seed_user.bio,
            "role_type": seed_user.role_type,
            "reviewed_by_id": reviewed_by_id,
        },
    )
    return cur.fetchone()[0]


def upsert_user_settings(cur, user_id: uuid.UUID, theme_mode: str = "LIGHT"):
    setting_id = seeded_uuid(f"user-settings:{user_id}")
    cur.execute(
        """
        insert into user_settings (
            id, created_at, updated_at, locale, notifications_enabled, theme_mode, timezone, user_id
        ) values (
            %(id)s, %(now)s, %(now)s, 'ko-KR', true, %(theme_mode)s, 'Asia/Seoul', %(user_id)s
        )
        on conflict (user_id) do update set
            updated_at = excluded.updated_at,
            locale = excluded.locale,
            notifications_enabled = excluded.notifications_enabled,
            theme_mode = excluded.theme_mode,
            timezone = excluded.timezone
        """,
        {"id": setting_id, "now": NOW_UTC, "theme_mode": theme_mode, "user_id": user_id},
    )


def upsert_course(cur, course: CourseSeed, created_by_id: uuid.UUID):
    cur.execute(
        """
        insert into courses (
            id, created_at, updated_at, course_code, title, description, track_name,
            status, start_date, end_date, max_capacity, created_by_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(course_code)s, %(title)s, %(description)s, %(track_name)s,
            'IN_PROGRESS', %(start_date)s, %(end_date)s, %(max_capacity)s, %(created_by_id)s
        )
        on conflict (course_code) do update set
            updated_at = excluded.updated_at,
            title = excluded.title,
            description = excluded.description,
            track_name = excluded.track_name,
            status = excluded.status,
            start_date = excluded.start_date,
            end_date = excluded.end_date,
            max_capacity = excluded.max_capacity,
            created_by_id = excluded.created_by_id
        returning id
        """,
        {
            "id": seeded_uuid(f"course:{course.code}"),
            "now": NOW_UTC,
            "course_code": course.code,
            "title": course.title,
            "description": course.description,
            "track_name": course.track_name,
            "start_date": course.start_date,
            "end_date": course.end_date,
            "max_capacity": course.max_capacity,
            "created_by_id": created_by_id,
        },
    )
    return cur.fetchone()[0]


def upsert_course_session(cur, course_id: uuid.UUID, session_order: int, title: str, description: str, classroom: str,
                          session_date: date, start_time: time, end_time: time, instructor_id: uuid.UUID):
    cur.execute(
        """
        insert into course_sessions (
            id, created_at, updated_at, course_id, session_order, title, description,
            classroom, session_date, start_time, end_time, status, instructor_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(course_id)s, %(session_order)s, %(title)s, %(description)s,
            %(classroom)s, %(session_date)s, %(start_time)s, %(end_time)s, 'SCHEDULED', %(instructor_id)s
        )
        on conflict on constraint uk_course_session_order do update set
            updated_at = excluded.updated_at,
            title = excluded.title,
            description = excluded.description,
            classroom = excluded.classroom,
            session_date = excluded.session_date,
            start_time = excluded.start_time,
            end_time = excluded.end_time,
            status = excluded.status,
            instructor_id = excluded.instructor_id
        returning id
        """,
        {
            "id": seeded_uuid(f"course-session:{course_id}:{session_order}"),
            "now": NOW_UTC,
            "course_id": course_id,
            "session_order": session_order,
            "title": title,
            "description": description,
            "classroom": classroom,
            "session_date": session_date,
            "start_time": start_time,
            "end_time": end_time,
            "instructor_id": instructor_id,
        },
    )
    return cur.fetchone()[0]


def upsert_session_resource(cur, course_session_id: uuid.UUID, last_updated_by_id: uuid.UUID, suffix: str):
    cur.execute(
        """
        insert into session_resources (
            id, created_at, updated_at, additional_notice, recording_link, recording_link_updated_at,
            summary_link, summary_link_updated_at, zoom_link, zoom_link_updated_at, course_session_id, last_updated_by_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(notice)s, %(recording)s, %(now)s,
            %(summary)s, %(now)s, %(zoom)s, %(now)s, %(course_session_id)s, %(last_updated_by_id)s
        )
        on conflict (course_session_id) do update set
            updated_at = excluded.updated_at,
            additional_notice = excluded.additional_notice,
            recording_link = excluded.recording_link,
            recording_link_updated_at = excluded.recording_link_updated_at,
            summary_link = excluded.summary_link,
            summary_link_updated_at = excluded.summary_link_updated_at,
            zoom_link = excluded.zoom_link,
            zoom_link_updated_at = excluded.zoom_link_updated_at,
            last_updated_by_id = excluded.last_updated_by_id
        """,
        {
            "id": seeded_uuid(f"session-resource:{course_session_id}"),
            "now": NOW_UTC,
            "notice": "샘플 데이터용 회차 리소스입니다. 실제 수업 전에 링크 동작을 점검해주세요.",
            "recording": f"https://videos.kiacms.local/recordings/{suffix}",
            "summary": f"https://docs.kiacms.local/summaries/{suffix}",
            "zoom": f"https://zoom.us/j/{suffix}",
            "course_session_id": course_session_id,
            "last_updated_by_id": last_updated_by_id,
        },
    )


def upsert_enrollment(cur, student_id: uuid.UUID, course_id: uuid.UUID, enrolled_by_id: uuid.UUID):
    cur.execute(
        """
        insert into enrollments (
            id, created_at, updated_at, completed_at, status, course_id, enrolled_by_id, student_id
        ) values (
            %(id)s, %(now)s, %(now)s, null, 'ENROLLED', %(course_id)s, %(enrolled_by_id)s, %(student_id)s
        )
        on conflict on constraint uk_enrollment_student_course do update set
            updated_at = excluded.updated_at,
            completed_at = excluded.completed_at,
            status = excluded.status,
            enrolled_by_id = excluded.enrolled_by_id
        """,
        {
            "id": seeded_uuid(f"enrollment:{student_id}:{course_id}"),
            "now": NOW_UTC,
            "course_id": course_id,
            "enrolled_by_id": enrolled_by_id,
            "student_id": student_id,
        },
    )


def upsert_mapping(cur, mapping_id: uuid.UUID, mentor_id: uuid.UUID, student_id: uuid.UUID, assigned_by_id: uuid.UUID, memo: str):
    cur.execute(
        """
        insert into mentor_student_mappings (
            id, created_at, updated_at, end_date, memo, start_date, status, assigned_by_id, mentor_id, student_id
        ) values (
            %(id)s, %(now)s, %(now)s, null, %(memo)s, %(start_date)s, 'ACTIVE', %(assigned_by_id)s, %(mentor_id)s, %(student_id)s
        )
        on conflict (id) do update set
            updated_at = excluded.updated_at,
            end_date = excluded.end_date,
            memo = excluded.memo,
            start_date = excluded.start_date,
            status = excluded.status,
            assigned_by_id = excluded.assigned_by_id,
            mentor_id = excluded.mentor_id,
            student_id = excluded.student_id
        """,
        {
            "id": mapping_id,
            "now": NOW_UTC,
            "memo": memo,
            "start_date": date(2026, 4, 13),
            "assigned_by_id": assigned_by_id,
            "mentor_id": mentor_id,
            "student_id": student_id,
        },
    )


def upsert_project_post(cur, project_id: uuid.UUID, owner_id: uuid.UUID, title: str, description: str, goal: str,
                        tech_stack: str, duration_text: str, contact_method: str, contact_value: str,
                        pm_introduction: str, pm_background: str, recruit_until: date):
    cur.execute(
        """
        insert into project_posts (
            id, created_at, updated_at, closed_at, contact_method, contact_value, description,
            duration_text, goal, pm_background, pm_introduction, recruit_until, status, tech_stack,
            title, owner_id, deleted_at, deleted_by_id, deletion_reason
        ) values (
            %(id)s, %(now)s, %(now)s, null, %(contact_method)s, %(contact_value)s, %(description)s,
            %(duration_text)s, %(goal)s, %(pm_background)s, %(pm_introduction)s, %(recruit_until)s, 'OPEN', %(tech_stack)s,
            %(title)s, %(owner_id)s, null, null, null
        )
        on conflict (id) do update set
            updated_at = excluded.updated_at,
            closed_at = excluded.closed_at,
            contact_method = excluded.contact_method,
            contact_value = excluded.contact_value,
            description = excluded.description,
            duration_text = excluded.duration_text,
            goal = excluded.goal,
            pm_background = excluded.pm_background,
            pm_introduction = excluded.pm_introduction,
            recruit_until = excluded.recruit_until,
            status = excluded.status,
            tech_stack = excluded.tech_stack,
            title = excluded.title,
            owner_id = excluded.owner_id,
            deleted_at = excluded.deleted_at,
            deleted_by_id = excluded.deleted_by_id,
            deletion_reason = excluded.deletion_reason
        """,
        {
            "id": project_id,
            "now": NOW_UTC,
            "contact_method": contact_method,
            "contact_value": contact_value,
            "description": description,
            "duration_text": duration_text,
            "goal": goal,
            "pm_background": pm_background,
            "pm_introduction": pm_introduction,
            "recruit_until": recruit_until,
            "tech_stack": tech_stack,
            "title": title,
            "owner_id": owner_id,
        },
    )


def upsert_project_position(cur, position_id: uuid.UUID, project_post_id: uuid.UUID, name: str, description: str,
                            required_skills: str, capacity: int):
    cur.execute(
        """
        insert into project_positions (
            id, created_at, updated_at, capacity, description, name, required_skills, project_post_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(capacity)s, %(description)s, %(name)s, %(required_skills)s, %(project_post_id)s
        )
        on conflict (id) do update set
            updated_at = excluded.updated_at,
            capacity = excluded.capacity,
            description = excluded.description,
            name = excluded.name,
            required_skills = excluded.required_skills,
            project_post_id = excluded.project_post_id
        """,
        {
            "id": position_id,
            "now": NOW_UTC,
            "capacity": capacity,
            "description": description,
            "name": name,
            "required_skills": required_skills,
            "project_post_id": project_post_id,
        },
    )


def upsert_project_application(cur, position_id: uuid.UUID, applicant_id: uuid.UUID, motivation: str, course_history: str,
                               certifications: str, tech_stack: str, portfolio_url: str | None,
                               self_introduction: str, status: str, reviewed_by_id: uuid.UUID | None = None,
                               decision_reason: str | None = None):
    cur.execute(
        """
        insert into project_applications (
            id, created_at, updated_at, certifications, course_history, decision_reason, motivation,
            portfolio_url, reviewed_at, self_introduction, status, tech_stack, withdrawn_at,
            applicant_id, project_position_id, reviewed_by_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(certifications)s, %(course_history)s, %(decision_reason)s, %(motivation)s,
            %(portfolio_url)s, %(reviewed_at)s, %(self_introduction)s, %(status)s, %(tech_stack)s, null,
            %(applicant_id)s, %(position_id)s, %(reviewed_by_id)s
        )
        on conflict on constraint uk_project_application do update set
            updated_at = excluded.updated_at,
            certifications = excluded.certifications,
            course_history = excluded.course_history,
            decision_reason = excluded.decision_reason,
            motivation = excluded.motivation,
            portfolio_url = excluded.portfolio_url,
            reviewed_at = excluded.reviewed_at,
            self_introduction = excluded.self_introduction,
            status = excluded.status,
            tech_stack = excluded.tech_stack,
            withdrawn_at = excluded.withdrawn_at,
            reviewed_by_id = excluded.reviewed_by_id
        """,
        {
            "id": seeded_uuid(f"project-application:{position_id}:{applicant_id}"),
            "now": NOW_UTC,
            "certifications": certifications,
            "course_history": course_history,
            "decision_reason": decision_reason,
            "motivation": motivation,
            "portfolio_url": portfolio_url,
            "reviewed_at": NOW_UTC if reviewed_by_id else None,
            "self_introduction": self_introduction,
            "status": status,
            "tech_stack": tech_stack,
            "applicant_id": applicant_id,
            "position_id": position_id,
            "reviewed_by_id": reviewed_by_id,
        },
    )


def upsert_mentor_application(cur, project_post_id: uuid.UUID, applicant_id: uuid.UUID, expertise_summary: str,
                              mentoring_experience: str, portfolio_url: str | None, support_plan: str,
                              status: str, reviewed_by_id: uuid.UUID | None = None, decision_reason: str | None = None):
    cur.execute(
        """
        insert into mentor_applications (
            id, created_at, updated_at, decision_reason, expertise_summary, mentoring_experience,
            portfolio_url, reviewed_at, status, support_plan, withdrawn_at,
            applicant_id, project_post_id, reviewed_by_id
        ) values (
            %(id)s, %(now)s, %(now)s, %(decision_reason)s, %(expertise_summary)s, %(mentoring_experience)s,
            %(portfolio_url)s, %(reviewed_at)s, %(status)s, %(support_plan)s, null,
            %(applicant_id)s, %(project_post_id)s, %(reviewed_by_id)s
        )
        on conflict on constraint uk_mentor_application do update set
            updated_at = excluded.updated_at,
            decision_reason = excluded.decision_reason,
            expertise_summary = excluded.expertise_summary,
            mentoring_experience = excluded.mentoring_experience,
            portfolio_url = excluded.portfolio_url,
            reviewed_at = excluded.reviewed_at,
            status = excluded.status,
            support_plan = excluded.support_plan,
            withdrawn_at = excluded.withdrawn_at,
            reviewed_by_id = excluded.reviewed_by_id
        """,
        {
            "id": seeded_uuid(f"mentor-application:{project_post_id}:{applicant_id}"),
            "now": NOW_UTC,
            "decision_reason": decision_reason,
            "expertise_summary": expertise_summary,
            "mentoring_experience": mentoring_experience,
            "portfolio_url": portfolio_url,
            "reviewed_at": NOW_UTC if reviewed_by_id else None,
            "status": status,
            "support_plan": support_plan,
            "applicant_id": applicant_id,
            "project_post_id": project_post_id,
            "reviewed_by_id": reviewed_by_id,
        },
    )


def upsert_notification(cur, notification_id: uuid.UUID, recipient_id: uuid.UUID, type_: str, title: str, message: str,
                        target_type: str, target_id: uuid.UUID | None, target_url: str | None):
    cur.execute(
        """
        insert into notifications (
            id, created_at, updated_at, is_read, message, read_at, target_id, target_type, target_url,
            title, type, recipient_id
        ) values (
            %(id)s, %(now)s, %(now)s, false, %(message)s, null, %(target_id)s, %(target_type)s, %(target_url)s,
            %(title)s, %(type)s, %(recipient_id)s
        )
        on conflict (id) do update set
            updated_at = excluded.updated_at,
            is_read = excluded.is_read,
            message = excluded.message,
            read_at = excluded.read_at,
            target_id = excluded.target_id,
            target_type = excluded.target_type,
            target_url = excluded.target_url,
            title = excluded.title,
            type = excluded.type,
            recipient_id = excluded.recipient_id
        """,
        {
            "id": notification_id,
            "now": NOW_UTC,
            "message": message,
            "target_id": target_id,
            "target_type": target_type,
            "target_url": target_url,
            "title": title,
            "type": type_,
            "recipient_id": recipient_id,
        },
    )


def seed():
    student_users = build_student_users()
    instructor_users = build_instructor_users()
    courses = build_courses()

    with connect() as conn:
        with conn.cursor() as cur:
            core_accounts = [
                SeedUser("root@kiacms.local", "Root", "ROOT", "010-9000-0001", "시스템 최고 관리자 테스트 계정입니다."),
                SeedUser("student@kiacms.local", "학생", "STUDENT", "010-9000-0002", "학생 메인 테스트 계정입니다."),
                SeedUser("mentor@kiacms.local", "멘토", "MENTOR", "010-9000-0003", "멘토 메인 테스트 계정입니다."),
                SeedUser("teacher@kiacms.local", "강사", "INSTRUCTOR", "010-9000-0004", "강사 메인 테스트 계정입니다."),
            ]

            user_ids: dict[str, uuid.UUID] = {}
            root_id = upsert_user(cur, seeded_uuid("user:root@kiacms.local"), core_accounts[0], None)
            user_ids["root@kiacms.local"] = root_id
            cur.execute(
                """
                update users
                set reviewed_by_id = %(root_id)s, reviewed_at = %(now)s, status = 'APPROVED'
                where id = %(root_id)s
                """,
                {"root_id": root_id, "now": NOW_UTC},
            )

            for account in core_accounts[1:]:
                user_ids[account.email] = upsert_user(cur, seeded_uuid(f"user:{account.email}"), account, root_id)

            for seed_user in student_users + instructor_users:
                user_ids[seed_user.email] = upsert_user(cur, seeded_uuid(f"user:{seed_user.email}"), seed_user, root_id)

            for email, user_id in user_ids.items():
                theme = "DARK" if email == "mentor@kiacms.local" else "LIGHT"
                upsert_user_settings(cur, user_id, theme)

            course_ids: dict[str, uuid.UUID] = {}
            session_ids: dict[str, list[uuid.UUID]] = {}

            for course in courses:
                course_id = upsert_course(cur, course, root_id)
                course_ids[course.code] = course_id
                occurrence_dates = generate_occurrence_dates(course.start_date, course.weekdays, course.occurrences)
                session_ids[course.code] = []
                for index, session_date in enumerate(occurrence_dates, start=1):
                    instructor_email = course.instructor_emails[(index - 1) % len(course.instructor_emails)]
                    instructor_id = user_ids[instructor_email]
                    session_id = upsert_course_session(
                        cur,
                        course_id=course_id,
                        session_order=index,
                        title=f"{course.title} {index}회차",
                        description=f"{course.title} {index}회차 샘플 일정입니다.",
                        classroom=course.classroom,
                        session_date=session_date,
                        start_time=course.start_time,
                        end_time=course.end_time,
                        instructor_id=instructor_id,
                    )
                    session_ids[course.code].append(session_id)
                    if index <= 3:
                        upsert_session_resource(
                            cur,
                            course_session_id=session_id,
                            last_updated_by_id=instructor_id,
                            suffix=f"{course.code.lower()}-{index:02d}",
                        )

            student_emails = ["student@kiacms.local"] + [user.email for user in student_users]
            for offset, email in enumerate(student_emails):
                student_id = user_ids[email]
                upsert_enrollment(cur, student_id, course_ids["SEED-JAVA-2026"], root_id)
                if offset % 2 == 0:
                    upsert_enrollment(cur, student_id, course_ids["SEED-MULTIMODAL-2026"], root_id)
                if offset % 3 == 0:
                    upsert_enrollment(cur, student_id, course_ids["SEED-LINUX-2026"], root_id)
                if offset % 4 == 0:
                    upsert_enrollment(cur, student_id, course_ids["SEED-SECURITY-CERT-2026"], root_id)

            mentor_managed_emails = ["student@kiacms.local"] + [user.email for user in student_users[:12]]
            for email in mentor_managed_emails:
                upsert_mapping(
                    cur,
                    mapping_id=seeded_uuid(f"mentor-mapping:mentor@kiacms.local:{email}"),
                    mentor_id=user_ids["mentor@kiacms.local"],
                    student_id=user_ids[email],
                    assigned_by_id=root_id,
                    memo="멘토 데모 화면 확인용 매핑입니다.",
                )

            project_specs = [
                {
                    "id": seeded_uuid("project:study-matching"),
                    "owner_email": "student@kiacms.local",
                    "title": "캠퍼스 스터디 매칭 웹앱",
                    "description": "수강생들이 과정별 스터디를 만들고 신청할 수 있는 웹 서비스를 개발합니다.",
                    "goal": "과정/관심사 기반 매칭, 일정 조율, 참가 신청까지 한 번에 처리하는 운영형 서비스를 완성합니다.",
                    "tech_stack": "React, Spring Boot, PostgreSQL, Redis",
                    "duration_text": "6주 MVP + 2주 고도화",
                    "contact_method": "DISCORD",
                    "contact_value": "kiacms-study#2026",
                    "pm_introduction": "백엔드 트랙을 수강 중이며 운영형 서비스를 끝까지 완성해 본 경험을 만들고 싶은 PM입니다.",
                    "pm_background": "Java, Spring Boot, ERD 설계 경험이 있고 사용자 흐름을 직접 설계해 본 경험이 있습니다.",
                    "recruit_until": date(2026, 5, 8),
                    "positions": [
                        ("프론트엔드", "대시보드와 스터디 게시판 UI 구현", "React, CSS, API 연동", 2),
                        ("백엔드", "스터디 생성/신청/알림 API 구현", "Spring Boot, JPA, PostgreSQL", 2),
                    ],
                },
                {
                    "id": seeded_uuid("project:multimodal-note-helper"),
                    "owner_email": "seed.student01@kiacms.local",
                    "title": "멀티모달 강의 요약 도우미",
                    "description": "강의 녹화본, 슬라이드, 텍스트 노트를 함께 분석해 학습 요약을 제공하는 프로젝트입니다.",
                    "goal": "멀티모달 입력 기반 요약 및 복습 포인트 추천 기능을 구현해 교육 서비스를 실증합니다.",
                    "tech_stack": "Python, FastAPI, React, OpenAI API",
                    "duration_text": "4주 프로토타입 + 4주 실험",
                    "contact_method": "EMAIL",
                    "contact_value": "seed.student01@kiacms.local",
                    "pm_introduction": "AI 트랙을 공부하면서 교육 현장에 바로 적용 가능한 서비스를 만들고 싶습니다.",
                    "pm_background": "Python 기반 데이터 처리와 프론트 협업 경험이 있으며 프롬프트 실험을 해왔습니다.",
                    "recruit_until": date(2026, 5, 15),
                    "positions": [
                        ("AI 엔지니어", "요약 파이프라인과 프롬프트 실험", "Python, LLM API, 데이터 전처리", 2),
                        ("프론트엔드", "대화형 요약 결과 UI 구현", "React, Chart, API 연동", 1),
                    ],
                },
                {
                    "id": seeded_uuid("project:security-log-dashboard"),
                    "owner_email": "seed.student02@kiacms.local",
                    "title": "보안 로그 이상 탐지 대시보드",
                    "description": "리눅스와 웹 서버 로그를 수집해 이상 징후를 시각화하고 대응 플로우를 제공하는 프로젝트입니다.",
                    "goal": "보안기사/리눅스 수업에서 다룬 내용을 실제 대시보드 서비스로 연결합니다.",
                    "tech_stack": "Spring Boot, Linux, PostgreSQL, Docker",
                    "duration_text": "8주",
                    "contact_method": "KAKAO_TALK",
                    "contact_value": "https://open.kakao.com/o/sampleSecurity2026",
                    "pm_introduction": "보안과 운영을 함께 다루는 서비스를 만들고 싶어 이번 프로젝트를 기획했습니다.",
                    "pm_background": "리눅스 서버 실습과 로그 분석 경험이 있으며 보안기사 필기 준비 중입니다.",
                    "recruit_until": date(2026, 5, 22),
                    "positions": [
                        ("백엔드", "로그 적재 및 이상 탐지 API 구현", "Spring Boot, PostgreSQL", 1),
                        ("보안", "탐지 규칙 설계와 시나리오 정리", "로그 분석, 네트워크, 보안 기초", 2),
                        ("인프라", "수집기/배포 환경 구성", "Linux, Docker, Shell", 1),
                    ],
                },
            ]

            position_ids: dict[str, uuid.UUID] = {}
            for project in project_specs:
                owner_id = user_ids[project["owner_email"]]
                upsert_project_post(
                    cur,
                    project_id=project["id"],
                    owner_id=owner_id,
                    title=project["title"],
                    description=project["description"],
                    goal=project["goal"],
                    tech_stack=project["tech_stack"],
                    duration_text=project["duration_text"],
                    contact_method=project["contact_method"],
                    contact_value=project["contact_value"],
                    pm_introduction=project["pm_introduction"],
                    pm_background=project["pm_background"],
                    recruit_until=project["recruit_until"],
                )
                for name, description, required_skills, capacity in project["positions"]:
                    position_id = seeded_uuid(f"project-position:{project['id']}:{name}")
                    position_ids[f"{project['title']}::{name}"] = position_id
                    upsert_project_position(
                        cur,
                        position_id=position_id,
                        project_post_id=project["id"],
                        name=name,
                        description=description,
                        required_skills=required_skills,
                        capacity=capacity,
                    )

            upsert_project_application(
                cur,
                position_id=position_ids["멀티모달 강의 요약 도우미::AI 엔지니어"],
                applicant_id=user_ids["student@kiacms.local"],
                motivation="강의 요약 기능을 직접 만들어 보고 싶고 AI 트랙 수업 내용과 연결되는 경험을 쌓고 싶습니다.",
                course_history="Java 수업, 멀티모달 수업 수강 중",
                certifications="ADsP 준비 중",
                tech_stack="Python, React, Spring Boot",
                portfolio_url="https://portfolio.kiacms.local/student",
                self_introduction="백엔드와 AI를 함께 배우고 있는 수강생으로 실제 서비스형 프로젝트를 경험하고 싶습니다.",
                status="SUBMITTED",
            )
            upsert_project_application(
                cur,
                position_id=position_ids["캠퍼스 스터디 매칭 웹앱::백엔드"],
                applicant_id=user_ids["seed.student03@kiacms.local"],
                motivation="운영형 서비스 API를 설계하는 경험을 쌓고 싶습니다.",
                course_history="Java 수업, 리눅스 수업 수강 중",
                certifications="정보처리기사 필기 합격",
                tech_stack="Java, Spring Boot, PostgreSQL",
                portfolio_url="https://github.com/kiacms-seed/student03",
                self_introduction="DB와 API 설계에 강점이 있고 팀 문서화에도 적극적으로 참여합니다.",
                status="ACCEPTED",
                reviewed_by_id=user_ids["student@kiacms.local"],
                decision_reason="운영형 서비스 경험과 현재 수강 과정이 잘 맞아 합류를 승인했습니다.",
            )
            upsert_project_application(
                cur,
                position_id=position_ids["보안 로그 이상 탐지 대시보드::보안"],
                applicant_id=user_ids["seed.student04@kiacms.local"],
                motivation="보안기사 학습 내용을 프로젝트로 연결하고 싶습니다.",
                course_history="보안기사 수업 수강 중",
                certifications="네트워크관리사 2급",
                tech_stack="Linux, Wireshark, Python",
                portfolio_url=None,
                self_introduction="로그 분석과 문서 정리에 강점이 있고 보안 시나리오 설계에 관심이 많습니다.",
                status="SUBMITTED",
            )

            upsert_mentor_application(
                cur,
                project_post_id=seeded_uuid("project:study-matching"),
                applicant_id=user_ids["teacher@kiacms.local"],
                expertise_summary="백엔드 구조 설계와 Spring Boot 실무 피드백",
                mentoring_experience="교육 현장에서 팀 프로젝트 코드 리뷰를 진행해 왔습니다.",
                portfolio_url="https://github.com/kiacms-teacher",
                support_plan="주 1회 구조 리뷰와 API 설계 피드백, 배포 전 체크리스트 제공",
                status="ACCEPTED",
                reviewed_by_id=user_ids["student@kiacms.local"],
                decision_reason="백엔드 구조 설계 멘토링이 프로젝트 방향과 잘 맞아 승인했습니다.",
            )
            upsert_mentor_application(
                cur,
                project_post_id=seeded_uuid("project:multimodal-note-helper"),
                applicant_id=user_ids["mentor@kiacms.local"],
                expertise_summary="학습 관리 관점에서 교육형 서비스 운영 자문",
                mentoring_experience="수강생 관리와 프로젝트 방향 정리를 담당한 경험이 있습니다.",
                portfolio_url=None,
                support_plan="학습 목표 정리, 일정 점검, 발표 자료 리뷰",
                status="SUBMITTED",
            )

            first_java_session = session_ids["SEED-JAVA-2026"][0]
            second_multimodal_session = session_ids["SEED-MULTIMODAL-2026"][1]
            study_matching_project = seeded_uuid("project:study-matching")
            multimodal_project = seeded_uuid("project:multimodal-note-helper")

            notifications = [
                (
                    seeded_uuid("notification:student:session-zoom"),
                    user_ids["student@kiacms.local"],
                    "SESSION_ZOOM_UPDATED",
                    "Java 수업 Zoom 링크 등록",
                    "Java 수업 1회차 Zoom 링크가 등록되었습니다. 오늘 수업 전에 접속 정보를 확인하세요.",
                    "COURSE_SESSION",
                    first_java_session,
                    f"/student/sessions/{first_java_session}",
                ),
                (
                    seeded_uuid("notification:student:project-result"),
                    user_ids["student@kiacms.local"],
                    "PROJECT_APPLICATION_RESULT",
                    "프로젝트 지원 현황 안내",
                    "멀티모달 강의 요약 도우미 지원서가 정상 접수되었습니다. PM 검토를 기다려주세요.",
                    "PROJECT_POST",
                    multimodal_project,
                    f"/projects/{multimodal_project}",
                ),
                (
                    seeded_uuid("notification:teacher:mentor"),
                    user_ids["teacher@kiacms.local"],
                    "MENTOR_APPLICATION_RESULT",
                    "멘토 지원 승인",
                    "캠퍼스 스터디 매칭 웹앱 멘토 지원이 승인되었습니다.",
                    "PROJECT_POST",
                    study_matching_project,
                    f"/projects/{study_matching_project}",
                ),
                (
                    seeded_uuid("notification:mentor:system"),
                    user_ids["mentor@kiacms.local"],
                    "SYSTEM_ANNOUNCEMENT",
                    "멘토 관리 샘플 데이터 준비 완료",
                    "담당 학생과 수업 매핑 샘플 데이터가 생성되었습니다. 멘토 화면에서 바로 확인할 수 있습니다.",
                    "DASHBOARD",
                    None,
                    "/mentor/students",
                ),
                (
                    seeded_uuid("notification:student02:project"),
                    user_ids["seed.student02@kiacms.local"],
                    "PROJECT_APPLICATION_RECEIVED",
                    "새 프로젝트 지원 도착",
                    "보안 로그 이상 탐지 대시보드 프로젝트에 새로운 지원서가 도착했습니다.",
                    "PROJECT_POST",
                    seeded_uuid("project:security-log-dashboard"),
                    f"/student/projects/{seeded_uuid('project:security-log-dashboard')}/manage",
                ),
                (
                    seeded_uuid("notification:student01:session-summary"),
                    user_ids["seed.student01@kiacms.local"],
                    "SESSION_SUMMARY_UPDATED",
                    "멀티모달 수업 정리 링크 등록",
                    "멀티모달 수업 2회차 정리 링크가 등록되었습니다. 복습 전에 요약 문서를 확인해보세요.",
                    "COURSE_SESSION",
                    second_multimodal_session,
                    f"/student/sessions/{second_multimodal_session}",
                ),
            ]

            for args in notifications:
                upsert_notification(cur, *args)

        conn.commit()

    with connect() as conn:
        with conn.cursor() as cur:
            print("=== Seed Summary ===")
            cur.execute(
                """
                select role_type, count(*)
                from users
                where email like 'seed.student%@kiacms.local'
                   or email like 'seed.instructor%@kiacms.local'
                group by role_type
                order by role_type
                """
            )
            for role_type, count in cur.fetchall():
                print(f"{role_type}: {count}")

            cur.execute(
                """
                select c.course_code, c.title, count(cs.id)
                from courses c
                left join course_sessions cs on cs.course_id = c.id
                where c.course_code like 'SEED-%'
                group by c.course_code, c.title
                order by c.course_code
                """
            )
            for course_code, title, count in cur.fetchall():
                print(f"{course_code} | {title} | sessions={count}")

            cur.execute(
                """
                select p.title, count(distinct pp.id) as positions, count(distinct pa.id) as applications, count(distinct ma.id) as mentor_applications
                from project_posts p
                left join project_positions pp on pp.project_post_id = p.id
                left join project_applications pa on pa.project_position_id = pp.id
                left join mentor_applications ma on ma.project_post_id = p.id
                where p.id in (
                    %(p1)s, %(p2)s, %(p3)s
                )
                group by p.title
                order by p.title
                """,
                {
                    "p1": seeded_uuid("project:study-matching"),
                    "p2": seeded_uuid("project:multimodal-note-helper"),
                    "p3": seeded_uuid("project:security-log-dashboard"),
                },
            )
            for title, positions, applications, mentor_applications in cur.fetchall():
                print(
                    f"{title} | positions={positions} | applications={applications} | mentor_applications={mentor_applications}"
                )


if __name__ == "__main__":
    seed()
