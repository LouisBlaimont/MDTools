import { writable, derived } from "svelte/store";
import { createPersistentStore } from "../utils/persistentStore";

export const user = createPersistentStore("user", null);
export const authChecking = writable(false);
export const authReady = writable(false);
export const userId = derived(user, ($user) => $user?.id ?? null);
export const isLoggedIn = derived(user, ($user) => $user !== null);
export const isAdmin = derived(user, ($user) => $user?.roles?.includes("ROLE_ADMIN") ?? false);
export const isUser = derived(user, ($user) => $user?.roles?.includes("ROLE_USER") ?? false);
export const isWebmaster = derived(user, ($user) => $user?.roles?.includes("ROLE_WEBMASTER") ?? false);
