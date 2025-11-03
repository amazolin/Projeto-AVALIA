// gerar-provas.js - Versão com Persistência para Provas Multidisciplinares

let questoesSelecionadas = [];

const listaSelecionadas = document.getElementById('lista-selecionadas');
const contadorQuestoes = document.getElementById('contador-questoes');
const btnCriarProva = document.getElementById('btn-criar-prova');

// ==================== INICIALIZAÇÃO ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Sistema de Geração de Provas iniciado');
    
    // Carrega questões salvas no localStorage
    carregarQuestoesSalvas();
    
    // Atualiza a interface imediatamente (painel direito)
    atualizarInterface();
    
    // Aguarda os botões carregarem e configura
    inicializarBotoes();
});

function inicializarBotoes() {
    // Tenta configurar após pequeno delay
    setTimeout(() => {
        const botoes = document.querySelectorAll('.btn-selecionar-questao');
        if (botoes.length > 0) {
            console.log(`✅ ${botoes.length} botões encontrados, configurando...`);
            configurarBotoesSelecao();
            marcarBotoesJaSelecionados();
        } else {
            console.log('⏳ Nenhum botão encontrado ainda...');
        }
    }, 200);
    
    // Tenta novamente após 600ms (garantia extra)
    setTimeout(() => {
        const botoes = document.querySelectorAll('.btn-selecionar-questao');
        if (botoes.length > 0) {
            console.log('🔄 Verificação adicional dos botões');
            marcarBotoesJaSelecionados();
        }
    }, 600);
}

// ==================== LOCALSTORAGE ====================

function salvarQuestoesNoStorage() {
    try {
        localStorage.setItem('questoesSelecionadas', JSON.stringify(questoesSelecionadas));
        console.log('💾 Questões salvas no localStorage');
    } catch (error) {
        console.error('❌ Erro ao salvar no localStorage:', error);
    }
}

function carregarQuestoesSalvas() {
    try {
        const salvas = localStorage.getItem('questoesSelecionadas');
        if (salvas) {
            questoesSelecionadas = JSON.parse(salvas);
            console.log(`📥 ${questoesSelecionadas.length} questões carregadas do localStorage`);
        }
    } catch (error) {
        console.error('❌ Erro ao carregar do localStorage:', error);
        questoesSelecionadas = [];
    }
}

function limparQuestoesDoStorage() {
    try {
        localStorage.removeItem('questoesSelecionadas');
        console.log('🗑️ Questões removidas do localStorage');
    } catch (error) {
        console.error('❌ Erro ao limpar localStorage:', error);
    }
}

// ==================== CONFIGURAR BOTÕES DE SELEÇÃO ====================

function configurarBotoesSelecao() {
    const botoes = document.querySelectorAll('.btn-selecionar-questao');
    
    console.log(`🔍 Botões encontrados: ${botoes.length}`);
    
    if (botoes.length === 0) {
        console.warn('⚠️ Nenhum botão encontrado');
        return;
    }
    
    // Remove listeners antigos
    botoes.forEach((botao) => {
        const novoBotao = botao.cloneNode(true);
        botao.parentNode.replaceChild(novoBotao, botao);
    });
    
    // Adiciona novos listeners
    const botoesAtualizados = document.querySelectorAll('.btn-selecionar-questao');
    
    botoesAtualizados.forEach((botao, index) => {
        const questaoId = parseInt(botao.getAttribute('data-id'));
        const enunciado = botao.getAttribute('data-enunciado');
        const disciplina = botao.getAttribute('data-disciplina');
        
        console.log(`Botão ${index + 1}:`, { 
            id: questaoId, 
            enunciado: enunciado?.substring(0, 30) + '...' 
        });
        
        botao.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            
            console.log('🖱️ Clique detectado!');
            
            const id = parseInt(this.getAttribute('data-id'));
            const enunc = this.getAttribute('data-enunciado');
            const disc = this.getAttribute('data-disciplina');
            
            if (!id || !enunc) {
                console.error('❌ Dados inválidos');
                mostrarToast('Erro ao capturar dados da questão!', 'danger');
                return;
            }
            
            const questao = {
                id: id,
                enunciado: enunc,
                disciplina: { nome: disc || 'Sem disciplina' }
            };
            
            selecionarQuestao(questao, this);
        });
    });
    
    console.log(`✅ ${botoesAtualizados.length} botões configurados`);
}

// ==================== MARCAR BOTÕES JÁ SELECIONADOS ====================

function marcarBotoesJaSelecionados() {
    if (questoesSelecionadas.length === 0) return;
    
    console.log(`🔍 Marcando ${questoesSelecionadas.length} questões já selecionadas`);
    
    questoesSelecionadas.forEach(questao => {
        const btn = document.querySelector(`.btn-selecionar-questao[data-id="${questao.id}"]`);
        if (btn) {
            btn.classList.remove('btn-success');
            btn.classList.add('btn-secondary');
            btn.disabled = true;
            btn.innerHTML = '<i class="bi bi-check-circle"></i> Selecionada';
            console.log(`✅ Botão da questão ${questao.id} marcado como selecionado`);
        }
    });
}

