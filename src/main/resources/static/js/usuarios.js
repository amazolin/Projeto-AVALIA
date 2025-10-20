// Espera o DOM carregar
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("formEmail");
  const inputEmail = document.getElementById("emailInput");

  if (!form || !inputEmail) {
    console.error("Formulário ou input não encontrados!");
    return;
  }

  form.addEventListener("submit", async function (event) {
    event.preventDefault(); // evita recarregar a página

    const email = inputEmail.value.trim();

    if (!email) {
      alert("Por favor, insira um e-mail válido.");
      return;
    }

    console.log("Enviando e-mail:", email);

    try {
      const response = await fetch("/api/usuarios/cadastrar-email", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ email: email })
      });

      const message = await response.text();

      if (response.ok) {
        alert("✅ " + message);
        form.reset();
      } else {
        alert("⚠️ " + message);
      }

    } catch (error) {
      alert("❌ Erro ao cadastrar e-mail.");
      console.error("Erro no fetch:", error);
    }
  });
});
