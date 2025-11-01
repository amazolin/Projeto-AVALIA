/**
 * Script para gerenciar a criação de provas
 * Funcionalidades: Selecionar questões, remover questões, pré-visualizar e criar prova
 */

// Array para armazenar as questões selecionadas
let questoesSelecionadas = [];

// Aguarda o DOM estar carregado
document.addEventListener('DOMContentLoaded', function() {
    inicializarEventos();
    carregarQuestoesSalvas();
});

/**
 * Inicializa todos os event listeners
 */
function inicializarEventos() {
    // Event listener para botões de selecionar questão
    document.addEventListener('click', function(e) {
        // Botão Selecionar
        if (e.target.classList.contains('btn-selecionar-questao') || 
            e.target.closest('.btn-selecionar-questao')) {
            
            const btn = e.target.classList.contains('btn-selecionar-questao') 
                ? e.target 
                : e.target.closest('.btn-selecionar-questao');
            
            adicionarQuestao(btn);
        }
        
        // Botão Remover
        if (e.target.classList.contains('btn-remover-questao') || 
            e.target.closest('.btn-remover-questao')) {
            
            const btn = e.target.classList.contains('btn-remover-questao') 
                ? e.target 
                : e.target.closest('.btn-remover-questao');
            
            removerQuestao(btn.dataset.id);
        }
    });
    
    // Botão Criar Prova
    const btnCriarProva = document.getElementById('btn-criar-prova');
    if (btnCriarProva) {
        btnCriarProva.addEventListener('click', criarProva);
    }
}

/**
 * Adiciona uma questão à lista de selecionadas
 */
function adicionarQuestao(btn) {
    const questaoId = btn.dataset.id;
    const questaoEnunciado = btn.dataset.enunciado;
    const questaoDisciplina = btn.dataset.disciplina;
    
    // Verifica se a questão já foi selecionada
    if (questoesSelecionadas.find(q => q.id === questaoId)) {
        mostrarAlerta('Esta questão já foi selecionada!', 'warning');
        return;
    }
    
    // Adiciona ao array
    questoesSelecionadas.push({
        id: questaoId,
        enunciado: questaoEnunciado,
        disciplina: questaoDisciplina
    });
    
    // Atualiza a interface
    atualizarListaSelecionadas();
    marcarQuestaoComoSelecionada(questaoId);
    salvarQuestoesNoStorage();
    
    // Feedback visual
    mostrarAlerta('Questão adicionada à prova!', 'success');
}

/**
 * Remove uma questão da lista de selecionadas
 */
function removerQuestao(questaoId) {
    // Remove do array
    questoesSelecionadas = questoesSelecionadas.filter(q => q.id !== questaoId);
    
    // Atualiza a interface
    atualizarListaSelecionadas();
    desmarcarQuestao(questaoId);
    salvarQuestoesNoStorage();
    
    // Feedback visual
    mostrarAlerta('Questão removida da prova!', 'info');
}

/**
 * Atualiza a lista visual de questões selecionadas
 */
function atualizarListaSelecionadas() {
    const listaContainer = document.getElementById('lista-selecionadas');
    const contador = document.getElementById('contador-questoes');
    const btnCriarProva = document.getElementById('btn-criar-prova');
    
    // Atualiza contador
    contador.textContent = questoesSelecionadas.length;
    
    // Habilita/desabilita botão criar prova
    if (questoesSelecionadas.length > 0) {
        btnCriarProva.disabled = false;
    } else {
        btnCriarProva.disabled = true;
    }
    
    // Se não há questões, mostra estado vazio
    if (questoesSelecionadas.length === 0) {
        listaContainer.innerHTML = `
            <div class="empty-state text-center py-5">
                <i class="bi bi-clipboard2-x display-4 text-muted mb-3"></i>
                <p class="text-muted">Nenhuma questão selecionada</p>
                <small class="text-muted">
                    Selecione questões ao lado para criar a prova
                </small>
            </div>
        `;
        return;
    }
    
    // Monta a lista de questões
    let html = '';
    questoesSelecionadas.forEach((questao, index) => {
        html += `
            <div class="questao-selecionada" data-questao-id="${questao.id}">
                <div class="questao-selecionada-conteudo">
                    <div class="questao-selecionada-disciplina">
                        ${questao.disciplina}
                    </div>
                    <div class="questao-selecionada-enunciado">
                        ${index + 1}. ${questao.enunciado}
                    </div>
                </div>
                <button type="button" class="btn-remover-questao" data-id="${questao.id}">
                    <i class="bi bi-x"></i> Remover
                </button>
            </div>
        `;
    });
    
    listaContainer.innerHTML = html;
}

/**
 * Marca visualmente uma questão como selecionada
 */
function marcarQuestaoComoSelecionada(questaoId) {
    const questaoItem = document.querySelector(`.questao-item[data-questao-id="${questaoId}"]`);
    if (questaoItem) {
        questaoItem.classList.add('selecionada');
        
        const btnSelecionar = questaoItem.querySelector('.btn-selecionar-questao');
        if (btnSelecionar) {
            btnSelecionar.disabled = true;
            btnSelecionar.innerHTML = '<i class="bi bi-check-circle"></i> Selecionada';
        }
    }
}

/**
 * Desmarca visualmente uma questão
 */
function desmarcarQuestao(questaoId) {
    const questaoItem = document.querySelector(`.questao-item[data-questao-id="${questaoId}"]`);
    if (questaoItem) {
        questaoItem.classList.remove('selecionada');
        
        const btnSelecionar = questaoItem.querySelector('.btn-selecionar-questao');
        if (btnSelecionar) {
            btnSelecionar.disabled = false;
            btnSelecionar.innerHTML = '<i class="bi bi-plus-circle"></i> Selecionar';
        }
    }
}

