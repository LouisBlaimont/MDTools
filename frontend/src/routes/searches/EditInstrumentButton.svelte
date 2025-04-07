<script>
  import { modals } from "svelte-modals";
  import editInstrumentModal from "$lib/modals/editInstrumentModal.svelte";
  import Icon from "@iconify/svelte";

  export let instrument;

  async function editInstrumentButton() {
    event.stopPropagation();
    await modals.open(editInstrumentModal, { instrument });
  }
</script>

<td class="text-center border border-solid border-[black] bg-cover hover:bg-yellow-500 w-[30px]">
  <button
    class="cursor-pointer"
    onclick={() => editInstrumentButton()}
    aria-label="Edit Instrument"
  >
    <Icon icon="material-symbols:edit" width="20" height="20" />
  </button>
</td>
