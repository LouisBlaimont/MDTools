<head><title>Mot de passe oublié</title></head>
<header class="bg-teal-500 h-16 flex items-center">
  <img alt="Logo MD" src="src/lib/assets/logo-blanc.png" class="h-full p-2" />
</header>

<main class="flex justify-center p-6 sm:p-10">
  <form class="border-3 border-teal-500 rounded-lg w-full sm:w-3/4 lg:w-1/2 xl:w-1/3 p-6">
    <div class="text-center mb-6">
      <img alt="Icône de cadenas" src="src/lib/assets/ouvrir.png" class="w-1/2 sm:w-1/3 mx-auto rounded-full mb-4"/>
      <p class="text-center text-gray-700 text-lg">
        Vous avez oublié votre mot de passe ? Pas de souci ! Entrez simplement votre adresse e-mail ci-dessous, et nous vous enverrons un lien pour le réinitialiser.
      </p>
    </div>

    <div class="space-y-4">
      <label for="email" class="block font-semibold">Adresse e-mail</label>
      <input type="email" placeholder="Entrez votre adresse e-mail" name="email" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <button class="bg-teal-500 text-white py-3 mt-4 rounded-lg w-full hover:bg-teal-600" type="submit">Envoyer le lien de réinitialisation</button>
    </div>

    <div class="flex justify-center mt-4 text-blue-500">
      <a href="/login">Retour à la page de connexion</a>
    </div>
  </form>
</main>
