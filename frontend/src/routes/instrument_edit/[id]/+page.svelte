<script>
    import { fetchToolById, editTool, deleteTool } from "../../../api.js";
    import { onMount } from "svelte";
    import { page } from '$app/stores';
    import { goto } from '$app/navigation';

    let toolId;
    let tool;

    $: toolId = $page.params.id;

    onMount(async () => {
        if (toolId) {
            tool = await fetchToolById(toolId);
            console.log('Tool:', tool);
        }
    });

    async function saveChanges() {
        if (tool) {
            await editTool(toolId, tool);
            console.log('Tool saved:', tool);
        }
    }

    async function deleteInstrument() {
        if (toolId) {
            const confirmed = window.confirm("Are you sure you want to delete this instrument?");
            if (confirmed) {
                await deleteTool(toolId);
                console.log('Tool deleted:', toolId);
                goto('/searches_admin');
            }
        }
    }
</script>

<div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">Edit Instrument</h1>
    {#if tool}
        <div class="flex flex-col gap-4">
            <div>
                <label class="block font-semibold">Reference:</label>
                <input type="text" bind:value={tool.ref} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Brand:</label>
                <input type="text" bind:value={tool.brand} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Description:</label>
                <input type="text" bind:value={tool.description} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Price:</label>
                <input type="text" bind:value={tool.price} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Alternatives:</label>
                <input type="text" bind:value={tool.alt} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Obsolete:</label>
                <input type="text" bind:value={tool.obs} class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Image Source:</label>
                <input type="text" bind:value={tool.src} class="border p-2 rounded w-full">
            </div>
            <div class="flex gap-4">
                <button class="bg-green-500 text-white p-2 rounded hover:bg-green-700" 
                    on:click={saveChanges}>Save</button>
                <button class="bg-red-500 text-white p-2 rounded hover:bg-red-700" 
                    on:click={deleteInstrument}>Delete</button>
            </div>
        </div>
    {/if}
</div>
