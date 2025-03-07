<script>
    function save() {
        // Logic to save the tool
    }

    function cancel() {
        // Logic to cancel the tool creation
    }
</script>

<div class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">Add Instrument</h1>
        <div class="flex flex-col gap-4">
            <div>
                <label class="block font-semibold">Reference:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Brand:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Description:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Price:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Alternatives:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Obsolete:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div>
                <label class="block font-semibold">Image Source:</label>
                <input type="text" class="border p-2 rounded w-full">
            </div>
            <div class="flex gap-4">
                <button class="bg-green-500 text-white p-2 rounded hover:bg-green-700" 
                    on:click={save}>Save</button>
                <button class="bg-red-500 text-white p-2 rounded hover:bg-red-700" 
                    on:click={cancel}>Cancel</button>
            </div>
        </div>
</div>
