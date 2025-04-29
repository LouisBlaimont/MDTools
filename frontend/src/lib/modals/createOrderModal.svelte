<script>
    import { onMount } from "svelte";
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { ordersNames, selectedOrderId, orderItems, orders } from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { getOrders } from "$lib/components/order_component";

    const {
        isOpen,
        close, 
    } = $props();

    // Add dragging functionality
    let posX = $state(0);
    let posY = $state(0);
    let offsetX = 0;
    let offsetY = 0;
    let isDragging = false;

    function startDrag(event) {
        isDragging = true;
        offsetX = event.clientX - posX;
        offsetY = event.clientY - posY;
    }

    function drag(event) {
        if (isDragging) {
            posX = event.clientX - offsetX;
            posY = event.clientY - offsetY;
        }
    }

    function stopDrag() {
        isDragging = false;
    }

    // Handle order creation
    function handleCreateOrder() {
        const newOrderName = document.getElementById("new-order-name").value.trim();
        const errorNoName = document.getElementById("error-no-name");
        if(newOrderName === '') {
            errorNoName.classList.remove('hidden');
        } else {
            errorNoName.classList.add('hidden');
            createOrder(newOrderName);
        }
    }

    async function createOrder(orderName) {
        const data = {
            userId: $userId,
            orderName: orderName,
        };
        try {
            const response = await apiFetch("/api/orders", {
                method: "POST",
                headers: { "Content-type": "application/json"},
                body: JSON.stringify(data),
            });

            if (response.status === 400) {
                const errorBadRequest = document.getElementById("error-no-order-for-user");
                errorBadRequest.classList.remove("hidden");
                throw new Error(`Failed to create new order: ${response.status}`);
            }
            else if (response.status === 404) {
                throw new Error(`Failed to create new order: ${response.status}`);
            }

            const result = await response.json();
            const errorBadRequest = document.getElementById("error-no-order-for-user");
            errorBadRequest.classList.add("hidden");
            ordersNames.set(result);
            await getOrders();
            close();
        } catch (error) {
            console.log("Error:", error);
        }
    }
</script>

{#if isOpen}
<div
    class="relative z-10"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
>
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div
        class="fixed inset-0 z-10 flex items-center justify-center"
        on:mousemove={drag}
        on:mouseup={stopDrag}
    >
        
        <div 
            class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto relative"
            style="transform: translate({posX}px, {posY}px);"
        >
            <div
                class="p-4 border-b cursor-move bg-gray-200 flex items-center justify-between rounded-t-lg select-none"
                on:mousedown={startDrag}
            >
                <h2 class="text-2xl font-bold text-teal-500 text-center" id="modal-title">{$_("modals.create_order.create_title")}</h2>
                <!-- Add order Icon -->
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    class="w-6 h-6 text-teal-500"
                >
                    <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                </svg>
            </div>

            <form on:submit|preventDefault={handleCreateOrder} class="bg-gray-100 p-6 rounded-b-lg">
                <div>
                    <label for="new-order-name" class="font-semibold text-lg">{$_("modals.create_order.name")}:</label>
                    <input
                        type="text"
                        id="new-order-name"
                        class="w-full p-2 border rounded mb-4"
                        placeholder="Enter order name"
                    />
                    <span id="error-no-name" class="text-red-600 text-sm hidden">{$_("modals.create_order.error_no_name")}</span>
                    <span id="error-no-order-for-user" class="text-red-600 text-sm hidden">{$_("modals.create_order.error_already_exists")}</span>
                </div>

                <div class="flex justify-end gap-4 mt-4">
                    <button
                        type="button"
                        class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700"
                        on:click={() => {
                            orderItems.set([]);
                            close();
                        }}
                    >
                        {$_("modals.create_order.cancel")}
                    </button>
                    <button
                        type="submit"
                        class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-700"
                    >
                        {$_("modals.create_order.create")}
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
{/if}