/**
 * Salva as questões selecionadas no sessionStorage
 */
function salvarQuestoesNoStorage() {
    try {
        sessionStorage.setItem('questoesSelecionadas', JSON.stringify(questoesSelecionadas));
    } catch (e) {
        console.error('Erro ao salvar questões:', e);
    }
}

/**
 * Carrega as questões salvas do sessionStorage (ao recarregar a página)
 */
function carregarQuestoesSalvas() {
    try {
        const questoesSalvas = sessionStorage.getItem('questoesSelecionadas');
        if (questoesSalvas) {
            questoesSelecionadas = JSON.parse(questoesSalvas);
            atualizarListaSelecionadas();
            
            // Marca as questões como selecionadas na lista
            questoesSelecionadas.forEach(q => {
                marcarQuestaoComoSelecionada(q.id);
            });
        }
    } catch (e) {
        console.error('Erro ao carregar questões salvas:', e);
    }
}

/**
 * Limpa as questões selecionadas
 */
function limparSelecao() {
    if (confirm('Deseja realmente limpar todas as questões selecionadas?')) {
        questoesSelecionadas.forEach(q => {
            desmarcarQuestao(q.id);
        });
        
        questoesSelecionadas = [];
        atualizarListaSelecionadas();
        salvarQuestoesNoStorage();
        
        mostrarAlerta('Seleção limpa!', 'info');
    }
}

/**
 * Cria a prova com as questões selecionadas
 */
function criarProva() {
    if (questoesSelecionadas.length === 0) {
        mostrarAlerta('Selecione pelo menos uma questão!', 'warning');
        return;
    }
    
    // Aqui você pode implementar a lógica para criar a prova
    // Por exemplo, abrir um modal para definir nome, data, etc.
    // Ou redirecionar para uma página de configuração da prova
    
    const confirmar = confirm(
        `Criar prova com ${questoesSelecionadas.length} questão(ões)?\n\n` +
        'Esta ação irá para a próxima etapa de configuração da prova.'
    );
    
    if (confirmar) {
        // Salva as questões no sessionStorage para usar na próxima página
        salvarQuestoesNoStorage();
        
        // Redireciona para página de configuração (ajuste a URL conforme necessário)
        window.location.href = '/provas/configurar';
        
        // OU se preferir enviar via POST:
        // enviarProvaParaServidor();
    }
}

/**
 * Envia as questões selecionadas para o servidor (exemplo)
 */
function enviarProvaParaServidor() {
    const questoesIds = questoesSelecionadas.map(q => q.id);
    
    fetch('/api/provas/criar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            questoes: questoesIds
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erro ao criar prova');
        }
        return response.json();
    })
    .then(data => {
        mostrarAlerta('Prova criada com sucesso!', 'success');
        
        // Limpa a seleção
        sessionStorage.removeItem('questoesSelecionadas');
        
        // Redireciona após 2 segundos
        setTimeout(() => {
            window.location.href = '/provas';
        }, 2000);
    })
    .catch(error => {
        console.error('Erro:', error);
        mostrarAlerta('Erro ao criar prova. Tente novamente.', 'danger');
    });
}

/**
 * Mostra um alerta Bootstrap
 */
function mostrarAlerta(mensagem, tipo = 'info') {
    // Remove alertas anteriores
    const alertasAntigos = document.querySelectorAll('.alerta-temporario');
    alertasAntigos.forEach(a => a.remove());
    
    const alerta = document.createElement('div');
    alerta.className = `alert alert-${tipo} alert-dismissible fade show alerta-temporario`;
    alerta.style.position = 'fixed';
    alerta.style.top = '20px';
    alerta.style.right = '20px';
    alerta.style.zIndex = '9999';
    alerta.style.minWidth = '300px';
    alerta.innerHTML = `
        ${mensagem}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alerta);
    
    // Remove automaticamente após 3 segundos
    setTimeout(() => {
        alerta.remove();
    }, 3000);
}

/**
 * Pré-visualiza as questões da prova em um modal (opcional)
 */
function preVisualizarProva() {
    if (questoesSelecionadas.length === 0) {
        mostrarAlerta('Nenhuma questão selecionada!', 'warning');
        return;
    }
    
    let html = '<h5 class="mb-3">Pré-visualização da Prova</h5>';
    html += '<div class="list-group">';
    
    questoesSelecionadas.forEach((questao, index) => {
        html += `
            <div class="list-group-item">
                <h6>${index + 1}. ${questao.disciplina}</h6>
                <p class="mb-0">${questao.enunciado}</p>
            </div>
        `;
    });
    
    html += '</div>';
    
    // Você pode usar um modal Bootstrap para exibir
    // ou criar sua própria implementação
    console.log('Pré-visualização:', questoesSelecionadas);
    
    // Exemplo com modal do Bootstrap (se tiver um modal no HTML)
    // const modalBody = document.querySelector('#modalPreVisualizacao .modal-body');
    // if (modalBody) {
    //     modalBody.innerHTML = html;
    //     const modal = new bootstrap.Modal(document.getElementById('modalPreVisualizacao'));
    //     modal.show();
    // }
}

// Exporta funções para uso global (se necessário)
window.criarProvaUtils = {
    limparSelecao,
    preVisualizarProva,
    getQuestoesSelecionadas: () => questoesSelecionadas
};