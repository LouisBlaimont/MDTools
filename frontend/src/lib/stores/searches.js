import { writable } from 'svelte/store';

export const isEditing = writable(false);

export const reload = writable(false);