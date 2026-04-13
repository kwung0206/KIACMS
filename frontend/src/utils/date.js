const KOREA_TIME_ZONE = "Asia/Seoul";

function formatToParts(value, options) {
  return new Intl.DateTimeFormat("ko-KR", {
    timeZone: KOREA_TIME_ZONE,
    ...options,
  }).format(new Date(value));
}

function getKoreaParts(value = new Date()) {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: KOREA_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

  const parts = formatter.formatToParts(new Date(value));
  const year = parts.find((part) => part.type === "year")?.value;
  const month = parts.find((part) => part.type === "month")?.value;
  const day = parts.find((part) => part.type === "day")?.value;

  return {
    year: Number(year),
    month: Number(month),
    day: Number(day),
  };
}

function toDateKeyFromParts({ year, month, day }) {
  return `${String(year).padStart(4, "0")}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function daysInMonth(year, month) {
  return new Date(year, month, 0).getDate();
}

export function getTodayDateKey() {
  return toDateKeyFromParts(getKoreaParts());
}

export function formatDateTime(value) {
  if (!value) {
    return "-";
  }

  return formatToParts(value, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function formatDate(value) {
  if (!value) {
    return "-";
  }

  return formatToParts(value, {
    dateStyle: "medium",
  });
}

export function formatTime(value) {
  if (!value) {
    return "-";
  }

  if (typeof value === "string" && /^\d{2}:\d{2}/.test(value)) {
    return value.slice(0, 5);
  }

  return formatToParts(value, {
    timeStyle: "short",
  });
}

export function getDateKey(value) {
  if (!value) {
    return "";
  }

  if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return value;
  }

  return toDateKeyFromParts(getKoreaParts(value));
}

export function todayRange() {
  return getMonthRange(new Date());
}

export function addDays(baseDateString, offset) {
  const [year, month, day] = baseDateString.split("-").map(Number);
  const date = new Date(year, month - 1, day);
  date.setDate(date.getDate() + offset);
  return toDateKeyFromParts({
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    day: date.getDate(),
  });
}

export function addMonths(baseDate, offset) {
  const next = new Date(baseDate);
  next.setDate(1);
  next.setMonth(next.getMonth() + offset);
  return next;
}

export function getMonthRange(baseDate) {
  const { year, month } = getKoreaParts(baseDate);

  return {
    from: toDateKeyFromParts({ year, month, day: 1 }),
    to: toDateKeyFromParts({ year, month, day: daysInMonth(year, month) }),
  };
}

export function formatMonthTitle(baseDate) {
  return new Intl.DateTimeFormat("ko-KR", {
    timeZone: KOREA_TIME_ZONE,
    year: "numeric",
    month: "long",
  }).format(new Date(baseDate));
}

export function buildMonthGrid(baseDate) {
  const firstDay = new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
  const lastDay = new Date(baseDate.getFullYear(), baseDate.getMonth() + 1, 0);
  const start = new Date(firstDay);
  start.setDate(start.getDate() - firstDay.getDay());
  const end = new Date(lastDay);
  end.setDate(end.getDate() + (6 - lastDay.getDay()));

  const days = [];
  const cursor = new Date(start);

  while (cursor <= end) {
    days.push(new Date(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }

  return days;
}

export function isSameMonth(baseDate, comparedDate) {
  return (
    baseDate.getFullYear() === comparedDate.getFullYear() &&
    baseDate.getMonth() === comparedDate.getMonth()
  );
}

export function isSameDateKey(left, right) {
  return Boolean(left) && Boolean(right) && left === right;
}