// ==================== SELECIONAR QUESTÃO ====================

function selecionarQuestao(questao, botao) {
    console.log('🎯 Selecionando questão:', questao.id);
    
    // Verifica se já está selecionada
    if (questoesSelecionadas.some(q => q.id === questao.id)) {
        console.log('⚠️ Questão já selecionada');
        mostrarToast('Esta questão já foi selecionada!', 'warning');
        return;
    }
    
    // Adiciona à lista
    questoesSelecionadas.push(questao);
    console.log('✅ Total de questões:', questoesSelecionadas.length);
    
    // Salva no localStorage
    salvarQuestoesNoStorage();
    
    // Atualiza o botão
    if (botao) {
        botao.classList.remove('btn-success');
        botao.classList.add('btn-secondary');
        botao.disabled = true;
        botao.innerHTML = '<i class="bi bi-check-circle"></i> Selecionada';
    }
    
    // Atualiza a interface
    atualizarInterface();
    
    console.log(`✅ Questão ${questao.id} adicionada com sucesso`);
}

// ==================== REMOVER QUESTÃO ====================

function removerQuestao(questaoId) {
    console.log('🗑️ Removendo questão:', questaoId);
    
    const tamanhoAntes = questoesSelecionadas.length;
    questoesSelecionadas = questoesSelecionadas.filter(q => q.id !== questaoId);
    
    if (questoesSelecionadas.length < tamanhoAntes) {
        console.log('✅ Questão removida. Total:', questoesSelecionadas.length);
        
        // Salva no localStorage
        salvarQuestoesNoStorage();
        
        atualizarInterface();
        
        // Atualiza o botão (se estiver visível)
        const btn = document.querySelector(`.btn-selecionar-questao[data-id="${questaoId}"]`);
        if (btn) {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-success');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-plus-circle"></i> Selecionar';
        }
    } else {
        console.warn('⚠️ Questão não encontrada na lista');
    }
}

// ==================== ATUALIZAR INTERFACE ====================

function atualizarInterface() {
    console.log('🔄 Atualizando interface. Questões:', questoesSelecionadas.length);
    
    // Atualiza contador
    if (contadorQuestoes) {
        contadorQuestoes.textContent = questoesSelecionadas.length;
    }
    
    // Habilita/desabilita botão criar prova
    if (btnCriarProva) {
        btnCriarProva.disabled = questoesSelecionadas.length === 0;
    }
    
    // Renderiza lista
    if (!listaSelecionadas) {
        console.error('❌ Elemento lista-selecionadas não encontrado!');
        return;
    }
    
    if (questoesSelecionadas.length === 0) {
        listaSelecionadas.innerHTML = `
            <div class="empty-state text-center py-5">
                <i class="bi bi-clipboard2-x display-4 text-muted mb-3"></i>
                <p class="text-muted mb-2">Nenhuma questão selecionada</p>
                <small class="text-muted">Selecione questões ao lado para criar a prova.</small>
            </div>
        `;
    } else {
        let html = '';
        questoesSelecionadas.forEach((questao, index) => {
            const disciplinaNome = questao.disciplina?.nome || 'Sem disciplina';
            const enunciadoCurto = questao.enunciado.length > 60 
                ? questao.enunciado.substring(0, 60) + '...' 
                : questao.enunciado;
            
            html += `
                <div class="card mb-2" style="animation: slideIn 0.3s ease-out; background-color: #f8f9fa; border: 1px solid #dee2e6;">
                    <div style="padding: 8px; display: flex; align-items: flex-start; justify-content: space-between; gap: 8px;">
                        <div style="flex: 1; min-width: 0; overflow: hidden;">
                            <small style="display: block; margin-bottom: 4px; font-weight: 600; color: #6a11cb;">
                                ${disciplinaNome}
                            </small>
                            <small style="display: block; color: #495057;" title="${questao.enunciado}">
                                <span class="badge bg-primary me-1">${index + 1}</span>
                                ${enunciadoCurto}
                            </small>
                        </div>
                        <button type="button" 
                                class="btn btn-sm btn-outline-danger"
                                onclick="removerQuestao(${questao.id})"
                                title="Remover questão"
                                style="width: 32px; height: 32px; min-width: 32px; max-width: 32px; padding: 0; display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin: 0;">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                </div>
            `;
        });
        listaSelecionadas.innerHTML = html;
    }
}

// ==================== CRIAR PROVA ====================

if (btnCriarProva) {
    btnCriarProva.addEventListener('click', abrirModalCriarProva);
}

