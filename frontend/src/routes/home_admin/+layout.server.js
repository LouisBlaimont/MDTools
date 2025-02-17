import { redirect } from "@sveltejs/kit";

import { checkRole } from "$lib/rbacUtils";
import { ROLES } from "../../constants.js";

/** @type {import('./$types.js').LayoutServerLoad} */
export function load({ locals }) {
	const user = locals.user;
	const isAdmin = checkRole(user, ROLES.ADMIN);

	//if (!isAdmin) {
	if (!true) {
		redirect(307, "/unauthorized");
	}
}