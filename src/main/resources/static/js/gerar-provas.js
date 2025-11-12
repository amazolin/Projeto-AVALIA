// gerar-provas.js - Versão corrigida com endpoint /provas/api/criar

let questoesSelecionadas = [];

const listaSelecionadas = document.getElementById('lista-selecionadas');
const contadorQuestoes = document.getElementById('contador-questoes');
const btnCriarProva = document.getElementById('btn-criar-prova');

// ==================== INICIALIZAÇÃO ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Sistema de Geração de Provas iniciado');
    
    carregarQuestoesSalvas();
    atualizarInterface();
    inicializarBotoes();
});

function inicializarBotoes() {
    setTimeout(() => {
        const botoes = document.querySelectorAll('.btn-selecionar-questao');
        if (botoes.length > 0) {
            console.log(`✅ ${botoes.length} botões encontrados`);
            configurarBotoesSelecao();
            marcarBotoesJaSelecionados();
        } else {
            console.log('⏳ Nenhum botão encontrado ainda...');
        }
    }, 200);
    
    setTimeout(() => {
        const botoes = document.querySelectorAll('.btn-selecionar-questao');
        if (botoes.length > 0) marcarBotoesJaSelecionados();
    }, 600);
}

// ==================== LOCALSTORAGE ====================

function salvarQuestoesNoStorage() {
    localStorage.setItem('questoesSelecionadas', JSON.stringify(questoesSelecionadas));
}

function carregarQuestoesSalvas() {
    const salvas = localStorage.getItem('questoesSelecionadas');
    if (salvas) questoesSelecionadas = JSON.parse(salvas);
}

function limparQuestoesDoStorage() {
    localStorage.removeItem('questoesSelecionadas');
}

// ==================== CONFIGURAR BOTÕES ====================

function configurarBotoesSelecao() {
    const botoes = document.querySelectorAll('.btn-selecionar-questao');
    botoes.forEach(botao => {
        const novo = botao.cloneNode(true);
        botao.parentNode.replaceChild(novo, botao);
    });

    document.querySelectorAll('.btn-selecionar-questao').forEach(botao => {
        botao.addEventListener('click', e => {
            e.preventDefault();
            const id = parseInt(botao.dataset.id);
            const enunciado = botao.dataset.enunciado;
            const disciplina = botao.dataset.disciplina;

            if (!id || !enunciado) return;

            const questao = {
                id,
                enunciado,
                disciplina: { nome: disciplina || 'Sem disciplina' }
            };
            selecionarQuestao(questao, botao);
        });
    });
}

// ==================== MARCAR BOTÕES ====================

function marcarBotoesJaSelecionados() {
    questoesSelecionadas.forEach(q => {
        const btn = document.querySelector(`.btn-selecionar-questao[data-id="${q.id}"]`);
        if (btn) {
            btn.classList.remove('btn-success');
            btn.classList.add('btn-secondary');
            btn.disabled = true;
            btn.innerHTML = '<i class="bi bi-check-circle"></i> Selecionada';
        }
    });
}

// ==================== SELECIONAR QUESTÃO ====================

function selecionarQuestao(questao, botao) {
    if (questoesSelecionadas.some(q => q.id === questao.id)) {
        mostrarToast('Esta questão já foi selecionada!', 'warning');
        return;
    }

    questoesSelecionadas.push(questao);
    salvarQuestoesNoStorage();

    if (botao) {
        botao.classList.remove('btn-success');
        botao.classList.add('btn-secondary');
        botao.disabled = true;
        botao.innerHTML = '<i class="bi bi-check-circle"></i> Selecionada';
    }

    atualizarInterface();
}

// ==================== REMOVER QUESTÃO ====================

function removerQuestao(questaoId) {
    questoesSelecionadas = questoesSelecionadas.filter(q => q.id !== questaoId);
    salvarQuestoesNoStorage();
    atualizarInterface();

    const btn = document.querySelector(`.btn-selecionar-questao[data-id="${questaoId}"]`);
    if (btn) {
        btn.classList.remove('btn-secondary');
        btn.classList.add('btn-success');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-plus-circle"></i> Selecionar';
    }
}

// ==================== ATUALIZAR INTERFACE ====================

function atualizarInterface() {
    if (contadorQuestoes) contadorQuestoes.textContent = questoesSelecionadas.length;
    if (btnCriarProva) btnCriarProva.disabled = questoesSelecionadas.length === 0;

    if (!listaSelecionadas) return;
    if (questoesSelecionadas.length === 0) {
        listaSelecionadas.innerHTML = `
            <div class="empty-state text-center py-5">
                <i class="bi bi-clipboard2-x display-4 text-muted mb-3"></i>
                <p class="text-muted mb-2">Nenhuma questão selecionada</p>
                <small class="text-muted">Selecione questões ao lado para criar a prova.</small>
            </div>`;
        return;
    }

    listaSelecionadas.innerHTML = questoesSelecionadas.map((q, i) => `
        <div class="card mb-2" style="animation: slideIn 0.3s ease-out;">
            <div class="d-flex justify-content-between align-items-start p-2">
                <div>
                    <small class="fw-semibold text-primary">${q.disciplina?.nome || 'Sem disciplina'}</small><br>
                    <small><span class="badge bg-primary me-1">${i + 1}</span>${q.enunciado.substring(0, 60)}...</small>
                </div>
                <button class="btn btn-sm btn-outline-danger" onclick="removerQuestao(${q.id})">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
        </div>
    `).join('');
}

