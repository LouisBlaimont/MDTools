import { writable } from "svelte/store";

export const user = writable({ role: 'admin' });
// for admin view: export const user = writable({ role: 'admin' });