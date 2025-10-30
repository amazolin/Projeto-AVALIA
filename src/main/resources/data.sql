
-- Desabilita verificação de chaves estrangeiras temporariamente
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- CLÁUSULAS DE LIMPEZA 
-- -----------------------------------------------------
DROP TABLE IF EXISTS ranking;
DROP TABLE IF EXISTS simulados_questoes;
DROP TABLE IF EXISTS simulados;
DROP TABLE IF EXISTS provas_questoes;
DROP TABLE IF EXISTS provas;
DROP TABLE IF EXISTS opcoes_questao;
DROP TABLE IF EXISTS questoes;
DROP TABLE IF EXISTS disciplinas;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS tipos_usuario;


-- -----------------------------------------------------
-- Tabela de Tipos de Usuário (para Coordenador e Professor)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS tipos_usuario (
    id_tipo BIGINT PRIMARY KEY,
    descricao VARCHAR(50) NOT NULL
);

-- Insere os tipos de usuário (Coordenador e Professor)
INSERT INTO tipos_usuario (id_tipo, descricao) VALUES
(1, 'Coordenador'),
(2, 'Professor');

-- -----------------------------------------------------
-- Tabela de Usuários (Coordenadores e Professores)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    rgm VARCHAR(50),
    status VARCHAR(50),
    frase_seguranca VARCHAR(255),
    id_tipo BIGINT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_modificacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_tipo) REFERENCES tipos_usuario (id_tipo)
);

-- -----------------------------------------------------
-- Inserção de dados iniciais - Usuários
-- -----------------------------------------------------
-- Inserção de ID explícito seguido de reset do AUTO_INCREMENT para evitar problemas futuros
INSERT INTO usuarios (id_usuario, nome, email, senha, rgm, status, id_tipo) VALUES
(1, 'Jadir', 'jadir@fatec.sp.br', '1234', '000000', 'ativo', 1);
-- Garante que o próximo ID gerado automaticamente será 2
ALTER TABLE usuarios AUTO_INCREMENT = 2;


-- -----------------------------------------------------
-- Tabela de Disciplinas
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS disciplinas (
    id_disciplina BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_disciplina VARCHAR(255) NOT NULL UNIQUE,
    id_criador BIGINT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_criador) REFERENCES usuarios (id_usuario)
);

-- -----------------------------------------------------
-- Inserção de dados iniciais - Disciplinas
-- -----------------------------------------------------
INSERT INTO disciplinas (nome_disciplina, id_criador) VALUES
('Banco de Dados em MySQL', 1),
('Linguagem de Programação C', 1),
('Linguagem de Programação C#', 1),
('Estrutura de Dados em Java', 1),
('Programação Orientada a Objetos em Java', 1),
('Engenharia de Software', 1),
('Arquitetura e Organização de Computadores', 1),
('Redes', 1);


-- -----------------------------------------------------
-- Tabela de Questões
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS questoes (
    id_questao BIGINT AUTO_INCREMENT PRIMARY KEY,
    texto_questao TEXT NOT NULL,
    tipo_questao ENUM('multipla_escolha', 'verdadeiro_falso') DEFAULT 'multipla_escolha',
    id_disciplina BIGINT NOT NULL,
    id_criador BIGINT NOT NULL,
    id_editor BIGINT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_edicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    peso DECIMAL(5,2) DEFAULT 1.00,
    alternativa_correta ENUM('a', 'b', 'c', 'd', 'e') NOT NULL,
    FOREIGN KEY (id_disciplina) REFERENCES disciplinas (id_disciplina),
    FOREIGN KEY (id_criador) REFERENCES usuarios (id_usuario),
    FOREIGN KEY (id_editor) REFERENCES usuarios (id_usuario)
);

-- -----------------------------------------------------
-- Tabela de Opções das Questões
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS opcoes_questao (
    id_opcao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_questao BIGINT NOT NULL,
    letra ENUM('a', 'b', 'c', 'd', 'e') NOT NULL,
    texto_opcao TEXT NOT NULL,
    correta BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_questao) REFERENCES questoes (id_questao) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Tabela de Provas (Geradas pelo Coordenador)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS provas (
    id_prova BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_coordenador BIGINT NOT NULL,
    nome_prova VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_coordenador) REFERENCES usuarios (id_usuario)
);

