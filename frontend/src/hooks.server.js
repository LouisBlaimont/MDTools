import { getUser } from "$lib/auth"; // Your function to get user data
import { error } from "@sveltejs/kit";

export async function handle({ event, resolve }) {
  const user = await getUser(event);

  // Protect all /admin routes
  if (event.url.pathname.startsWith("/admin") && user.role !== "admin") {
    throw error(403, "Forbidden: Admins only");
  }

  return resolve(event);
}
