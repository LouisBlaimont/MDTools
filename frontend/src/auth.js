import { PUBLIC_API_URL } from "$env/static/public";
import { PUBLIC_OIDC_ENDPOINT } from "$env/static/public";
import { user } from "$lib/stores/user_stores"; 
import { apiFetch } from "$lib/utils/fetch";
import { createPersistentStore, clearPersistentStore } from "$lib/utils/persistentStore";


export function login() {
  window.location.href = PUBLIC_API_URL + "/oauth2/authorization/" + PUBLIC_OIDC_ENDPOINT; // Backend URL for OAuth login
  user.set(null);
  clearPersistentStore("user");
}

// check if user is logged in using the API
export async function checkUser() {
  const res = await apiFetch("/api/auth/me");
  if (res.ok) {
    const data = await res.json();
    user.set(data);
  } else {
    user.set(null);
  }
  console.log("User: ", user);
}