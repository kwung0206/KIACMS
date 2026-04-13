from __future__ import annotations

import os

import psycopg


DB_HOST = os.getenv("KIACMS_DB_HOST", "192.168.0.10")
DB_PORT = int(os.getenv("KIACMS_DB_PORT", "5432"))
DB_NAME = os.getenv("KIACMS_DB_NAME", "kiacmsdb")
DB_USER = os.getenv("KIACMS_DB_USER", "kwung")
DB_PASSWORD = os.environ["KIACMS_DB_PASSWORD"]


def connect():
    return psycopg.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
    )


def cleanup():
    with connect() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                select id, title
                from project_posts
                where title like '%?%'
                   or coalesce(description, '') like '%?%'
                   or coalesce(goal, '') like '%?%'
                   or coalesce(tech_stack, '') like '%?%'
                """
            )
            bad_projects = cur.fetchall()

            for project_id, title in bad_projects:
                cur.execute("delete from notifications where target_type = 'PROJECT_POST' and target_id = %s", (project_id,))
                cur.execute("delete from mentor_applications where project_post_id = %s", (project_id,))
                cur.execute(
                    """
                    delete from project_applications
                    where project_position_id in (
                        select id from project_positions where project_post_id = %s
                    )
                    """,
                    (project_id,),
                )
                cur.execute("delete from project_positions where project_post_id = %s", (project_id,))
                cur.execute("delete from project_posts where id = %s", (project_id,))
                print(f"deleted malformed project: {title}")

            cur.execute(
                """
                select id, title, course_code
                from courses
                where title like '%?%'
                   or coalesce(description, '') like '%?%'
                   or coalesce(track_name, '') like '%?%'
                """
            )
            bad_courses = cur.fetchall()

            for course_id, title, course_code in bad_courses:
                cur.execute(
                    """
                    delete from notifications
                    where (target_type = 'COURSE' and target_id = %s)
                       or (target_type = 'COURSE_SESSION' and target_id in (
                           select id from course_sessions where course_id = %s
                       ))
                    """,
                    (course_id, course_id),
                )
                cur.execute(
                    "delete from session_resources where course_session_id in (select id from course_sessions where course_id = %s)",
                    (course_id,),
                )
                cur.execute(
                    """
                    delete from session_watch_statuses
                    where course_session_id in (
                        select id from course_sessions where course_id = %s
                    )
                    """,
                    (course_id,),
                )
                cur.execute("delete from course_sessions where course_id = %s", (course_id,))
                cur.execute("delete from enrollments where course_id = %s", (course_id,))
                cur.execute("delete from courses where id = %s", (course_id,))
                print(f"deleted malformed course: {course_code} / {title}")

        conn.commit()


if __name__ == "__main__":
    cleanup()
