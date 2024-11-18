<head><title>Inscription</title></head>
<main class="flex justify-center py-6 px-6 sm:px-10 text-[14px]">
  <form class="border-3 rounded-lg w-full md:w-2/3 lg:w-1/2 p-6 mx-auto bg-gray-100 shadow-lg">
    <div class="text-center mb-4">
      <p class="text-teal-500 text-3xl font-bold">Bienvenue sur MDTools</p>
    </div>

    <div class="text-center mb-5 text-lg">
      <p>
        Créez un compte <br />
        ou <br />
        connectez-vous <a href="/login" class="text-blue-500 underline">ici</a> si vous en avez déjà un.
      </p>
    </div>

    <div class="space-y-3">
      <label for="email" class="block font-semibold text-lg">Adresse e-mail:</label>
      <input type="email" placeholder="Entrez votre adresse e-mail" name="email" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="password" class="block font-semibold text-lg">Mot de passe:</label>
      <input type="password" placeholder="Créez un mot de passe" name="password" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="confirm-password" class="block font-semibold text-lg">Confirmation du mot de passe:</label>
      <input type="password" placeholder="Confirmez votre mot de passe" name="confirm-password" required
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">

      <label for="function" class="block font-semibold text-lg">Quelle est votre fonction ?</label>
      <input list="functions" name="function" type="text" placeholder="Choisissez votre fonction"
             class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">
      <datalist id="functions">
        <option value="Je travaille chez MDTools">
        <option value="Je suis médecin">
      </datalist>
      
      <button class="bg-teal-500 text-white py-3 mt-6 rounded-lg w-full hover:bg-teal-600 text-lg" type="submit">S'inscrire</button>
    </div>
  </form>
</main>
