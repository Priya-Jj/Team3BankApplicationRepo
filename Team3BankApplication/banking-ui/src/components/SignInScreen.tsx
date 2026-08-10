/**
 * Sign-in screen shown to unauthenticated users.
 *
 * Renders a single anchor tag pointing at the BFF's OAuth login URL. We use an
 * anchor (full page navigation) rather than a fetch call because the OAuth flow
 * involves redirects through the auth server that the browser must follow on
 * its own. fetch and AJAX cannot follow cross-origin redirects to HTML pages.
 */

export function SignInScreen() {
  return (
    <section className="sign-in-screen">
      <h2>Welcome to Team3's Bank</h2>
      <p>Sign in to view your accounts and transfer funds.</p>
      <a href="/oauth2/authorization/bank-auth" className="sign-in-button">
        Sign in
      </a>
    </section>
  );
}