// ==================== CRIAR PROVA ====================

if (btnCriarProva) btnCriarProva.addEventListener('click', abrirModalCriarProva);

function abrirModalCriarProva() {
    if (questoesSelecionadas.length === 0) {
        mostrarToast('Selecione pelo menos uma questão!', 'warning');
        return;
    }

    const modalHtml = `
        <div class="modal fade" id="modalCriarProva" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title"><i class="bi bi-file-earmark-plus me-2"></i>Criar Nova Prova</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <form id="formCriarProva">
                            <div class="mb-3">
                                <label class="form-label">Título da Prova *</label>
                                <input type="text" class="form-control" id="tituloProva" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Descrição</label>
                                <textarea class="form-control" id="descricaoProva" rows="3"></textarea>
                            </div>
                            <div class="alert alert-info">
                                <i class="bi bi-info-circle me-2"></i>
                                ${questoesSelecionadas.length} questão(ões) serão incluídas.
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-primary" id="btnConfirmarCriarProva">Criar Prova</button>
                    </div>
                </div>
            </div>
        </div>`;
    
    const antigo = document.getElementById('modalCriarProva');
    if (antigo) antigo.remove();
    document.body.insertAdjacentHTML('beforeend', modalHtml);

    const modal = new bootstrap.Modal(document.getElementById('modalCriarProva'));
    modal.show();

    document.getElementById('btnConfirmarCriarProva').addEventListener('click', confirmarCriarProva);
}

async function confirmarCriarProva() {
    const titulo = document.getElementById('tituloProva').value.trim();
    const descricao = document.getElementById('descricaoProva').value.trim();

    if (!titulo) {
        mostrarToast('Digite um título!', 'warning');
        return;
    }

    const btnConfirmar = document.getElementById('btnConfirmarCriarProva');
    btnConfirmar.disabled = true;
    btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Criando...';

    try {
        const dados = { titulo, descricao, questoes: questoesSelecionadas.map(q => q.id) };

        // 🔹 Corrigido o caminho da API
		const response = await fetch('/provas/api/criar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });

        if (!response.ok) throw new Error(await response.text());
        const resultado = await response.json();

        const modal = bootstrap.Modal.getInstance(document.getElementById('modalCriarProva'));
        modal.hide();

        questoesSelecionadas = [];
        limparQuestoesDoStorage();
        atualizarInterface();

        document.querySelectorAll('.btn-selecionar-questao').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-success');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-plus-circle"></i> Selecionar';
        });

        mostrarToast(`Prova "${titulo}" criada com sucesso! Baixando PDF...`, 'success');

        // 🔹 Corrigido o link de download
        setTimeout(() => {
            const link = document.createElement('a');
			link.href = `/provas/api/${resultado.id}/pdf`;
            link.download = `prova_${resultado.id}.pdf`;
            document.body.appendChild(link);
            link.click();
            link.remove();
        }, 500);
    } catch (e) {
        console.error(e);
        mostrarToast('Erro ao criar prova: ' + e.message, 'danger');
        btnConfirmar.disabled = false;
        btnConfirmar.innerHTML = 'Criar Prova';
    }
}

// ==================== TOAST ====================

function mostrarToast(msg, tipo = 'info') {
    const icones = {
        success: 'check-circle-fill',
        danger: 'exclamation-triangle-fill',
        warning: 'exclamation-circle-fill',
        info: 'info-circle-fill'
    };
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${tipo} border-0 position-fixed top-0 start-50 translate-middle-x mt-3"
             role="alert" data-bs-delay="3000">
            <div class="d-flex">
                <div class="toast-body"><i class="bi bi-${icones[tipo]} me-2"></i>${msg}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>`;
    document.body.insertAdjacentHTML('beforeend', toastHtml);
    const toastEl = document.querySelector('.toast:last-child');
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

// ==================== ESTILO ====================

const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { opacity: 0; transform: translateX(-10px); }
        to { opacity: 1; transform: translateX(0); }
    }
`;
document.head.appendChild(style);

window.removerQuestao = removerQuestao;
window.configurarBotoesSelecao = configurarBotoesSelecao;
window.marcarBotoesJaSelecionados = marcarBotoesJaSelecionados;

console.log('✅ Script carregar-provas.js carregado com sucesso');
