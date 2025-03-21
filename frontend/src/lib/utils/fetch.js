import { PUBLIC_API_URL } from "$env/static/public";
import { clearPersistentStore, persistentStoreExists } from "./persistentStore";
import { isLoggedIn, user } from "../stores/user_stores";
import { get } from "svelte/store";

export const apiFetch = (url, options = {}) => {
   return fetch(PUBLIC_API_URL + url, {
     ...options,
     credentials: 'include',  // Always include cookies in requests
   }).then(response => {
    if(!response.ok) {
      const isli = get(isLoggedIn);
      if(response.status == 401 && persistentStoreExists("user") && isli) {
        console.log("Unauthorized");
        user.set(null);
      } else if (response.status == 401 && !persistentStoreExists("user")) {
        console.log("Unauthorized");
      } else if (response.status == 403) {
        console.log("Forbidden");
      }
    }
    return response;
  });
};
 