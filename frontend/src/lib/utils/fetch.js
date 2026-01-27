import { PUBLIC_API_URL } from "$env/static/public";
import { clearPersistentStore, persistentStoreExists } from "./persistentStore";
import { isLoggedIn, user } from "../stores/user_stores";
import { get } from "svelte/store";

export const apiFetch = (url, options = {}) => {
  return fetch(PUBLIC_API_URL + url, {
    ...options,
    credentials: "include",
  }).then((response) => {
    if (!response.ok) {
      if (response.status === 401) {
        const loggedIn = get(isLoggedIn);

        console.log("Unauthorized");

        // If we thought we were logged in, clear everything
        if (loggedIn) {
          user.set(null);
          clearPersistentStore("user"); // IMPORTANT
        }
      } else if (response.status === 403) {
        console.log("Forbidden");
      }
    }
    return response;
  });
};