-- -----------------------------------------------------
-- Tabela de Relacionamento entre Provas e Questões
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS provas_questoes (
    id_prova BIGINT,
    id_questao BIGINT,
    PRIMARY KEY (id_prova, id_questao),
    FOREIGN KEY (id_prova) REFERENCES provas (id_prova) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES questoes (id_questao)
);

-- -----------------------------------------------------
-- Tabela de Simulados (Para Alunos sem cadastro)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS simulados (
    id_simulado BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_professor BIGINT NOT NULL,
    id_disciplina BIGINT NOT NULL,
    pontuacao DECIMAL(5,2),
    data_realizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_professor) REFERENCES usuarios (id_usuario),
    FOREIGN KEY (id_disciplina) REFERENCES disciplinas (id_disciplina)
);

-- -----------------------------------------------------
-- Tabela de Relacionamento entre Simulados e Questões
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS simulados_questoes (
    id_simulado BIGINT,
    id_questao BIGINT,
    resposta_aluno ENUM('a', 'b', 'c', 'd', 'e'),
    PRIMARY KEY (id_simulado, id_questao),
    FOREIGN KEY (id_simulado) REFERENCES simulados (id_simulado) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES questoes (id_questao)
);

-- -----------------------------------------------------
-- Tabela de Ranking 
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS ranking (
    id_ranking BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_simulado BIGINT NOT NULL,
    nome_aluno VARCHAR(255),
    pontuacao DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (id_simulado) REFERENCES simulados (id_simulado)
);

-- -----------------------------------------------------
-- Inserção de dados iniciais - Questões
-- -----------------------------------------------------

-- Banco de Dados em MySQL (id_disciplina = 1) 
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(1, 'Qual comando é usado para selecionar dados em MySQL?', 'multipla_escolha', 1, 1, 'a'),
(2, 'Qual comando cria um novo banco de dados?', 'multipla_escolha', 1, 1, 'b'),
(3, 'Qual tipo de dado armazena texto longo?', 'multipla_escolha', 1, 1, 'c'),
(4, 'Qual comando insere dados em uma tabela?', 'multipla_escolha', 1, 1, 'b'),
(5, 'Qual comando remove uma tabela?', 'multipla_escolha', 1, 1, 'b'),
(6, 'Qual comando modifica dados já existentes?', 'multipla_escolha', 1, 1, 'a'),
(7, 'Qual comando mostra todas as tabelas de um banco?', 'multipla_escolha', 1, 1, 'a'),
(8, 'Qual cláusula é usada para ordenar resultados?', 'verdadeiro_falso', 1, 1, 'b'),
(9, 'Qual comando adiciona uma nova coluna em uma tabela?', 'multipla_escolha', 1, 1, 'a'),
(10, 'Qual cláusula filtra registros?', 'verdadeiro_falso', 1, 1, 'a');

-- Linguagem de Programação C (id_disciplina = 2) 
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(11, 'Qual especificador imprime um inteiro?', 'multipla_escolha', 2, 1, 'b'),
(12, 'Qual função lê dados do teclado?', 'multipla_escolha', 2, 1, 'b'),
(13, 'Qual operador é usado para incremento?', 'multipla_escolha', 2, 1, 'b'),
(14, 'Qual é o cabeçalho da função principal?', 'multipla_escolha', 2, 1, 'c'),
(15, 'Qual palavra-chave declara uma constante?', 'multipla_escolha', 2, 1, 'b'),
(16, 'Qual estrutura de repetição verifica primeiro a condição?', 'multipla_escolha', 2, 1, 'a'),
(17, 'Qual operador lógico representa E?', 'multipla_escolha', 2, 1, 'c'),
(18, 'Qual biblioteca é usada para entrada e saída padrão?', 'multipla_escolha', 2, 1, 'a'),
(19, 'Qual tipo de dado armazena caracteres?', 'multipla_escolha', 2, 1, 'c'),
(20, 'Qual símbolo indica ponteiro?', 'multipla_escolha', 2, 1, 'b');

