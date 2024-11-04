<head><title>Password forgot</title></head>
<header class ="header_bar"><img alt="md original logo" src="src/lib/assets/logo-blanc.png" class="md_logo" /></header>
<main>
  <form class="login_form"> 
    <div class="imgcontainer">
      <img alt="cadenas" src="src/lib/assets/ouvrir.png" class="cadenas"/>
    <span class="txt"> Entrez votre adresse email et nous vous enverrons un lien pour récupérer votre compte.</span>
    </div>
   
    <div class="container">
      <label for="email"><b>Email</b></label>
      <input type="text" placeholder="Entrez un email" name="email" required>
      <button class="btn" type="submit">Envoyer un lien</button>
      <span class="rtn"><a href="src/routes/login/+page.svelte">Retourner sur la page de connexion</a></span>
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

  main {
    font-family: Arial, Helvetica, sans-serif;
    display: flex;
    justify-content: center;
    padding: 100px;
  }

  .login_form {
    border: 3px solid rgb(109, 199, 172);
    width: 30%;
    border-radius: 10px; 
  }

  input[type=text] {
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

  .imgcontainer {
    text-align: center;
    margin: 24px 0 12px 0;
  }

  .cadenas {
    width: 40%;
    border-radius: 50%;
    width: 200px;
  }

  .container {
    padding: 16px;
  }

  .txt {
    display: flex;
    align-content: center;
    padding-top: 30px;
  }

  .rtn {
    display: flex;
    justify-content: center;
    padding-top: 10px;
  }
</style>