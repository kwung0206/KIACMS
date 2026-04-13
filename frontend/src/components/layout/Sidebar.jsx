import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getNavigationItems, isNavigationItemActive } from "../../utils/navigation";
import { getRoleLabel } from "../../utils/userLabels";

export default function Sidebar() {
  const { user } = useAuth();
  const location = useLocation();
  const items = getNavigationItems(user?.roleType);

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <p className="sidebar-caption">Korea IT Academy</p>
        <Link className="sidebar-home-link" to={user ? "/" : "/login"}>
          <h2>KIACMS</h2>
        </Link>
        <span className="sidebar-role">{getRoleLabel(user?.roleType)}</span>
      </div>

      <nav className="sidebar-nav">
        {items.map((item) => (
          <Link
            key={`${item.to}-${item.label}`}
            to={item.to}
            className={
              isNavigationItemActive(item, location.pathname)
                ? "sidebar-link sidebar-link-active"
                : "sidebar-link"
            }
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}
