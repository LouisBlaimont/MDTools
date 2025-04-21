<script>
    export let message = "Chargement...";
  </script>
  
  <div class="flex flex-col items-center justify-center h-96 w-full">
    <div class="loader ease-linear rounded-full border-8 border-t-8 border-gray-200 h-24 w-24 mb-4"></div>
    <p class="mt-4 text-gray-700 text-lg">{message}</p>
  </div>
  
  <style>
    .loader {
      border-top-color: #4299e1;
      animation: spin 1.2s linear infinite;
    }
  
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  </style>
  