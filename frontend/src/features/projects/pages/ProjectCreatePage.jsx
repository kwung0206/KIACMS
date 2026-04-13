import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createProjectPost } from "../../../api/projectApi";
import FormField from "../../../components/common/FormField";
import PageHeader from "../../../components/common/PageHeader";
import { getTodayDateKey } from "../../../utils/date";

const CONTACT_METHOD_OPTIONS = [
  { value: "EMAIL", label: "이메일" },
  { value: "OPEN_CHAT", label: "오픈채팅" },
  { value: "DISCORD", label: "디스코드" },
  { value: "NOTION", label: "노션" },
  { value: "GOOGLE_FORM", label: "구글 폼" },
  { value: "OTHER", label: "기타" },
];

const emptyPosition = {
  name: "",
  description: "",
  requiredSkills: "",
  capacity: 1,
};

export default function ProjectCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    description: "",
    goal: "",
    techStack: "",
    durationText: "",
    contactMethod: "EMAIL",
    contactValue: "",
    pmIntroduction: "",
    pmBackground: "",
    recruitUntil: getTodayDateKey(),
    positions: [{ ...emptyPosition }],
  });
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function updatePosition(index, field, value) {
    setForm((current) => ({
      ...current,
      positions: current.positions.map((position, positionIndex) =>
        positionIndex === index ? { ...position, [field]: value } : position,
      ),
    }));
  }

  function addPosition() {
    if (form.positions.length >= 10) {
      setMessage("모집 포지션은 최대 10개까지 등록할 수 있습니다.");
      return;
    }

    setMessage("");
    setForm((current) => ({
      ...current,
      positions: [...current.positions, { ...emptyPosition }],
    }));
  }

  function removePosition(index) {
    if (form.positions.length === 1) {
      setMessage("최소 1개의 모집 포지션이 필요합니다.");
      return;
    }

    setMessage("");
    setForm((current) => ({
      ...current,
      positions: current.positions.filter((_, positionIndex) => positionIndex !== index),
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");

    try {
      const response = await createProjectPost({
        ...form,
        positions: form.positions.map((position) => ({
          ...position,
          capacity: Number(position.capacity),
        })),
      });

      navigate(`/projects/${response.id}`, { replace: true });
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page-stack">
      <PageHeader
        title="새 모집글 작성"
        description="프로젝트 개요와 모집 포지션을 입력해 학생 지원을 받을 수 있는 모집글을 등록합니다."
        actions={
          <Link className="ghost-button button-small uniform-action-button" to="/student/projects/me">
            내 모집글
          </Link>
        }
      />

      <form className="panel form-stack form-stack-relaxed" onSubmit={handleSubmit}>
        <FormField label="프로젝트 제목">
          <input name="title" value={form.title} onChange={handleChange} required />
        </FormField>

        <FormField label="프로젝트 소개" hint="프로젝트의 핵심 아이디어와 진행 방향을 적어 주세요.">
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            rows={4}
            required
          />
        </FormField>

        <FormField label="목표" hint="최종 결과물이나 해결하려는 문제를 명확히 적어 주세요.">
          <textarea name="goal" value={form.goal} onChange={handleChange} rows={3} required />
        </FormField>

        <FormField label="기술 스택" hint="주요 언어, 프레임워크, 협업 도구를 함께 적어 주세요.">
          <textarea
            name="techStack"
            value={form.techStack}
            onChange={handleChange}
            rows={3}
            required
          />
        </FormField>

        <div className="form-grid">
          <FormField label="예상 진행 기간">
            <input name="durationText" value={form.durationText} onChange={handleChange} required />
          </FormField>

          <FormField label="모집 마감일">
            <input
              type="date"
              name="recruitUntil"
              value={form.recruitUntil}
              onChange={handleChange}
              min={getTodayDateKey()}
              required
            />
          </FormField>
        </div>

        <div className="form-grid">
          <FormField label="연락 방식">
            <select name="contactMethod" value={form.contactMethod} onChange={handleChange}>
              {CONTACT_METHOD_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </FormField>

          <FormField label="연락처" hint="이메일 주소, 링크, 아이디 등 실제 연락 가능한 값을 입력해 주세요.">
            <input name="contactValue" value={form.contactValue} onChange={handleChange} required />
          </FormField>
        </div>

        <FormField label="PM 소개">
          <textarea
            name="pmIntroduction"
            value={form.pmIntroduction}
            onChange={handleChange}
            rows={3}
            required
          />
        </FormField>

        <FormField
          label="PM 경험 및 강점"
          hint="수강 이력, 프로젝트 경험, 협업 경험 등 팀원에게 도움이 될 내용을 적어 주세요."
        >
          <textarea
            name="pmBackground"
            value={form.pmBackground}
            onChange={handleChange}
            rows={3}
            required
          />
        </FormField>

        <div className="panel nested-panel nested-panel-soft">
          <div className="section-title-row">
            <div>
              <h2>모집 포지션</h2>
              <p className="muted-text">지원자가 선택할 역할과 인원을 설정합니다.</p>
            </div>
            <button className="ghost-button button-small" type="button" onClick={addPosition}>
              포지션 추가
            </button>
          </div>

          <div className="list-stack">
            {form.positions.map((position, index) => (
              <div key={`position-${index}`} className="info-card form-card">
                <div className="spread-row">
                  <strong>{index + 1}번 포지션</strong>
                  <button
                    className="text-button danger-text-button"
                    type="button"
                    onClick={() => removePosition(index)}
                    disabled={form.positions.length === 1}
                  >
                    삭제
                  </button>
                </div>

                <div className="form-grid">
                  <FormField label="포지션명">
                    <input
                      value={position.name}
                      onChange={(event) => updatePosition(index, "name", event.target.value)}
                      required
                    />
                  </FormField>

                  <FormField label="모집 인원">
                    <input
                      type="number"
                      min="1"
                      value={position.capacity}
                      onChange={(event) => updatePosition(index, "capacity", event.target.value)}
                      required
                    />
                  </FormField>
                </div>

                <FormField label="포지션 설명">
                  <textarea
                    value={position.description}
                    onChange={(event) => updatePosition(index, "description", event.target.value)}
                    rows={3}
                  />
                </FormField>

                <FormField label="필수 역량">
                  <input
                    value={position.requiredSkills}
                    onChange={(event) => updatePosition(index, "requiredSkills", event.target.value)}
                  />
                </FormField>
              </div>
            ))}
          </div>
        </div>

        {message ? <div className="form-alert error">{message}</div> : null}

        <div className="button-row">
          <button className="primary-button button-small" type="submit" disabled={saving}>
            {saving ? "등록 중..." : "모집글 등록"}
          </button>
        </div>
      </form>
    </div>
  );
}
