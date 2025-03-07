import { writable } from "svelte/store";

export const user = writable({ role: 'user' });
// for admin view: export const user = writable({ role: 'admin' });

