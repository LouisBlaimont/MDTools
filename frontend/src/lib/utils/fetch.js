import { PUBLIC_API_URL } from "$env/static/public";
import { clearPersistentStore } from "./persistentStore";
import { isLoggedIn, user, authReady } from "../stores/user_stores";
import { get } from "svelte/store";

/**
 * Performs a hard redirect to the login page.
 *
 * Why hard redirect instead of SvelteKit `goto()`?
 * - This utility is used inside a low-level fetch wrapper.
 * - When the app is in a "zombie" state (stale stores, broken UI, pending modals),
 *   `goto()` might not run reliably. A hard redirect is the most robust option.
 */
function redirectToLogin() {
  if (typeof window !== "undefined" && window.location.pathname !== "/login") {
    window.location.href = "/login";
  }
}

/**
 * Performs a hard redirect to the unauthorized page (403).
 *
 * Use this when the user is authenticated but does not have the required permissions.
 */
function redirectToUnauthorized() {
  if (typeof window !== "undefined" && window.location.pathname !== "/unauthorized") {
    window.location.href = "/unauthorized";
  }
}

/**
 * API fetch wrapper for the Spring backend.
 *
 * Features:
 * - Always includes cookies (Spring session) via `credentials: "include"`.
 * - Detects authentication problems and recovers cleanly:
 *   - 401 Unauthorized: clears local user state and redirects to /login.
 *   - 403 Forbidden: redirects to /unauthorized.
 *
 * Expected backend behavior:
 * - 401 means the session is missing/expired or the user is not authenticated.
 * - 403 means the user is authenticated but not allowed to access the resource.
 *
 * @param {string} url - API path starting with `/api/...` (without PUBLIC_API_URL prefix)
 * @param {RequestInit} [options={}] - Standard fetch options (method, headers, body, etc.)
 * @returns {Promise<Response>} - The original fetch Response.
 */
export const apiFetch = (url, options = {}) => {
  return fetch(PUBLIC_API_URL + url, {
    ...options,
    credentials: "include",
  }).then((response) => {
    if (!response.ok) {
      if (response.status === 401) {
        const loggedIn = get(isLoggedIn);

        console.log("Unauthorized (401) - clearing local session and redirecting to /login.");

        // Clear frontend auth state and persisted user data.
        // This prevents the UI from staying in a stale "logged in" state.
        user.set(null);
        clearPersistentStore("user");

        // Mark auth as ready so UI guards can react immediately.
        // Even if you do not use this elsewhere, it is safe and helps avoid inconsistent states.
        authReady.set(true);

        // Redirect to login if we believed we were logged in (or if you want to force login on any 401).
        // Keeping the `loggedIn` check avoids redirect loops on the login page itself.
        if (loggedIn) {
          redirectToLogin();
        }
      } else if (response.status === 403) {
        console.log("Forbidden (403) - redirecting to /unauthorized.");
        redirectToUnauthorized();
      }
    }

    return response;
  });
};
