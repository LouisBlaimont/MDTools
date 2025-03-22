<script>
    import { goto } from "$app/navigation";
    import { user } from "$lib/stores/user_stores";

    let email = $user?.email;
    let name = $user?.name;
    let active = $user?.active;
    active = "Oui";
    console.log("user", $user);
    console.log("email", email);
    console.log("name", name);
    console.log("active", active);

    function goBack() {
        goto("/searches");
    }

    function addUser() {
        goto("/users/add_user");
    }

    function editProfile() {
        goto(`/users/edit/${user.name}`);
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <div class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-4">Profil de l'utilisateur</h2>
        
        <p class="text-lg"><strong>Nom:</strong> {name}</p>
        <p class="text-lg"><strong>Email:</strong> {email}</p>
        <p class="text-lg"><strong>Compte actif:</strong> {active ? "Oui" : "Non"}</p>

        <div class="flex gap-4 mt-6">
            <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={goBack}>Retour</button>
            <button class="bg-green-500 text-white p-2 rounded hover:bg-green-700" on:click={addUser}>Ajouter un utilisateur</button>
            <button class="bg-yellow-500 text-white p-2 rounded hover:bg-yellow-700" on:click={editProfile}>Modifier le profil</button>
        </div>
    </div>
</main>
