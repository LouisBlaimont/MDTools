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
    {#if user.enabled}
      <Icon icon="material-symbols:disabled" width="24" height="24" />
    {:else}
      <Icon icon="material-symbols:check-circle" width="24" height="24" />
    {/if}
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
