import { clearStoredToken, clearStoredUser, getStoredToken } from "../utils/storage";

export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

export class ApiClientError extends Error {
  constructor(message, { status, code, fieldErrors, payload } = {}) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors || [];
    this.payload = payload;
  }
}

export function buildQuery(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      search.append(key, value);
    }
  });

  const query = search.toString();
  return query ? `?${query}` : "";
}

export function resolveApiUrl(path) {
  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

export async function request(path, options = {}) {
  const {
    method = "GET",
    body,
    auth = true,
    headers = {},
    signal,
  } = options;

  const token = auth ? getStoredToken() : null;
  const requestHeaders = new Headers(headers);
  const isFormData = body instanceof FormData;

  if (!isFormData && !requestHeaders.has("Content-Type")) {
    requestHeaders.set("Content-Type", "application/json");
  }

  if (token) {
    requestHeaders.set("Authorization", `Bearer ${token}`);
  }

  let response;
  try {
    response = await fetch(resolveApiUrl(path), {
      method,
      headers: requestHeaders,
      body: body === undefined ? undefined : isFormData ? body : JSON.stringify(body),
      signal,
    });
  } catch (error) {
    throw new ApiClientError(
      "서버에 연결할 수 없습니다. 프론트엔드와 백엔드 실행 상태를 확인해 주세요.",
      {
        code: "NETWORK_ERROR",
        payload: error,
      },
    );
  }

  let payload = null;
  const text = await response.text();
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch (error) {
      payload = null;
    }
  }

  if (response.status === 401) {
    clearStoredToken();
    clearStoredUser();
    window.dispatchEvent(new CustomEvent("kiacms:unauthorized"));
  }

  if (!response.ok || (payload && payload.success === false)) {
    const error = payload?.error;
    throw new ApiClientError(
      error?.message || response.statusText || `Request failed with status ${response.status}.`,
      {
        status: response.status,
        code: error?.code,
        fieldErrors: error?.fieldErrors,
        payload,
      },
    );
  }

  return payload?.data ?? null;
}
