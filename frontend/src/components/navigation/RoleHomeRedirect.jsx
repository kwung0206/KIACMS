import { Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getHomePathByRole } from "../../utils/navigation";

export default function RoleHomeRedirect() {
  const { user } = useAuth();
  return <Navigate to={getHomePathByRole(user?.roleType)} replace />;
}
