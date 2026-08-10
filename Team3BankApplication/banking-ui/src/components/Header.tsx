/**
 * Header component.
 *
 * Reads the current user from the auth context. When a user is logged in, shows
 * their name and a Sign-out button.
 */

import { useAuth } from '../auth/AuthContext';
import { logout as logoutApi } from '../api/client';

export function Header() {
  const { user, refresh } = useAuth();

  async function handleSignOut() {
    window.location.href = '/logout';
  }

  return (
    <header className="header">
      <div className="header-content">
        <div>
          <h1>Team 3's Bank</h1>
          <p className="tagline">Online Banking</p>
        </div>
        {user && (
          <div className="header-user">
            <span className="user-name">Hello, {user.preferredUsername}</span>
            <button type="button" onClick={handleSignOut} className="sign-out-button">
              Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  );
}