<script lang="ts">
    const images: Record<string, {default: string}> =
        import.meta.glob('$lib/assets/groups/*.png', {eager: true});
</script>

<head><title>Homepage</title></head>
<header><img alt="md original logo" src="src/lib/assets/logo-blanc.png" class="md_logo" /></header>
<main class="column_free">
    <aside class="menu_aside column_free column_column">
      <form class="form_class">
        <div class="form_line">
          <div>
            <label for="id_search_keyword">Recherche par mot(s) clé(s):</label>
          </div>
          <div>
            <input name="search_keyword" id="id_search_keyword" class="cadre" placeholder="Entrez un mot clé">
          </div>
        </div>
        <div class="form_line">
          <div>
            <label for="id_search_by_groups">Recherche par groupe ou sous-groupe:</label>
          </div>
          <div>
            <input name="search_by_groups" id="search_by_groups" class="cadre" placeholder="Entrez un groupe">   
          </div>
        </div>
        <div class="form_line">
          <div>
            <label for="id_search_set">Recherche par référence:</label>
          </div>
          <div>
            <input list="ref" name="ref" class="cadre" placeholder="Entrez une référence">
            <datalist id="ref">
              <option value="ref1">
              <option value="ref2">
              <option value="ref3">
              <option value="ref4">
              <option value="ref5">
            </datalist>
          </div>
        </div>
        <div class="form_line">
        <button class="btn submit">Rechercher</button>
        </div>
      </form>
    </aside>
  
    <div class="content scroller">
        {#each Object.values(images) as { default: imageUrl }}
            <img class="image" src={imageUrl} alt="group_img"/>
        {/each}
    </div>
</main>
  
<footer class="set_footer">
    <div class="set">
      <span class="commande">Set/commande</span>
      <label for="id_ref">Rechercher une commande: </label>
    </div>
    <div class="set">
      <input list="commandes" name="commandes" class="cadre" placeholder="Entrez un numéro de commande">
      <datalist id="commandes">
        <option value="#123456">
        <option value="#123457">
        <option value="#123458">
        <option value="#123459">
      </datalist>
    </div>
    <div class="set">
      <button class="btn submit">Rechercher</button>
    </div>
</footer>

<style>

    header {
      background-color: rgb(109, 199, 172);
      height: 70px;
    }
    
    .set_footer {
      bottom: 120px;
      position: fixed;
    }
    .set {
      display: inline-block;
    }

    .commande {
      font-size:1.5em;
      padding-right: 200px;
    }

    .column_free {
      display: flex;
    }

    .column_column {
      flex-direction: column;
    }

    .menu_aside {
      padding-right: 10px;
    }

    .form_line {
      padding-top: 8px;
      padding-bottom: 8px;
    }

    .form_class {
      padding: 6px 10px;
    }

    .md_logo {
      margin-left: 5px;
    }

    .btn {
      border: none;
      color: black;
      padding: 5px 15px;
      font-size: 16px;
      cursor: pointer;
      border-radius: 7px;
    }

    .submit {background-color:   rgb(109, 199, 172);}
    .submit:hover {background-color: #4f74a1;}

    .cadre {
      width: 250px;
      height: 20px;
      border: 1.5px solid black;
    }

    .content {
      display: flex;
      flex-wrap: wrap;
      align-content: space-between;
      justify-content: flex-end;
      margin-bottom: 20px;
      margin-left: auto;
      margin-right: auto;
    }

    .scroller {
      width: 1200px;
      height: 500px;
      overflow-y: scroll;
      scrollbar-width: thin;
    }

    .image {
      width: 200px;
      height: 150px;
      max-width: fit-content;
    }

</style>