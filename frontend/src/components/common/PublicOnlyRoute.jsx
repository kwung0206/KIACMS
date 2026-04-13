import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getHomePathByRole } from "../../utils/navigation";
import LoadingScreen from "./LoadingScreen";

export default function PublicOnlyRoute() {
  const { isAuthenticated, isBootstrapping, user } = useAuth();

  if (isBootstrapping) {
    return <LoadingScreen message="화면을 준비하고 있습니다..." />;
  }

  if (isAuthenticated) {
    return <Navigate to={getHomePathByRole(user?.roleType)} replace />;
  }

  return <Outlet />;
}
