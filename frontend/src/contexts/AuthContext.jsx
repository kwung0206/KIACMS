import { createContext, useEffect, useMemo, useState } from "react";
import * as authApi from "../api/authApi";
import { fetchMyProfile } from "../api/userApi";
import {
  clearPendingSignup,
  clearStoredToken,
  clearStoredUser,
  getPendingSignup,
  getStoredToken,
  getStoredUser,
  setPendingSignup,
  setStoredToken,
  setStoredUser,
} from "../utils/storage";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredToken());
  const [user, setUser] = useState(() => getStoredUser());
  const [pendingSignup, setPendingSignupState] = useState(() => getPendingSignup());
  const [isBootstrapping, setIsBootstrapping] = useState(true);

  function setAuthSession(nextToken, nextUser) {
    setToken(nextToken);
    setUser(nextUser);

    if (nextToken) {
      setStoredToken(nextToken);
    } else {
      clearStoredToken();
    }

    if (nextUser) {
      setStoredUser(nextUser);
    } else {
      clearStoredUser();
    }
  }

  function clearPendingSignupState() {
    clearPendingSignup();
    setPendingSignupState(null);
  }

  useEffect(() => {
    function handleUnauthorized() {
      logout();
    }

    window.addEventListener("kiacms:unauthorized", handleUnauthorized);
    return () => window.removeEventListener("kiacms:unauthorized", handleUnauthorized);
  }, []);

  useEffect(() => {
    async function bootstrap() {
      if (!token) {
        if (user) {
          setUser(null);
          clearStoredUser();
        }
        setIsBootstrapping(false);
        return;
      }

      try {
        const profile = await fetchMyProfile();
        setAuthSession(token, profile);
      } catch (error) {
        setAuthSession(null, null);
      } finally {
        setIsBootstrapping(false);
      }
    }

    bootstrap();
  }, [token]);

  async function login(credentials) {
    const response = await authApi.login(credentials);
    setAuthSession(response.accessToken, response.user);
    clearPendingSignupState();
    return response;
  }

  async function signup(payload) {
    const response = await authApi.signup(payload);
    setPendingSignup(response);
    setPendingSignupState(response);
    return response;
  }

  function logout() {
    setAuthSession(null, null);
    clearPendingSignupState();
  }

  async function refreshProfile() {
    const profile = await fetchMyProfile();
    setAuthSession(token, profile);
    return profile;
  }

  const value = useMemo(
    () => ({
      token,
      user,
      pendingSignup,
      isBootstrapping,
      isAuthenticated: Boolean(token && user),
      login,
      signup,
      logout,
      refreshProfile,
    }),
    [token, user, pendingSignup, isBootstrapping],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
