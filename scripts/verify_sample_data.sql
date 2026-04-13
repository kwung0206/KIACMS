-- Seed user counts
select role_type, count(*) as user_count
from users
where email like 'seed.student%@kiacms.local'
   or email like 'seed.instructor%@kiacms.local'
group by role_type
order by role_type;

-- Seed course/session overview
select
    c.course_code,
    c.title,
    c.track_name,
    c.status,
    count(cs.id) as session_count,
    min(cs.session_date) as first_session_date,
    max(cs.session_date) as last_session_date
from courses c
left join course_sessions cs on cs.course_id = c.id
where c.course_code like 'SEED-%'
group by c.course_code, c.title, c.track_name, c.status
order by c.course_code;

-- Enrollment counts by seed course
select
    c.course_code,
    c.title,
    count(e.id) as enrolled_students
from courses c
left join enrollments e on e.course_id = c.id and e.status = 'ENROLLED'
where c.course_code like 'SEED-%'
group by c.course_code, c.title
order by c.course_code;

-- Project, position, and application overview
select
    p.title,
    owner.name as owner_name,
    p.recruit_until,
    p.status,
    count(distinct pos.id) as position_count,
    count(distinct app.id) as application_count,
    count(distinct mapp.id) as mentor_application_count
from project_posts p
join users owner on owner.id = p.owner_id
left join project_positions pos on pos.project_post_id = p.id
left join project_applications app on app.project_position_id = pos.id
left join mentor_applications mapp on mapp.project_post_id = p.id
where p.title in (
    '캠퍼스 스터디 매칭 웹앱',
    '멀티모달 강의 요약 도우미',
    '보안 로그 이상 탐지 대시보드'
)
group by p.title, owner.name, p.recruit_until, p.status
order by p.title;

-- Existing demo student calendar readiness
select
    u.email,
    count(distinct e.id) as enrolled_courses,
    count(distinct cs.id) as upcoming_sessions
from users u
left join enrollments e on e.student_id = u.id and e.status = 'ENROLLED'
left join course_sessions cs on cs.course_id = e.course_id and cs.session_date >= date '2026-04-13'
where u.email in ('student@kiacms.local', 'seed.student01@kiacms.local', 'seed.student02@kiacms.local')
group by u.email
order by u.email;

-- Mentor management readiness
select
    mentor.email as mentor_email,
    count(msm.id) as managed_students
from mentor_student_mappings msm
join users mentor on mentor.id = msm.mentor_id
where mentor.email = 'mentor@kiacms.local'
  and msm.status = 'ACTIVE'
group by mentor.email;

-- Notification readiness
select
    u.email,
    count(n.id) as notification_count
from notifications n
join users u on u.id = n.recipient_id
where u.email in ('student@kiacms.local', 'teacher@kiacms.local', 'mentor@kiacms.local')
group by u.email
order by u.email;