-- Linguagem de Programação C# (id_disciplina = 3)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(21, 'Qual é a extensão padrão de arquivos C#?', 'multipla_escolha', 3, 1, 'b'),
(22, 'Qual namespace contém classes básicas do C#?', 'multipla_escolha', 3, 1, 'a'),
(23, 'Qual comando imprime no console?', 'multipla_escolha', 3, 1, 'c'),
(24, 'Qual palavra-chave define uma classe?', 'multipla_escolha', 3, 1, 'c'),
(25, 'Qual tipo representa números inteiros?', 'multipla_escolha', 3, 1, 'a'),
(26, 'Qual palavra-chave cria um objeto?', 'multipla_escolha', 3, 1, 'b'),
(27, 'Qual palavra-chave indica herança?', 'multipla_escolha', 3, 1, 'c'),
(28, 'Qual método é o ponto de entrada do programa?', 'multipla_escolha', 3, 1, 'c'),
(29, 'Qual estrutura de decisão existe em C#?', 'multipla_escolha', 3, 1, 'a'),
(30, 'Qual tipo de dado armazena verdadeiro ou falso?', 'verdadeiro_falso', 3, 1, 'c');

-- Estrutura de Dados em Java (id_disciplina = 4)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(31, 'Qual estrutura segue o princípio FIFO?', 'multipla_escolha', 4, 1, 'b'),
(32, 'Qual estrutura segue o princípio LIFO?', 'multipla_escolha', 4, 1, 'a'),
(33, 'Qual classe implementa listas dinâmicas em Java?', 'multipla_escolha', 4, 1, 'b'),
(34, 'Qual estrutura é usada para busca rápida por chave?', 'multipla_escolha', 4, 1, 'c'),
(35, 'Qual algoritmo ordena dividindo em subproblemas?', 'multipla_escolha', 4, 1, 'b'),
(36, 'Qual é a complexidade do acesso em ArrayList?', 'multipla_escolha', 4, 1, 'a'),
(37, 'Qual árvore é balanceada automaticamente?', 'multipla_escolha', 4, 1, 'a'),
(38, 'Qual estrutura usa prioridade?', 'multipla_escolha', 4, 1, 'c'),
(39, 'Qual método insere no final do ArrayList?', 'multipla_escolha', 4, 1, 'b'),
(40, 'ArrayList permite elementos duplicados?', 'verdadeiro_falso', 4, 1, 'a');

-- Programação Orientada a Objetos em Java (id_disciplina = 5)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(41, 'Qual palavra-chave define uma classe?', 'multipla_escolha', 5, 1, 'a'),
(42, 'Qual palavra-chave cria um objeto?', 'multipla_escolha', 5, 1, 'b'),
(43, 'Qual modificador permite acesso somente dentro da classe?', 'multipla_escolha', 5, 1, 'b'),
(44, 'Qual conceito permite reutilização por herança?', 'multipla_escolha', 5, 1, 'b'),
(45, 'Qual conceito permite múltiplas formas de um método?', 'multipla_escolha', 5, 1, 'a'),
(46, 'Qual palavra-chave impede herança?', 'multipla_escolha', 5, 1, 'a'),
(47, 'Qual método é chamado ao criar um objeto?', 'multipla_escolha', 5, 1, 'b'),
(48, 'Qual anotação é usada para sobrescrever um método?', 'multipla_escolha', 5, 1, 'a'),
(49, 'Qual palavra-chave referencia a própria instância?', 'multipla_escolha', 5, 1, 'a'),
(50, 'Java é uma linguagem orientada a objetos?', 'verdadeiro_falso', 5, 1, 'a');

-- Engenharia de Software (id_disciplina = 6)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(51, 'Qual modelo é conhecido como cascata?', 'multipla_escolha', 6, 1, 'c'),
(52, 'Qual metodologia usa sprints?', 'multipla_escolha', 6, 1, 'b'),
(53, 'Qual artefato documenta requisitos?', 'multipla_escolha', 6, 1, 'c'),
(54, 'Qual diagrama UML mostra interações?', 'multipla_escolha', 6, 1, 'b'),
(55, 'Qual prática envolve integração frequente do código?', 'multipla_escolha', 6, 1, 'b'),
(56, 'Qual é o objetivo da engenharia de requisitos?', 'multipla_escolha', 6, 1, 'b'),
(57, 'Qual documento detalha o design do sistema?', 'multipla_escolha', 6, 1, 'a'),
(58, 'Qual técnica de estimativa é usada em ágil?', 'multipla_escolha', 6, 1, 'c'),
(59, 'Scrum é uma metodologia ágil?', 'verdadeiro_falso', 6, 1, 'a'),
(60, 'Testes devem ser feitos apenas no final do projeto?', 'verdadeiro_falso', 6, 1, 'b');

