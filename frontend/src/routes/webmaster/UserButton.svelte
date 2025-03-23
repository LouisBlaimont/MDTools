<script>
  import { modals } from "svelte-modals";
  import EditUserModal from "$lib/modals/editUserModal.svelte";
  import Icon from "@iconify/svelte";
  import { toast } from "@zerodevx/svelte-toast";

  const { user, roles } = $props();

  function editUser() {
    event.stopPropagation();
    modals.open(EditUserModal, { user: user, roles: roles });
  }

  const disableAccount = () => {
    user.enabled = !user.enabled;
    if (user.enabled) {
      toast.push(`Le compte de ${user.email} a été activé.`);
    } else {
      toast.push(`Le compte de ${user.email} a été désactivé.`);
    }
  };
</script>

<button
  onclick={() => disableAccount()}
  class={`px-5 py-2 rounded-l-lg w-36 flex items-center transform transition ${
    user.enabled
      ? "bg-red-600 hover:bg-red-700 text-white"
      : "bg-green-600 hover:bg-green-700 text-white"
  }`}
>
  <span>
<svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      stroke-width="1.5"
      stroke="currentColor"
      class="size-6"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632Z"
      />
    </svg>
  </span>
  <span class="pl-2">{user.enabled ? "Désactiver" : "Activer"}</span>
</button>

<button
  onclick={() => editUser()}
  class="bg-cyan-500 text-white px-5 py-2 rounded-r-lg hover:bg-cyan-600 flex items-center transform transition"
  >
<span>
    <Icon icon="material-symbols:edit-rounded" width="24" height="24" />
  </span>
  <span class="pl-2">Modifier l'utilisateur</span>
</button>
