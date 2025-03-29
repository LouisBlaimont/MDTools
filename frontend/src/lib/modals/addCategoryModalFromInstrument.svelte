<script>
    import { goto } from "$app/navigation";
    import { modals } from "svelte-modals";
    export let title = "Notification";
    export let message = "Message";
    
    function goToaddCategory() {
        console.log("Go to add category");
        modals.close();
        goto("/admin/add_category");
    }
    
</script>

<div class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    <div class="bg-white rounded-lg shadow-lg p-6 w-1/3">
        <h2 class="text-xl font-bold mb-4">{title}</h2>
        <p class="mb-6">{message}</p>
        <div class="flex justify-end gap-4 mt-4">
            <!-- Button to close the modal -->
            <button
                class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700"
                on:click={() => modals.close()}
            >
                Retour
            </button>

            <!-- Button to navigate to the add_category page -->
            <button
                class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700"
                on:click={() => goToaddCategory()}
            >
                Ajouter une catégorie
            </button>
        </div>
    </div>
</div>