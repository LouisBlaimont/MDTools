<head><title>Sign up</title></head>
<header class ="header_bar"><img alt="md original logo" src="src/lib/assets/logo-blanc.png" class="md_logo" /></header>
<main>
  <form class="sign_form"> 
    <div class="bienvue">
        <p>Bienvenue sur MDTools</p>
    </div>
    <div class="txt">
        <span> 
            Crée un compte <br />
            ou <br />
            connecte toi&nbsp<a href="src/routes/login/+page.svelte">ici</a>&nbspsi tu en as déjà un. <br />
        </span>
    </div>
   
    <div class="container">
      <label for="email"><b>Email</b></label>
      <input type="text" placeholder="Entrez un email" name="email" required>

      <label for="mdp"><b>Mot de passe</b></label>
      <input type="text" placeholder="Entrez votre mot de passe" name="mdp" required>

      <label for="mdp_2"><b>Confirmation du mot de passe</b></label>
      <input type="text" placeholder="Entrez de nouveau votre mot de passe" name="mdp_2" required>
      
      <div>
        <label for="function"><b>Quelle est votre fonction?</b></label>
        <input list="functions" name="function" type="text" placeholder="Choisissez votre fonction">
        <datalist id="functions">
          <option value="Je travail chez MDTools">
          <option value="Je suis médecin">
        </datalist>
      </div>

      <button class="btn" type="submit">Sign up</button>
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
    padding-top: 50px;
  }

  .sign_form {
    border: 3px solid rgb(109, 199, 172);
    width: 35%;
    border-radius: 10px; 
  }

  input[type=text] {
    width: 100%;
    padding: 12px 20px;
    margin: 9px 0;
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

  .container {
    padding: 18px;
  }

  .txt {
    text-align: center;
    margin-bottom: 2px;
  }

  .bienvue {
    color:rgb(109, 199, 172);
    font-size: 2.6em;
    text-align: center;
    margin-bottom: -20px;
  }

</style>