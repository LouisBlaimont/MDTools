<head><title>Login</title></head>
<header class ="header_bar"><img alt="md original logo" src="src/lib/assets/logo-blanc.png" class="md_logo" /></header>
<main>
  <div class="column_free">
    <form class="sign_form">
      <div class="sign_text">
        <h1>Bienvenue sur MDTools</h1>
        <span>Pas encore de compte?</span>
        <button class="sign_btn" type="submit">Crée un compte ici</button>
      </div>
    </form>
  </div>
  
  <form class="login_form"> 
    <div class="imgcontainer">
      <img alt="The project logo" src="src/lib/assets/image0.webp" class="project_logo"/>
    </div>

    <div class="container">
      <label for="email"><b>Email</b></label>
      <input type="text" placeholder="Entrez un email" name="email" required>
      
      <label for="mdp"><b>Mot de passe</b></label>
      <input type="password" placeholder="Entrez un mot de passe" name="mdp" required>
      
      <button class="btn" type="submit">Login</button>
      <label>
        <input type="checkbox" name="remember">
        Se souvenir de moi
      </label>

      <span class="mdp"><a href="src/routes/psw_forgot/+page.svelte">Mot de passe oublié?</a></span>
    </div>
  </form>
</main>

<style>  
  .header_bar {
    background-color: rgb(109, 199, 172);
    height: 70px;
  }

  .md_logo {
    margin-left: 5px;
    background-color: rgb(109, 199, 172);
    height: 70px;
  }

  .column_free {
    display: flex;
  }

  main {
    font-family: Arial, Helvetica, sans-serif;
    display: flex;
    justify-content: center;
    width: 70%;
    margin-top: 25px;
    margin-left: auto;
    margin-right: auto;
    padding: 30px;
  }

  .sign_text {
    font-size: 1.5em;
    font-family: Comic sans ms, cursive;
    color: white;
    margin-left: auto;
    margin-right: auto;
    text-align: center;
  }

  .login_form {
    border: 3px solid rgb(109, 199, 172);
    width: 50%;
    border-radius: 10px;
    margin-right: auto;
  }

  .sign_form {
    border: 3px solid rgb(109, 199, 172);
    width: 90%;
    margin-left: auto;
    border-radius: 10px;
    background-color:  rgb(109, 199, 172);
    display: flex;
    align-items: center;
  }

  input[type=text], input[type=password] {
    width: 100%;
    padding: 12px 20px;
    margin: 8px 0;
    display: inline-block;
    border: 1px solid #ccc;
    box-sizing: border-box;
    border-radius: 20px;
  }

  .btn {
    background-color:rgb(109, 199, 172);
    color: white;
    padding: 14px 20px;
    margin: 8px 0;
    border: none;
    cursor: pointer;
    width: 100%;
    border-radius: 20px;
  }

  button:hover {
    background-color: #4f74a1;
  }

  .sign_btn {
    background-color:white;
    color: rgb(109, 199, 172);
    padding: 14px 20px;
    margin: 10px 0;
    border: none;
    cursor: pointer;
    width: 70%;
    border-radius: 20px;
  }

  .imgcontainer {
    text-align: center;
    margin: 24px 0 12px 0;
  }

  .project_logo {
    width: 40%;
    border-radius: 50%;
  }

  .container {
    padding: 16px;
  }

  .mdp {
    float: right;
  }
</style>