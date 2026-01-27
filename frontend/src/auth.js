import { PUBLIC_API_URL } from "$env/static/public";
import { PUBLIC_OIDC_ENDPOINT } from "$env/static/public";
import { user, authChecking, authReady } from "$lib/stores/user_stores";
import { apiFetch } from "$lib/utils/fetch";
import { createPersistentStore, clearPersistentStore } from "$lib/utils/persistentStore";
import { toast } from "@zerodevx/svelte-toast";

export function login() {
  user.set(null);
  clearPersistentStore("user");
  window.location.href = PUBLIC_API_URL + "/oauth2/authorization/" + PUBLIC_OIDC_ENDPOINT; // Backend URL for OAuth login
}

// check if user is logged in using the API
export async function checkUser() {
  authChecking.set(true);

  try {
    const res = await apiFetch("/api/auth/me");
    if (!res.ok) throw new Error("Not authenticated");

    const me = await res.json();
    user.set(me);
  } catch (e) {
    user.set(null);
  } finally {
    authChecking.set(false);
    authReady.set(true);
  }
}

// Handle authentication
export async function handleLogin() {
  login();
}

export async function handleLogout() {
  try {
    // 1) Get Keycloak logout URL (needs auth, so do it BEFORE local logout)
    const urlRes = await apiFetch("/api/auth/logout-url");
    if (!urlRes.ok) {
      toast.push("Logout failed", {
        theme: {
          "--toastBackground": "#f44336",
          "--toastColor": "#ffffff",
        },
      });
      console.error("Logout failed (logout-url)", urlRes.status);
      return;
    }

    const { logoutUrl } = await urlRes.json();

    // 2) Local logout (invalidate Spring session)
    const res = await apiFetch("/api/auth/logout", { method: "POST" });
    if (!res.ok) {
      toast.push("Logout failed", {
        theme: {
          "--toastBackground": "#f44336",
          "--toastColor": "#ffffff",
        },
      });
      console.error("Logout failed (local logout)", res.status);
      return;
    }

    // 3) Clear frontend state
    user.set(null);
    clearPersistentStore("user");

    // 4) Show success toast AFTER redirect back from Keycloak
    sessionStorage.setItem("logout_toast", "1");

    // 5) Redirect to Keycloak end_session
    window.location.assign(logoutUrl);
  } catch (e) {
    toast.push("Logout failed", {
      theme: {
        "--toastBackground": "#f44336",
        "--toastColor": "#ffffff",
      },
    });
    console.error("Logout failed", e);
  }
}
