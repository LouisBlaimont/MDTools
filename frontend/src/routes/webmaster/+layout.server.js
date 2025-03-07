import { redirect } from "@sveltejs/kit";

import { checkRole } from "$lib/rbacUtils";
import { ROLES } from "../../constants";
import { user } from "$lib/stores/user_stores"; 

/** @type {import('./$types').LayoutServerLoad} */
export function load({ locals }) {
	let userValue;
	user.subscribe(value => {
		userValue = value;
	});
	let isWebmaster = checkRole(userValue, ROLES.WEBMASTER);

	if (!isWebmaster) {
		throw redirect(403, "/unauthorized");
	}
}