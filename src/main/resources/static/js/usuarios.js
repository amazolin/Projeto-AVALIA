// Espera o DOM carregar
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("formEmail");
  const inputEmail = document.getElementById("emailInput");
  const lista = document.querySelector(".lista-usuarios");
  const formEditar = document.getElementById("formEditarUsuario");

  // Variável para armazenar o ID do usuário sendo editado
  let usuarioEditandoId = null;

  // Inicializa o Choices.js para disciplinas
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

  // === 1. Carregar disciplinas do banco de dados ===
  async function carregarDisciplinas() {
    try {
      console.log("Carregando disciplinas...");
      const response = await fetch("/api/disciplinas");
      if (!response.ok) throw new Error("Erro ao buscar disciplinas.");
      
      const disciplinas = await response.json();
      console.log("Disciplinas carregadas:", disciplinas);
      
      // Limpa as opções atuais
      selectDisciplinas.innerHTML = "";
      
      // Adiciona as disciplinas do banco
      disciplinas.forEach(disc => {
        const option = document.createElement("option");
        option.value = disc.id;
        option.textContent = disc.nome;
        selectDisciplinas.appendChild(option);
      });
      
      // Reinicializa o Choices.js
      if (window.choicesDisciplinas) {
        window.choicesDisciplinas.destroy();
        window.choicesDisciplinas = new Choices(selectDisciplinas, {
          removeItemButton: true,
          searchEnabled: true,
          placeholderValue: "Selecione as disciplinas",
          maxItemCount: 8,
        });
      }
      
    } catch (error) {
      console.error("Erro ao carregar disciplinas:", error);
      alert("❌ Erro ao carregar disciplinas do banco de dados.");
    }
  }

  // === 2. Função para carregar usuários ===
  async function carregarUsuarios() {
    try {
      const response = await fetch("/api/usuarios");
      if (!response.ok) throw new Error("Erro ao buscar usuários.");
      const usuarios = await response.json();

      lista.innerHTML = ""; // limpa a lista atual

      // Filtra: remove coordenador (id_tipo = 1)
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
          console.log("Editando usuário:", emailUsuario);
          
          try {
            // ✅ ENDPOINT CORRETO: buscar-completo
            const res = await fetch(
              `/api/usuarios/buscar-completo?email=${encodeURIComponent(emailUsuario)}`
            );

            console.log("Status da resposta:", res.status);

            if (!res.ok) {
              const errorText = await res.text();
              console.error("Erro na resposta:", errorText);
              throw new Error("Usuário não encontrado: " + errorText);
            }
            
            const usuario = await res.json();
            console.log("Usuário recebido:", usuario);

            // Armazena o ID do usuário sendo editado
            usuarioEditandoId = usuario.id;

            // Preenche o modal
            document.getElementById("nomeEditar").value = usuario.nome || "";
            document.getElementById("emailEditar").value = usuario.email || "";
            document.getElementById("rgmEditar").value = usuario.rgm || "";
            document.getElementById("senhaEditar").value = "";
            document.getElementById("senhaEditar").placeholder = "Deixe em branco para não alterar";

            // Tipo de usuário
            const tipoId = usuario.tipoUsuario?.id || 2;
            document.getElementById("statusAutenticacao").value = 
              tipoId === 1 ? "coordenador" : "professor";

            // Status
            document.getElementById("statusEditar").value = usuario.status || "Pendente";

            // Disciplinas (Choices.js)
            if (window.choicesDisciplinas) {
              window.choicesDisciplinas.removeActiveItems();
              
              console.log("Disciplinas do usuário:", usuario.disciplinas);
              
              // Marca as disciplinas do usuário
              if (Array.isArray(usuario.disciplinas) && usuario.disciplinas.length > 0) {
                usuario.disciplinas.forEach((discId) => {
                  console.log("Marcando disciplina ID:", discId);
                  window.choicesDisciplinas.setChoiceByValue(discId.toString());
                });
              }
            }
          } catch (error) {
            console.error("Erro ao buscar usuário:", error);
            alert("❌ Não foi possível carregar os dados do usuário.\n" + error.message);
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
                carregarUsuarios();
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

  // === 3. Cadastrar novo e-mail ===
  form.addEventListener("submit", async function (event) {
    event.preventDefault();

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
        carregarUsuarios();
      } else {
        alert("⚠️ " + message);
      }
    } catch (error) {
      alert("❌ Erro ao cadastrar e-mail.");
      console.error("Erro no fetch:", error);
    }
  });

  // === 4. EDITAR USUÁRIO (submit do formulário do modal) ===
  if (formEditar) {
    formEditar.addEventListener("submit", async function (event) {
      event.preventDefault();

      if (!usuarioEditandoId) {
        alert("❌ Erro: ID do usuário não encontrado!");
        return;
      }

      // Coleta os dados do formulário
      const nome = document.getElementById("nomeEditar").value.trim();
      const email = document.getElementById("emailEditar").value.trim();
      const rgm = document.getElementById("rgmEditar").value.trim();
      const senha = document.getElementById("senhaEditar").value.trim();
      const status = document.getElementById("statusEditar").value;

      // Coleta disciplinas selecionadas
      const disciplinasSelecionadas = window.choicesDisciplinas 
        ? window.choicesDisciplinas.getValue(true) 
        : [];

      console.log("Dados para editar:", {
        id: usuarioEditandoId,
        nome,
        email,
        rgm,
        status,
        disciplinas: disciplinasSelecionadas
      });

      // Validações
      if (!nome || !email || !rgm) {
        alert("❌ Preencha todos os campos obrigatórios!");
        return;
      }

      try {
        // Monta o FormData
        const formData = new URLSearchParams();
        formData.append("id", usuarioEditandoId);
        formData.append("nomeCompleto", nome);
        formData.append("email", email);
        formData.append("rgm", rgm);
        formData.append("status", status);
        
        // Adiciona senha apenas se foi preenchida
        if (senha) {
          formData.append("senha", senha);
        }

        // Adiciona disciplinas
        disciplinasSelecionadas.forEach(discId => {
          formData.append("disciplinas", discId);
        });

        console.log("Enviando dados:", formData.toString());

        // Faz a requisição
        const response = await fetch("/api/usuarios/editar", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: formData
        });

        const resultado = await response.text();

        if (response.ok) {
          alert("✅ " + resultado);
          
          // Fecha o modal
          const modal = bootstrap.Modal.getInstance(document.getElementById('editarUsuario'));
          modal.hide();
          
          // Recarrega a lista
          carregarUsuarios();
          
          // Limpa o ID
          usuarioEditandoId = null;
        } else {
          alert("❌ " + resultado);
        }

      } catch (error) {
        console.error("Erro ao editar usuário:", error);
        alert("❌ Erro ao editar usuário. Tente novamente.");
      }
    });
  }

  // === 5. Inicializa ao carregar a página ===
  carregarDisciplinas();
  carregarUsuarios();
});