<head><title>Inscription</title></head>
<header class="bg-teal-500 h-16 flex items-center">
  <img alt="Logo MD" src="src/lib/assets/logo-blanc.png" class="h-full p-2" />
</header>

<main class="flex justify-center py-12 px-6 sm:px-10">
  <form class="border-3 border-teal-500 rounded-lg w-full sm:w-3/4 lg:w-1/2 xl:w-1/3 p-8 bg-white shadow-md">
    <div class="text-center mb-6">
      <p class="text-teal-500 text-3xl font-bold">Bienvenue sur MDTools</p>
    </div>

    <div class="text-center text-gray-700 mb-6">
      <p>
        Créez un compte <br />
        ou <br />
        connectez-vous <a href="/login" class="text-blue-500 underline">ici</a> si vous en avez déjà un.
      </p>
    </div>

    <div class="space-y-4">
      <label for="email" class="block font-semibold">Adresse e-mail</label>
      <input type="email" placeholder="Entrez votre adresse e-mail" name="email" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="password" class="block font-semibold">Mot de passe</label>
      <input type="password" placeholder="Créez un mot de passe" name="password" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="confirm-password" class="block font-semibold">Confirmation du mot de passe</label>
      <input type="password" placeholder="Confirmez votre mot de passe" name="confirm-password" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="function" class="block font-semibold">Quelle est votre fonction ?</label>
      <input list="functions" name="function" type="text" placeholder="Choisissez votre fonction"
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">
      <datalist id="functions">
        <option value="Je travaille chez MDTools">
        <option value="Je suis médecin">
      </datalist>
      
      <button class="bg-teal-500 text-white py-3 mt-6 rounded-lg w-full hover:bg-teal-600" type="submit">S'inscrire</button>
    </div>
  </form>
</main>
