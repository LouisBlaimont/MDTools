import { writable, derived } from "svelte/store";
import { createPersistentStore } from "../utils/persistentStore";

export const user = createPersistentStore("user", null);
export const userId = derived(user, ($user) => $user?.id ?? null);
export const isLoggedIn = derived(user, ($user) => $user !== null);
export const isAdmin = derived(user, ($user) => $user?.roles?.includes("ROLE_ADMIN"));
export const isUser = derived(user, ($user) => $user?.roles?.includes("ROLE_USER"));
export const isWebmaster = derived(user, ($user) => $user?.roles?.includes("ROLE_WEBMASTER"));
