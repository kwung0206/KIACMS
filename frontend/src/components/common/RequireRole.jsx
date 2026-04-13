import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getHomePathByRole } from "../../utils/navigation";

export default function RequireRole({ roles }) {
  const { user } = useAuth();

  if (!user || !roles.includes(user.roleType)) {
    return <Navigate to={getHomePathByRole(user?.roleType)} replace />;
  }

  return <Outlet />;
}
