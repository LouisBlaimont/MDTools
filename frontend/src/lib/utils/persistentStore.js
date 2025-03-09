import { writable } from "svelte/store";

// Utility function to create a persistent store
export function createPersistentStore(key, initialValue) {
  // Ensure localStorage is available in the browser (window is defined)
  let storedValue = initialValue;
  if (typeof window !== "undefined") {
    const stored = localStorage.getItem(key);
    storedValue = stored ? JSON.parse(stored) : initialValue;
  }

  const store = writable(storedValue);

  // Subscribe to store changes and update localStorage (only in the browser)
  store.subscribe((value) => {
    if (typeof window !== "undefined") {
      if (value !== null) {
        localStorage.setItem(key, JSON.stringify(value));
      } else {
        localStorage.removeItem(key); // Clear storage on logout
      }
    }
  });

  return store;
}

export function clearPersistentStore(key) {
  console.log("Clearing persistent store: " + key);
  localStorage.removeItem(key);
}
