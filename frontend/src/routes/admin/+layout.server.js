import { redirect } from "@sveltejs/kit";
import { checkRole } from "$lib/rbacUtils";
import { ROLES } from "../../constants.js";

export function load({ locals }) {
	const user = locals.user;
	const isAdmin = checkRole(user, ROLES.ADMIN);

	if (!isAdmin) {
	 	redirect(403, "/unauthorized");
 	}
}
