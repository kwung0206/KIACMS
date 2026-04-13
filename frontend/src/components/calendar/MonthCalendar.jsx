import {
  buildMonthGrid,
  formatMonthTitle,
  getDateKey,
  isSameDateKey,
  isSameMonth,
} from "../../utils/date";

const WEEK_DAYS = ["일", "월", "화", "수", "목", "금", "토"];

export default function MonthCalendar({
  month,
  events,
  selectedDate,
  onMonthChange,
  onSelectDate,
  onEventClick,
  emptyMessage,
}) {
  const dateMap = events.reduce((accumulator, event) => {
    const key = getDateKey(event.date || event.start);
    if (!accumulator[key]) {
      accumulator[key] = [];
    }
    accumulator[key].push(event);
    return accumulator;
  }, {});

  const days = buildMonthGrid(month);

  return (
    <section className="panel calendar-panel">
      <div className="calendar-header">
        <div>
          <h2>{formatMonthTitle(month)}</h2>
          {emptyMessage ? <p className="muted-text">{emptyMessage}</p> : null}
        </div>

        <div className="calendar-actions">
          <button className="ghost-button button-small" type="button" onClick={() => onMonthChange(-1)}>
            이전 달
          </button>
          <button className="ghost-button button-small" type="button" onClick={() => onMonthChange(1)}>
            다음 달
          </button>
        </div>
      </div>

      <div className="calendar-grid">
        {WEEK_DAYS.map((day) => (
          <div key={day} className="calendar-weekday">
            {day}
          </div>
        ))}

        {days.map((day) => {
          const key = getDateKey(day);
          const dayEvents = dateMap[key] || [];
          const inCurrentMonth = isSameMonth(month, day);
          const isSelected = isSameDateKey(selectedDate, key);

          return (
            <button
              key={key}
              type="button"
              className={[
                "calendar-day",
                inCurrentMonth ? "" : "calendar-day-muted",
                isSelected ? "calendar-day-selected" : "",
              ]
                .filter(Boolean)
                .join(" ")}
              onClick={() => onSelectDate(key)}
            >
              <span className="calendar-day-number">{day.getDate()}</span>

              <div className="calendar-markers">
                {dayEvents.slice(0, 3).map((event) => (
                  <span
                    key={event.markerKey || event.id}
                    className="calendar-marker"
                    style={{ "--marker-color": event.color || "#0f62fe" }}
                    onClick={(clickEvent) => {
                      clickEvent.stopPropagation();
                      onEventClick?.(event);
                    }}
                    title={event.label || event.title}
                    role={onEventClick ? "button" : undefined}
                    tabIndex={onEventClick ? 0 : undefined}
                    onKeyDown={(keyboardEvent) => {
                      if (!onEventClick) {
                        return;
                      }

                      if (keyboardEvent.key === "Enter" || keyboardEvent.key === " ") {
                        keyboardEvent.preventDefault();
                        keyboardEvent.stopPropagation();
                        onEventClick(event);
                      }
                    }}
                  >
                    <span className="calendar-marker-dot" />
                    <span className="calendar-marker-text">{event.label || event.title}</span>
                  </span>
                ))}

                {dayEvents.length > 3 ? (
                  <span className="calendar-more">+{dayEvents.length - 3}</span>
                ) : null}
              </div>
            </button>
          );
        })}
      </div>
    </section>
  );
}
