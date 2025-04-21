import { PUBLIC_API_URL } from "$env/static/public";
import { PUBLIC_OIDC_ENDPOINT } from "$env/static/public";
import { user } from "$lib/stores/user_stores";
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
  const res = await apiFetch("/api/auth/me");
  if (res.ok) {
    const data = await res.json();
    user.set(data);
    console.log("User data:", data);
  } else {
    user.set(null);
  }
}

// Handle authentication
export async function handleLogin() {
  login();
}

export async function handleLogout() {
  const res = await apiFetch("/api/auth/logout");
  if (res.ok) {
    user.set(null);
    clearPersistentStore("user");
    toast.push("You have successfully log out !")
  } else {
    toast.push("Logout failed", {
      theme: {
        "--toastBackground": "#f44336",
        "--toastColor": "#ffffff",
      },
    });
    console.error("Logout failed");
  }
}
