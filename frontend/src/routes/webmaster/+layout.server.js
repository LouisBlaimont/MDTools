import { redirect } from "@sveltejs/kit";

import { checkRole } from "$lib/rbacUtils";
import { ROLES } from "../../constants.js";

/** @type {import('./$types.js').LayoutServerLoad} */
export function load({ locals }) {
	const user = locals.user;
	const isWebmaster = checkRole(user, ROLES.WEBMASTER);

	if (!isWebmaster) {
	 	redirect(403, "/unauthorized");
 	}
}