-- Arquitetura e Organização de Computadores (id_disciplina = 7)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(61, 'Qual unidade executa instruções?', 'multipla_escolha', 7, 1, 'c'),
(62, 'Qual é a menor unidade de informação?', 'multipla_escolha', 7, 1, 'b'),
(63, 'Qual memória é volátil?', 'multipla_escolha', 7, 1, 'd'),
(64, 'Qual barramento transfere dados?', 'multipla_escolha', 7, 1, 'c'),
(65, 'Qual nível de cache é mais rápido?', 'multipla_escolha', 7, 1, 'a'),
(66, 'Qual registrador armazena o próximo endereço de instrução?', 'multipla_escolha', 7, 1, 'a'),
(67, 'Qual arquitetura é usada em PCs?', 'multipla_escolha', 7, 1, 'c'),
(68, 'Qual componente armazena dados temporários?', 'multipla_escolha', 7, 1, 'a'),
(69, 'RAM é uma memória volátil?', 'verdadeiro_falso', 7, 1, 'a'),
(70, 'ROM é uma memória somente leitura?', 'verdadeiro_falso', 7, 1, 'a');

-- Redes (id_disciplina = 8)
INSERT INTO questoes (id_questao, texto_questao, tipo_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(71, 'Qual camada do modelo OSI é responsável pelo roteamento?', 'multipla_escolha', 8, 1, 'c'),
(72, 'Qual protocolo é usado para enviar e-mails?', 'multipla_escolha', 8, 1, 'b'),
(73, 'Qual protocolo é usado para transferência de arquivos?', 'multipla_escolha', 8, 1, 'a'),
(74, 'Qual dispositivo conecta redes diferentes?', 'multipla_escolha', 8, 1, 'c'),
(75, 'Qual é o endereço IP privado?', 'multipla_escolha', 8, 1, 'a'),
(76, 'Qual protocolo traduz nomes para IPs?', 'multipla_escolha', 8, 1, 'a'),
(77, 'Qual camada garante comunicação ponta a ponta?', 'multipla_escolha', 8, 1, 'b'),
(78, 'Qual tecnologia é usada em redes sem fio?', 'multipla_escolha', 8, 1, 'b'),
(79, 'HTTP é um protocolo da camada de aplicação?', 'verdadeiro_falso', 8, 1, 'a'),
(80, 'HTTPS é mais seguro que HTTP?', 'verdadeiro_falso', 8, 1, 'a');

-- Garante que o próximo ID de questão gerado automaticamente será 81
ALTER TABLE questoes AUTO_INCREMENT = 81;


-- -----------------------------------------------------
-- Inserção de dados iniciais - Opções das Questões
-- -----------------------------------------------------
-- Banco de Dados em MySQL (id_questao 1 a 10)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(1, 'a', 'SELECT', TRUE), (1, 'b', 'INSERT', FALSE), (1, 'c', 'UPDATE', FALSE), (1, 'd', 'DELETE', FALSE), (1, 'e', 'CREATE', FALSE),
(2, 'a', 'SELECT', FALSE), (2, 'b', 'CREATE DATABASE', TRUE), (2, 'c', 'ALTER DATABASE', FALSE), (2, 'd', 'USE', FALSE), (2, 'e', 'SHOW', FALSE),
(3, 'a', 'VARCHAR', TRUE), (3, 'b', 'CHAR', FALSE), (3, 'c', 'TEXT', FALSE), (3, 'd', 'INT', FALSE), (3, 'e', 'FLOAT', FALSE),
(4, 'a', 'SELECT', FALSE), (4, 'b', 'INSERT INTO', TRUE), (4, 'c', 'UPDATE', FALSE), (4, 'd', 'DELETE', FALSE), (4, 'e', 'DROP', FALSE),
(5, 'a', 'REMOVE', FALSE), (5, 'b', 'DROP TABLE', TRUE), (5, 'c', 'DELETE TABLE', FALSE), (5, 'd', 'ERASE', FALSE), (5, 'e', 'CLEAR', FALSE),
(6, 'a', 'UPDATE', TRUE), (6, 'b', 'SELECT', FALSE), (6, 'c', 'DELETE', FALSE), (6, 'd', 'CREATE', FALSE), (6, 'e', 'SHOW', FALSE),
(7, 'a', 'SHOW TABLES', TRUE), (7, 'b', 'DESCRIBE', FALSE), (7, 'c', 'SELECT *', FALSE), (7, 'd', 'LIST TABLES', FALSE), (7, 'e', 'VIEW', FALSE),
(8, 'a', 'GROUP BY', TRUE), (8, 'b', 'ORDER BY', FALSE), (8, 'c', 'SORT', FALSE), (8, 'd', 'HAVING', FALSE), (8, 'e', 'WHERE', FALSE),
(9, 'a', 'ALTER TABLE', FALSE), (9, 'b', 'CREATE COLUMN', FALSE), (9, 'c', 'ADD COLUMN', TRUE), (9, 'd', 'MODIFY', FALSE), (9, 'e', 'APPEND', FALSE),
(10, 'a', 'WHERE', FALSE), (10, 'b', 'GROUP BY', FALSE), (10, 'c', 'HAVING', TRUE), (10, 'd', 'ORDER BY', FALSE), (10, 'e', 'JOIN', FALSE);

-- Linguagem de Programação C (id_questao 11 a 20)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(11, 'a', '%s', TRUE), (11, 'b', '%d', FALSE), (11, 'c', '%c', FALSE), (11, 'd', '%f', FALSE), (11, 'e', '%i', FALSE),
(12, 'a', 'printf', FALSE), (12, 'b', 'scanf', TRUE), (12, 'c', 'cin', FALSE), (12, 'd', 'gets', FALSE), (12, 'e', 'input', FALSE),
(13, 'a', '--', FALSE), (13, 'b', '++', TRUE), (13, 'c', '**', FALSE), (13, 'd', '+=', FALSE), (13, 'e', '=>', FALSE),
(14, 'a', 'main()', FALSE), (14, 'b', 'void main()', FALSE), (14, 'c', 'int main()', TRUE), (14, 'd', 'function main()', FALSE), (14, 'e', 'public main()', FALSE),
(15, 'a', 'final', FALSE), (15, 'b', 'const', TRUE), (15, 'c', 'let', FALSE), (15, 'd', 'constant', FALSE), (15, 'e', 'define', FALSE),
(16, 'a', 'while', FALSE), (16, 'b', 'do...while', FALSE), (16, 'c', 'for', TRUE), (16, 'd', 'foreach', FALSE), (16, 'e', 'loop', FALSE),
(17, 'a', '||', FALSE), (17, 'b', '!', TRUE), (17, 'c', '&&', FALSE), (17, 'd', '&', FALSE), (17, 'e', '==', FALSE),
(18, 'a', 'stdio.h', TRUE), (18, 'b', 'iostream', FALSE), (18, 'c', 'conio.h', FALSE), (18, 'd', 'system.h', FALSE), (18, 'e', 'string.h', FALSE),
(19, 'a', 'int', TRUE), (19, 'b', 'float', FALSE), (19, 'c', 'char', FALSE), (19, 'd', 'string', FALSE), (19, 'e', 'text', FALSE),
(20, 'a', '&', TRUE), (20, 'b', '*', FALSE), (20, 'c', '->', FALSE), (20, 'd', '=>', FALSE), (20, 'e', '%', FALSE);

-- Linguagem de Programação C# (id_questao 21 a 30)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(21, 'a', '.java', FALSE), (21, 'b', '.cs', TRUE), (21, 'c', '.cpp', FALSE), (21, 'd', '.c', FALSE), (21, 'e', '.py', FALSE),
(22, 'a', 'System', TRUE), (22, 'b', 'Main', FALSE), (22, 'c', 'CSharp', FALSE), (22, 'd', 'Console', FALSE), (22, 'e', 'IO', FALSE),
(23, 'a', 'print()', FALSE), (23, 'b', 'echo()', FALSE), (23, 'c', 'Console.WriteLine()', TRUE), (23, 'd', 'cout<<', FALSE), (23, 'e', 'printf()', FALSE),
(24, 'a', 'struct', FALSE), (24, 'b', 'object', FALSE), (24, 'c', 'class', TRUE), (24, 'd', 'define', FALSE), (24, 'e', 'entity', FALSE),
(25, 'a', 'int', TRUE), (25, 'b', 'float', FALSE), (25, 'c', 'string', FALSE), (25, 'd', 'double', FALSE), (25, 'e', 'bool', FALSE),
(26, 'a', 'class', FALSE), (26, 'b', 'new', TRUE), (26, 'c', 'create', FALSE), (26, 'd', 'object', FALSE), (26, 'e', 'alloc', FALSE),
(27, 'a', 'implements', FALSE), (27, 'b', 'extends', FALSE), (27, 'c', ':', TRUE), (27, 'd', '->', FALSE), (27, 'e', 'inherit', FALSE),
(28, 'a', 'Start()', FALSE), (28, 'b', 'Begin()', FALSE), (28, 'c', 'Main()', TRUE), (28, 'd', 'Run()', FALSE), (28, 'e', 'Init()', FALSE),
(29, 'a', 'if', TRUE), (29, 'b', 'choose', FALSE), (29, 'c', 'select', FALSE), (29, 'd', 'switch', FALSE), (29, 'e', 'case', FALSE),
(30, 'a', 'Verdadeiro', FALSE), (30, 'b', 'Falso', TRUE);

-- Estrutura de Dados em Java (id_questao 31 a 40)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(31, 'a', 'Pilha', FALSE), (31, 'b', 'Fila', TRUE), (31, 'c', 'Lista', FALSE), (31, 'd', 'Árvore', FALSE), (31, 'e', 'Grafo', FALSE),
(32, 'a', 'Pilha', TRUE), (32, 'b', 'Fila', FALSE), (32, 'c', 'Lista', FALSE), (32, 'd', 'Árvore', FALSE), (32, 'e', 'Hash', FALSE),
(33, 'a', 'Array', FALSE), (33, 'b', 'ArrayList', TRUE), (33, 'c', 'List', FALSE), (33, 'd', 'HashMap', FALSE), (33, 'e', 'Set', FALSE),
(34, 'a', 'Fila', FALSE), (34, 'b', 'Pilha', FALSE), (34, 'c', 'HashMap', TRUE), (34, 'd', 'Árvore', FALSE), (34, 'e', 'Grafo', FALSE),
(35, 'a', 'BubbleSort', FALSE), (35, 'b', 'QuickSort', TRUE), (35, 'c', 'SelectionSort', FALSE), (35, 'd', 'InsertionSort', FALSE), (35, 'e', 'MergeSort', FALSE),
(36, 'a', 'O(1)', TRUE), (36, 'b', 'O(n)', FALSE), (36, 'c', 'O(log n)', FALSE), (36, 'd', 'O(n²)', FALSE), (36, 'e', 'O(n log n)', FALSE),
(37, 'a', 'AVL', TRUE), (37, 'b', 'Binária', FALSE), (37, 'c', 'B', FALSE), (37, 'd', 'Trie', FALSE), (37, 'e', 'Heap', FALSE),
(38, 'a', 'Fila', FALSE), (38, 'b', 'Pilha', FALSE), (38, 'c', 'PriorityQueue', TRUE), (38, 'd', 'HashSet', FALSE), (38, 'e', 'LinkedList', FALSE),
(39, 'a', 'insert()', FALSE), (39, 'b', 'add()', TRUE), (39, 'c', 'push()', FALSE), (39, 'd', 'append()', FALSE), (39, 'e', 'put()', FALSE),
(40, 'a', 'Verdadeiro', TRUE), (40, 'b', 'Falso', FALSE);

-- Programação Orientada a Objetos em Java (id_questao 41 a 50)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(41, 'a', 'class', TRUE), (41, 'b', 'object', FALSE), (41, 'c', 'struct', FALSE), (41, 'd', 'define', FALSE), (41, 'e', 'entity', FALSE),
(42, 'a', 'object', FALSE), (42, 'b', 'new', TRUE), (42, 'c', 'create', FALSE), (42, 'd', 'alloc', FALSE), (42, 'e', 'make', FALSE),
(43, 'a', 'public', FALSE), (43, 'b', 'private', TRUE), (43, 'c', 'protected', FALSE), (43, 'd', 'static', FALSE), (43, 'e', 'default', FALSE),
(44, 'a', 'Polimorfismo', FALSE), (44, 'b', 'Herança', TRUE), (44, 'c', 'Encapsulamento', FALSE), (44, 'd', 'Abstração', FALSE), (44, 'e', 'Modularidade', FALSE),
(45, 'a', 'Polimorfismo', TRUE), (45, 'b', 'Herança', FALSE), (45, 'c', 'Encapsulamento', FALSE), (45, 'd', 'Abstração', FALSE), (45, 'e', 'Modularidade', FALSE),
(46, 'a', 'final', TRUE), (46, 'b', 'static', FALSE), (46, 'c', 'const', FALSE), (46, 'd', 'private', FALSE), (46, 'e', 'sealed', FALSE),
(47, 'a', 'init()', FALSE), (47, 'b', 'constructor', TRUE), (47, 'c', 'main()', FALSE), (47, 'd', 'start()', FALSE), (47, 'e', 'void()', FALSE),
(48, 'a', '@Override', TRUE), (48, 'b', '@Over', FALSE), (48, 'c', '@Rewrite', FALSE), (48, 'd', '@Replace', FALSE), (48, 'e', '@Super', FALSE),
(49, 'a', 'this', TRUE), (49, 'b', 'self', FALSE), (49, 'c', 'super', FALSE), (49, 'd', 'me', FALSE), (49, 'e', 'obj', FALSE),
(50, 'a', 'Verdadeiro', TRUE), (50, 'b', 'Falso', FALSE);

-- Engenharia de Software (id_questao 51 a 60)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(51, 'a', 'Incremental', FALSE), (51, 'b', 'Espiral', FALSE), (51, 'c', 'Waterfall', TRUE), (51, 'd', 'Ágil', FALSE), (51, 'e', 'RAD', FALSE),
(52, 'a', 'Cascata', FALSE), (52, 'b', 'Scrum', TRUE), (52, 'c', 'Espiral', FALSE), (52, 'd', 'RAD', FALSE), (52, 'e', 'Prototipação', FALSE),
(53, 'a', 'DFD', FALSE), (53, 'b', 'DER', FALSE), (53, 'c', 'Documento de Requisitos', TRUE), (53, 'd', 'UML', FALSE), (53, 'e', 'Casos de Teste', FALSE),
(54, 'a', 'Classe', FALSE), (54, 'b', 'Sequência', TRUE), (54, 'c', 'Caso de Uso', FALSE), (54, 'd', 'Atividade', FALSE), (54, 'e', 'Estado', FALSE),
(55, 'a', 'TDD', FALSE), (55, 'b', 'CI', TRUE), (55, 'c', 'Refatoração', FALSE), (55, 'd', 'BDD', FALSE), (55, 'e', 'Scrum', FALSE),
(56, 'a', 'Testar código', FALSE), (56, 'b', 'Definir o que o sistema deve fazer', TRUE), (56, 'c', 'Criar diagramas UML', FALSE), (56, 'd', 'Implementar banco', FALSE), (56, 'e', 'Codificar', FALSE),
(57, 'a', 'Documento de Arquitetura', TRUE), (57, 'b', 'Documento de Requisitos', FALSE), (57, 'c', 'DER', FALSE), (57, 'd', 'Diagrama de Sequência', FALSE), (57, 'e', 'Plano de Testes', FALSE),
(58, 'a', 'PERT', FALSE), (58, 'b', 'CPM', FALSE), (58, 'c', 'Planning Poker', TRUE), (58, 'd', 'Function Points', FALSE), (58, 'e', 'Gantt', FALSE),
(59, 'a', 'Verdadeiro', TRUE), (59, 'b', 'Falso', FALSE),
(60, 'a', 'Verdadeiro', FALSE), (60, 'b', 'Falso', TRUE);

-- Arquitetura e Organização de Computadores (id_disciplina = 7)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(61, 'a', 'ULA', FALSE), (61, 'b', 'UC', FALSE), (61, 'c', 'CPU', TRUE), (61, 'd', 'Registradores', FALSE), (61, 'e', 'Memória', FALSE),
(62, 'a', 'Byte', FALSE), (62, 'b', 'Bit', TRUE), (62, 'c', 'Palavra', FALSE), (62, 'd', 'Nibble', FALSE), (62, 'e', 'Registro', FALSE),
(63, 'a', 'ROM', FALSE), (63, 'b', 'Flash', FALSE), (63, 'c', 'HD', FALSE), (63, 'd', 'RAM', TRUE), (63, 'e', 'SSD', FALSE),
(64, 'a', 'Controle', FALSE), (64, 'b', 'Endereço', FALSE), (64, 'c', 'Dados', TRUE), (64, 'd', 'Cache', FALSE), (64, 'e', 'Clock', FALSE),
(65, 'a', 'L1', TRUE), (65, 'b', 'L2', FALSE), (65, 'c', 'L3', FALSE), (65, 'd', 'RAM', FALSE), (65, 'e', 'Disco', FALSE),
(66, 'a', 'PC', TRUE), (66, 'b', 'IR', FALSE), (66, 'c', 'MAR', FALSE), (66, 'd', 'MDR', FALSE), (66, 'e', 'ACC', FALSE),
(67, 'a', 'RISC', FALSE), (67, 'b', 'CISC', FALSE), (67, 'c', 'Von Neumann', TRUE), (67, 'd', 'Harvard', FALSE), (67, 'e', 'MIPS', FALSE),
(68, 'a', 'Registradores', TRUE), (68, 'b', 'Memória Secundária', FALSE), (68, 'c', 'Cache', FALSE), (68, 'd', 'HD', FALSE), (68, 'e', 'ROM', FALSE),
(69, 'a', 'Verdadeiro', TRUE), (69, 'b', 'Falso', FALSE),
(70, 'a', 'Verdadeiro', TRUE), (70, 'b', 'Falso', FALSE);

-- Redes (id_disciplina = 8)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao, correta) VALUES
(71, 'a', 'Física', FALSE), (71, 'b', 'Enlace', FALSE), (71, 'c', 'Rede', TRUE), (71, 'd', 'Transporte', FALSE), (71, 'e', 'Aplicação', FALSE),
(72, 'a', 'HTTP', FALSE), (72, 'b', 'SMTP', TRUE), (72, 'c', 'FTP', FALSE), (72, 'd', 'SNMP', FALSE), (72, 'e', 'POP', FALSE),
(73, 'a', 'FTP', TRUE), (73, 'b', 'HTTP', FALSE), (73, 'c', 'SMTP', FALSE), (73, 'd', 'DNS', FALSE), (73, 'e', 'DHCP', FALSE),
(74, 'a', 'Switch', FALSE), (74, 'b', 'Hub', FALSE), (74, 'c', 'Roteador', TRUE), (74, 'd', 'Bridge', FALSE), (74, 'e', 'Modem', FALSE),
(75, 'a', '192.168.0.1', TRUE), (75, 'b', '8.8.8.8', FALSE), (75, 'c', '172.217.3.110', FALSE), (75, 'd', '54.23.12.1', FALSE), (75, 'e', '1.1.1.1', FALSE),
(76, 'a', 'DNS', TRUE), (76, 'b', 'DHCP', FALSE), (76, 'c', 'SMTP', FALSE), (76, 'd', 'ARP', FALSE), (76, 'e', 'ICMP', FALSE),
(77, 'a', 'Rede', FALSE), (77, 'b', 'Transporte', TRUE), (77, 'c', 'Aplicação', FALSE), (77, 'd', 'Enlace', FALSE), (77, 'e', 'Física', FALSE),
(78, 'a', 'Ethernet', FALSE), (78, 'b', 'Wi-Fi', TRUE), (78, 'c', 'Token Ring', FALSE), (78, 'd', 'Bluetooth', FALSE), (78, 'e', 'DSL', FALSE),
(79, 'a', 'Verdadeiro', TRUE), (79, 'b', 'Falso', FALSE),
(80, 'a', 'Verdadeiro', TRUE), (80, 'b', 'Falso', FALSE);

-- Garante que o próximo ID de opção gerado automaticamente será 401 (80 questões * 5 opções = 400 IDs inseridos)
ALTER TABLE opcoes_questao AUTO_INCREMENT = 401;

-- Reabilita verificação de chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 1;

-- FIM DO SCRIPT