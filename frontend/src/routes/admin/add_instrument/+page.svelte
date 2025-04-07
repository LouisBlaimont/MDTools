<script>
    import AddInstrumentModal from "$lib/modals/addInstrumentModal.svelte";

    let isModalOpen = true;

    function closeModal() {
        isModalOpen = false;
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <AddInstrumentModal {isModalOpen} close={closeModal} />
</main>
