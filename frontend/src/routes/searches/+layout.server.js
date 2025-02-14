/*import { redirect } from "@sveltejs/kit";

import { checkRoleAndPermissions } from "$lib/rbacUtils";
import { PERMISSIONS, ROLES } from "../../constants.js";

export function load({ locals }) {
	const user = locals.user;
	const isCustomerAndCanViewProfile = checkRoleAndPermissions(
		user,
		ROLES.CUSTOMER,
		PERMISSIONS.READ_PROFILE
	);

	if (!isCustomerAndCanViewProfile) {
		redirect(307, "/unauthorized");
	}
}*/