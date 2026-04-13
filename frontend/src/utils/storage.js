const TOKEN_KEY = "kiacms.accessToken";
const USER_KEY = "kiacms.user";
const THEME_KEY = "kiacms.theme";
const PENDING_SIGNUP_KEY = "kiacms.pendingSignup";

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredToken() {
  localStorage.removeItem(TOKEN_KEY);
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch (error) {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function setStoredUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearStoredUser() {
  localStorage.removeItem(USER_KEY);
}

export function getStoredTheme() {
  return localStorage.getItem(THEME_KEY);
}

export function setStoredTheme(theme) {
  localStorage.setItem(THEME_KEY, theme);
}

export function getPendingSignup() {
  const raw = sessionStorage.getItem(PENDING_SIGNUP_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch (error) {
    sessionStorage.removeItem(PENDING_SIGNUP_KEY);
    return null;
  }
}

export function setPendingSignup(payload) {
  sessionStorage.setItem(PENDING_SIGNUP_KEY, JSON.stringify(payload));
}

export function clearPendingSignup() {
  sessionStorage.removeItem(PENDING_SIGNUP_KEY);
}
