<!-- {id : 1, ref : "04.54.16", brand : "MEDICON", description : "KELLY SCRS STR 16CM",price :"66.65" , alt :"" , obs :"", src : "/supplier1.png"}, -->

<script>
    import { suppliers } from '../../../suppliers.js';
    import { onMount } from 'svelte';
    import { page } from '$app/stores';

    let toolId;
    let tool;

    $: toolId = $page.params.id;

    console.log('Tool ID:', toolId);

    onMount(() => {
        tool = suppliers.flat().find(t => t.id == toolId);
        console.log('Tool:', tool);
    });

    function saveChanges() {
        // Logic to save changes to the tool
    }

    function deleteTool() {
        // Logic to delete the tool
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
                    on:click={deleteTool}>Delete</button>
            </div>
        </div>
    {/if}
</div>
