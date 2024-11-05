<head><title>Login</title></head>
<header class="bg-teal-500 h-16 flex items-center">
  <img alt="md original logo" src="src/lib/assets/logo-blanc.png" class="h-full p-2" />
</header>

<main class="flex flex-col md:flex-row justify-center w-11/12 lg:w-3/4 mx-auto mt-6 p-6">
  <div class="flex w-full md:w-1/3 lg:w-1/4 mb-6 md:mb-0 justify-center items-center border-3 rounded-lg bg-teal-500 shadow-lg shadow-teal-500">
    <form class="flex flex-col items-center w-full p-6">
      <h1 class="text-2xl text-center mb-2 text-white">Bienvenue sur MDTools</h1>
      <span class="text-center text-white">Pas encore de compte?</span>
      <button class="mt-4 bg-white text-teal-500 py-2 px-4 rounded-lg w-3/4 lg:w-4/5" type="submit">Crée un compte ici</button>
    </form>
  </div>

  <form class="border-3 rounded-lg w-full md:w-2/3 lg:w-1/2 p-6 mx-auto bg-white shadow-lg">
    <div class="text-center mb-4">
      <img alt="The project logo" src="src/lib/assets/image0.webp" class="w-1/2 md:w-1/3 lg:w-1/4 rounded-full mx-auto"/>
    </div>

    <div class="space-y-4">
      <label for="email" class="block font-bold">Email</label>
      <input type="text" placeholder="Entrez un email" name="email" required
             class="w-full p-3 border border-gray-300 rounded-lg">

      <label for="mdp" class="block font-bold">Mot de passe</label>
      <input type="password" placeholder="Entrez un mot de passe" name="mdp" required
             class="w-full p-3 border border-gray-300 rounded-lg">

      <button class="bg-teal-500 text-white py-3 mt-4 rounded-lg w-full hover:bg-blue-700" type="submit">Login</button>

      <label class="flex items-center mt-2">
        <input type="checkbox" name="remember" class="mr-2"> Se souvenir de moi
      </label>

      <span class="float-right text-blue-500"><a href="/psw_forgot">Mot de passe oublié?</a></span>
    </div>
  </form>
</main>