import { PUBLIC_API_URL } from "$env/static/public";
import { clearPersistentStore } from "./persistentStore";

export const apiFetch = (url, options = {}) => {
   return fetch(PUBLIC_API_URL + url, {
     ...options,
     credentials: 'include',  // Always include cookies in requests
   }).then(response => {
    if(!response.ok) {
      if(response.status == 401) {
        console.log("Unauthorized");
        clearPersistentStore("user");
        isLoggedIn.set(false);
      } else if (response.status == 403) {
        console.log("Forbidden");
      }
    }
    return response;
  });
};
 