function abrirModalCriarProva() {
    console.log('📋 Abrindo modal para criar prova');
    
    if (questoesSelecionadas.length === 0) {
        mostrarToast('Selecione pelo menos uma questão!', 'warning');
        return;
    }
    
    const modalHtml = `
        <div class="modal fade" id="modalCriarProva" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">
                            <i class="bi bi-file-earmark-plus me-2"></i>Criar Nova Prova
                        </h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <form id="formCriarProva">
                            <div class="mb-3">
                                <label for="tituloProva" class="form-label">
                                    Título da Prova <span class="text-danger">*</span>
                                </label>
                                <input type="text" class="form-control" id="tituloProva" 
                                       placeholder="Ex: Prova de MySQL - 1º Bimestre" required>
                            </div>
                            <div class="mb-3">
                                <label for="descricaoProva" class="form-label">Descrição (opcional)</label>
                                <textarea class="form-control" id="descricaoProva" rows="3"
                                          placeholder="Adicione instruções ou observações sobre a prova"></textarea>
                            </div>
                            <div class="alert alert-info mb-0">
                                <i class="bi bi-info-circle me-2"></i>
                                <strong>${questoesSelecionadas.length}</strong> 
                                ${questoesSelecionadas.length === 1 ? 'questão será incluída' : 'questões serão incluídas'} nesta prova.
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                            <i class="bi bi-x-lg me-1"></i>Cancelar
                        </button>
                        <button type="button" class="btn btn-primary" id="btnConfirmarCriarProva">
                            <i class="bi bi-check-lg me-1"></i>Criar Prova
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    // Remove modal anterior
    const modalExistente = document.getElementById('modalCriarProva');
    if (modalExistente) {
        modalExistente.remove();
    }
    
    // Adiciona novo modal
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Mostra o modal
    const modalElement = document.getElementById('modalCriarProva');
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
    
    // Foca no campo título
    modalElement.addEventListener('shown.bs.modal', function () {
        document.getElementById('tituloProva').focus();
    });
    
    // Event listener para confirmar
    document.getElementById('btnConfirmarCriarProva').addEventListener('click', confirmarCriarProva);
}

async function confirmarCriarProva() {
    const titulo = document.getElementById('tituloProva').value.trim();
    const descricao = document.getElementById('descricaoProva').value.trim();
    
    if (!titulo) {
        mostrarToast('Digite um título para a prova!', 'warning');
        document.getElementById('tituloProva').focus();
        return;
    }
    
    const btnConfirmar = document.getElementById('btnConfirmarCriarProva');
    btnConfirmar.disabled = true;
    btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Criando...';
    
    try {
        const dados = {
            titulo: titulo,
            descricao: descricao,
            questoes: questoesSelecionadas.map(q => q.id)
        };
        
        console.log('📤 Enviando dados:', dados);
        
        const response = await fetch('/api/criar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dados)
        });
        
        if (!response.ok) {
            const erro = await response.text();
            throw new Error(erro);
        }
        
        const resultado = await response.json();
        console.log('✅ Prova criada:', resultado);
        
        // Fecha o modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('modalCriarProva'));
        modal.hide();
        
        // Limpa seleções
        questoesSelecionadas = [];
        limparQuestoesDoStorage();
        atualizarInterface();
        
        // Reseta todos os botões
        document.querySelectorAll('.btn-selecionar-questao').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-success');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-plus-circle"></i> Selecionar';
        });
        
        // Mensagem de sucesso
        mostrarToast(`Prova "${titulo}" criada com sucesso! Baixando PDF...`, 'success');
        
        // ✅ BAIXA O PDF AUTOMATICAMENTE
        console.log('📄 Iniciando download do PDF ID:', resultado.id);
        setTimeout(() => {
            const linkDownload = document.createElement('a');
            linkDownload.href = `/api/prova/${resultado.id}/pdf`;
            linkDownload.download = `prova_${resultado.id}.pdf`;
            document.body.appendChild(linkDownload);
            linkDownload.click();
            document.body.removeChild(linkDownload);
            console.log('✅ Download iniciado!');
        }, 500);
        
    } catch (error) {
        console.error('❌ Erro ao criar prova:', error);
        mostrarToast('Erro ao criar prova: ' + error.message, 'danger');
        btnConfirmar.disabled = false;
        btnConfirmar.innerHTML = '<i class="bi bi-check-lg me-1"></i>Criar Prova';
    }
}

// ==================== FUNÇÕES AUXILIARES ====================

function mostrarToast(mensagem, tipo = 'info') {
    const icones = {
        success: 'check-circle-fill',
        danger: 'exclamation-triangle-fill',
        warning: 'exclamation-circle-fill',
        info: 'info-circle-fill'
    };
    
    const toastHtml = `
        <div class="toast align-items-center text-white bg-${tipo} border-0 position-fixed top-0 start-50 translate-middle-x mt-3" 
             role="alert" style="z-index: 9999;" data-bs-autohide="true" data-bs-delay="3000">
            <div class="d-flex">
                <div class="toast-body">
                    <i class="bi bi-${icones[tipo]} me-2"></i>
                    ${mensagem}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.querySelector('.toast:last-child');
    const toast = new bootstrap.Toast(toastElement);
    toast.show();
    
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

// Adiciona estilo para animação
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            opacity: 0;
            transform: translateX(-10px);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }
`;
document.head.appendChild(style);

// Torna funções acessíveis globalmente
window.removerQuestao = removerQuestao;
window.configurarBotoesSelecao = configurarBotoesSelecao;
window.marcarBotoesJaSelecionados = marcarBotoesJaSelecionados;

console.log('✅ Script carregado completamente');