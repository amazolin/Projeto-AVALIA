// Espera o DOM carregar
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("formEmail");
  const inputEmail = document.getElementById("emailInput");
  const lista = document.querySelector(".lista-usuarios");

  const selectDisciplinas = document.querySelector("#disciplinas");
  if (selectDisciplinas) {
    window.choicesDisciplinas = new Choices(selectDisciplinas, {
      removeItemButton: true,
      searchEnabled: true,
      placeholderValue: "Selecione as disciplinas",
      maxItemCount: 8,
    });
  }

  if (!form || !inputEmail || !lista) {
    console.error("Elemento(s) do DOM não encontrados!");
    return;
  }

  // === 1. Função para carregar usuários ===
  async function carregarUsuarios() {
    try {
      const response = await fetch("/api/usuarios");
      if (!response.ok) throw new Error("Erro ao buscar usuários.");
      const usuarios = await response.json();

      lista.innerHTML = ""; // limpa a lista atual

      // 🔥 Filtra: remove coordenador (id_tipo = 1)
      const usuariosFiltrados = usuarios.filter(
        (user) => user.tipoUsuario?.id !== 1
      );

      usuariosFiltrados.forEach((user) => {
        const statusClass =
          user.status === "Concluído" ? "status-concluido" : "status-pendente";

        const item = document.createElement("div");
        item.classList.add("usuario-item");
        item.innerHTML = `
          <span class="email-usuario">${user.email}</span>
          <span class="${statusClass}">${user.status || "Pendente"}</span>
          <div class="botoes-acao">
            <button type="button" class="btn-editar" data-email="${user.email}" data-bs-toggle="modal" data-bs-target="#editarUsuario">
              <i class="bi bi-pencil-fill"></i>
            </button>
            <button class="btn-excluir" data-email="${user.email}">
              <i class="bi bi-trash-fill"></i>
            </button>
          </div>
        `;
        lista.appendChild(item);
      });

      // === Botões de edição ===
      document.querySelectorAll(".btn-editar").forEach((btn) => {
        btn.addEventListener("click", async function () {
          const emailUsuario = this.getAttribute("data-email");
          try {
            // ✅ Novo endpoint corrigido
            const res = await fetch(
              `/api/usuarios/buscar?email=${encodeURIComponent(emailUsuario)}`
            );

            if (!res.ok) throw new Error("Usuário não encontrado.");
            const usuario = await res.json();

            // Preenche o modal com segurança
            document.getElementById("nomeEditar").value = usuario.nome || "";
            document.getElementById("emailEditar").value = usuario.email || "";
            document.getElementById("rgmEditar").value = usuario.rgm || "";
            document.getElementById("senhaEditar").value = ""; // não mostramos a senha

            // Tipo de usuário
            const tipo = usuario.tipoUsuario?.descricao?.toLowerCase() || "";
            document.getElementById("statusAutenticacao").value =
              tipo.includes("coordenador") ? "coordenador" : "professor";

            // Status
            document.getElementById("statusEditar").value = usuario.status || "";

            // Disciplinas (Choices.js)
            if (window.choicesDisciplinas) {
              window.choicesDisciplinas.removeActiveItems();
              if (Array.isArray(usuario.disciplinas)) {
                usuario.disciplinas.forEach((disc) => {
                  window.choicesDisciplinas.setChoiceByValue(disc);
                });
              }
            }
          } catch (error) {
            console.error("Erro ao buscar usuário:", error);
            alert("❌ Não foi possível carregar os dados do usuário.");
          }
        });
      });

      // === Botões de exclusão ===
      document.querySelectorAll(".btn-excluir").forEach((btn) => {
        btn.addEventListener("click", async (e) => {
          const email = e.currentTarget.getAttribute("data-email");
          if (confirm(`Excluir o usuário ${email}?`)) {
            try {
              const del = await fetch("/api/usuarios/apagar-email", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ email }),
              });

              const msg = await del.text();

              if (del.ok) {
                alert("✅ " + msg);
                carregarUsuarios(); // atualiza lista
              } else {
                alert("⚠️ " + msg);
              }
            } catch (error) {
              alert("❌ Erro ao excluir usuário.");
              console.error("Erro ao excluir:", error);
            }
          }
        });
      });
    } catch (err) {
      console.error("Erro ao carregar usuários:", err);
    }
  }

  // === 2. Cadastrar novo e-mail ===
  form.addEventListener("submit", async function (event) {
    event.preventDefault(); // evita reload

    const email = inputEmail.value.trim();

    if (!email) {
      alert("Por favor, insira um e-mail válido.");
      return;
    }

    try {
      const response = await fetch("/api/usuarios/cadastrar-email", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ email }),
      });

      const message = await response.text();

      if (response.ok) {
        alert("✅ " + message);
        form.reset();
        carregarUsuarios(); // atualiza lista após cadastro
      } else {
        alert("⚠️ " + message);
      }
    } catch (error) {
      alert("❌ Erro ao cadastrar e-mail.");
      console.error("Erro no fetch:", error);
    }
  });

  // === 3. Carregar usuários ao abrir a página ===
  carregarUsuarios();
});
