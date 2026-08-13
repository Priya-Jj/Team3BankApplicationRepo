/**
 * Header component.
 *
 * Reads the current user from the auth context. When a user is logged in, shows
 * their name, role, and a Sign-out button.
 */

import { useAuth } from '../auth/AuthContext';

export function Header() {
  const { user } = useAuth();

  async function handleSignOut() {
    window.location.href = '/logout';
  }

  const getUserRoleDisplay = () => {
    if (!user?.roles || user.roles.length === 0) {
      return 'User';
    }
    // Format role for display (e.g., 'account_holder' -> 'Account Holder')
    return user.roles
      .map(role => role.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' '))
      .join(', ');
  };

  return (
    <header className="header">
      <div className="header-content">
        <div>
          <h1>Team 3's Bank</h1>
          <p className="tagline">Trusted banking for modern customers</p>
        </div>
        {user && (
          <div className="header-user">
            <div className="user-info">
              <span className="user-name">Hello, {user.preferredUsername}</span>
              <span className="user-role">Role: {getUserRoleDisplay()}</span>
            </div>
            <button type="button" onClick={handleSignOut} className="sign-out-button">
              Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  );
}