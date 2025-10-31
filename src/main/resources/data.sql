
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
INSERT INTO usuarios (id_usuario, nome, email, senha, id_tipo) VALUES
(1, 'Jadir', 'jadir@fatec.sp.br', '1234', 1);

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
-- Tabela de Relacionamento entre Professores e Disciplinas
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS professor_disciplina (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_disciplina BIGINT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_disciplina) REFERENCES disciplinas(id_disciplina)
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
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(1, 'Qual comando é usado para selecionar dados em MySQL?', 1, 1, 'a'),
(2, 'Qual comando cria um novo banco de dados?', 1, 1, 'b'),
(3, 'Qual tipo de dado armazena texto longo?', 1, 1, 'c'),
(4, 'Qual comando insere dados em uma tabela?', 1, 1, 'b'),
(5, 'Qual comando remove uma tabela?', 1, 1, 'b'),
(6, 'Qual comando modifica dados já existentes?', 1, 1, 'a'),
(7, 'Qual comando mostra todas as tabelas de um banco?', 1, 1, 'a'),
(8, 'Qual cláusula é usada para ordenar resultados?', 1, 1, 'b'),
(9, 'Qual comando adiciona uma nova coluna em uma tabela?', 1, 1, 'a'),
(10, 'Qual cláusula filtra registros?', 1, 1, 'a');

-- Linguagem de Programação C (id_disciplina = 2) 
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(11, 'Qual especificador imprime um inteiro?', 2, 1, 'b'),
(12, 'Qual função lê dados do teclado?', 2, 1, 'b'),
(13, 'Qual operador é usado para incremento?', 2, 1, 'b'),
(14, 'Qual é o cabeçalho da função principal?', 2, 1, 'c'),
(15, 'Qual palavra-chave declara uma constante?', 2, 1, 'b'),
(16, 'Qual estrutura de repetição verifica primeiro a condição?', 2, 1, 'a'),
(17, 'Qual operador lógico representa E?', 2, 1, 'c'),
(18, 'Qual biblioteca é usada para entrada e saída padrão?', 2, 1, 'a'),
(19, 'Qual tipo de dado armazena caracteres?', 2, 1, 'c'),
(20, 'Qual símbolo indica ponteiro?', 2, 1, 'b');

-- Linguagem de Programação C# (id_disciplina = 3)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(21, 'Qual é a extensão padrão de arquivos C#?', 3, 1, 'b'),
(22, 'Qual namespace contém classes básicas do C#?', 3, 1, 'a'),
(23, 'Qual comando imprime no console?', 3, 1, 'c'),
(24, 'Qual palavra-chave define uma classe?', 3, 1, 'c'),
(25, 'Qual tipo representa números inteiros?', 3, 1, 'a'),
(26, 'Qual palavra-chave cria um objeto?', 3, 1, 'b'),
(27, 'Qual palavra-chave indica herança?', 3, 1, 'c'),
(28, 'Qual método é o ponto de entrada do programa?', 3, 1, 'c'),
(29, 'Qual estrutura de decisão existe em C#?', 3, 1, 'a'),
(30, 'Qual tipo de dado armazena verdadeiro ou falso?', 3, 1, 'c');

-- Estrutura de Dados em Java (id_disciplina = 4)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(31, 'Qual estrutura segue o princípio FIFO?', 4, 1, 'b'),
(32, 'Qual estrutura segue o princípio LIFO?', 4, 1, 'a'),
(33, 'Qual classe implementa listas dinâmicas em Java?', 4, 1, 'b'),
(34, 'Qual estrutura é usada para busca rápida por chave?', 4, 1, 'c'),
(35, 'Qual algoritmo ordena dividindo em subproblemas?', 4, 1, 'b'),
(36, 'Qual é a complexidade do acesso em ArrayList?', 4, 1, 'a'),
(37, 'Qual árvore é balanceada automaticamente?', 4, 1, 'a'),
(38, 'Qual estrutura usa prioridade?', 4, 1, 'c'),
(39, 'Qual método insere no final do ArrayList?', 4, 1, 'b'),
(40, 'Qual estrutura não permite elementos duplicados?', 4, 1, 'b');

-- Programação Orientada a Objetos em Java (id_disciplina = 5)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(41, 'Qual palavra-chave define uma classe?', 5, 1, 'a'),
(42, 'Qual palavra-chave cria um objeto?', 5, 1, 'b'),
(43, 'Qual modificador permite acesso somente dentro da classe?', 5, 1, 'b'),
(44, 'Qual conceito permite reutilização por herança?', 5, 1, 'b'),
(45, 'Qual conceito permite múltiplas formas de um método?', 5, 1, 'a'),
(46, 'Qual palavra-chave impede herança?', 5, 1, 'a'),
(47, 'Qual método é chamado ao criar um objeto?', 5, 1, 'b'),
(48, 'Qual anotação é usada para sobrescrever um método?', 5, 1, 'a'),
(49, 'Qual palavra-chave referencia a própria instância?', 5, 1, 'a'),
(50, 'Qual conceito protege os dados e expõe apenas métodos?', 5, 1, 'b');

-- Engenharia de Software (id_disciplina = 6)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(51, 'Qual modelo é conhecido como cascata?', 6, 1, 'c'),
(52, 'Qual metodologia usa sprints?', 6, 1, 'b'),
(53, 'Qual artefato documenta requisitos?', 6, 1, 'c'),
(54, 'Qual diagrama UML mostra interações?', 6, 1, 'b'),
(55, 'Qual prática envolve integração frequente do código?', 6, 1, 'b'),
(56, 'Qual é o objetivo da engenharia de requisitos?', 6, 1, 'b'),
(57, 'Qual documento detalha o design do sistema?', 6, 1, 'a'),
(58, 'Qual técnica de estimativa é usada em ágil?', 6, 1, 'c'),
(59, 'Qual é um princípio do manifesto ágil?', 6, 1, 'b'),
(60, 'Qual fase do ciclo de vida inclui testes?', 6, 1, 'd');

-- Arquitetura e Organização de Computadores (id_disciplina = 7)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(61, 'Qual unidade executa instruções?', 7, 1, 'c'),
(62, 'Qual é a menor unidade de informação?', 7, 1, 'b'),
(63, 'Qual memória é volátil?', 7, 1, 'd'),
(64, 'Qual barramento transfere dados?', 7, 1, 'c'),
(65, 'Qual nível de cache é mais rápido?', 7, 1, 'a'),
(66, 'Qual registrador armazena o próximo endereço de instrução?', 7, 1, 'a'),
(67, 'Qual arquitetura é usada em PCs?', 7, 1, 'c'),
(68, 'Qual componente armazena dados temporários?', 7, 1, 'a'),
(69, 'Qual unidade realiza cálculos matemáticos?', 7, 1, 'a'),
(70, 'Qual memória é somente leitura?', 7, 1, 'b');

-- Redes (id_disciplina = 8)
INSERT INTO questoes (id_questao, texto_questao, id_disciplina, id_criador, alternativa_correta) VALUES
(71, 'Qual camada do modelo OSI é responsável pelo roteamento?', 8, 1, 'c'),
(72, 'Qual protocolo é usado para enviar e-mails?', 8, 1, 'b'),
(73, 'Qual protocolo é usado para transferência de arquivos?', 8, 1, 'a'),
(74, 'Qual dispositivo conecta redes diferentes?', 8, 1, 'c'),
(75, 'Qual é o endereço IP privado?', 8, 1, 'a'),
(76, 'Qual protocolo traduz nomes para IPs?', 8, 1, 'a'),
(77, 'Qual camada garante comunicação ponta a ponta?', 8, 1, 'b'),
(78, 'Qual tecnologia é usada em redes sem fio?', 8, 1, 'b'),
(79, 'Qual protocolo é usado para navegação web?', 8, 1, 'b'),
(80, 'Qual protocolo seguro substitui o HTTP?', 8, 1, 'a');

-- Garante que o próximo ID de questão gerado automaticamente será 81
ALTER TABLE questoes AUTO_INCREMENT = 81;


-- -----------------------------------------------------
-- Inserção de dados iniciais - Opções das Questões
-- -----------------------------------------------------
-- Banco de Dados em MySQL (id_questao 1 a 10)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(1, 'a', 'SELECT'), (1, 'b', 'INSERT'), (1, 'c', 'UPDATE'), (1, 'd', 'DELETE'), (1, 'e', 'CREATE'),
(2, 'a', 'SELECT'), (2, 'b', 'CREATE DATABASE'), (2, 'c', 'ALTER DATABASE'), (2, 'd', 'USE'), (2, 'e', 'SHOW'),
(3, 'a', 'VARCHAR'), (3, 'b', 'CHAR'), (3, 'c', 'TEXT'), (3, 'd', 'INT'), (3, 'e', 'FLOAT'),
(4, 'a', 'SELECT'), (4, 'b', 'INSERT INTO'), (4, 'c', 'UPDATE'), (4, 'd', 'DELETE'), (4, 'e', 'DROP'),
(5, 'a', 'REMOVE'), (5, 'b', 'DROP TABLE'), (5, 'c', 'DELETE TABLE'), (5, 'd', 'ERASE'), (5, 'e', 'CLEAR'),
(6, 'a', 'UPDATE'), (6, 'b', 'SELECT'), (6, 'c', 'DELETE'), (6, 'd', 'CREATE'), (6, 'e', 'SHOW'),
(7, 'a', 'SHOW TABLES'), (7, 'b', 'DESCRIBE'), (7, 'c', 'SELECT *'), (7, 'd', 'LIST TABLES'), (7, 'e', 'VIEW'),
(8, 'a', 'GROUP BY'), (8, 'b', 'ORDER BY'), (8, 'c', 'SORT'), (8, 'd', 'HAVING'), (8, 'e', 'WHERE'),
(9, 'a', 'ALTER TABLE'), (9, 'b', 'CREATE COLUMN'), (9, 'c', 'ADD COLUMN'), (9, 'd', 'MODIFY'), (9, 'e', 'APPEND'),
(10, 'a', 'WHERE'), (10, 'b', 'GROUP BY'), (10, 'c', 'HAVING'), (10, 'd', 'ORDER BY'), (10, 'e', 'JOIN');

-- Linguagem de Programação C (id_questao 11 a 20)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(11, 'a', '%s'), (11, 'b', '%d'), (11, 'c', '%c'), (11, 'd', '%f'), (11, 'e', '%i'),
(12, 'a', 'printf'), (12, 'b', 'scanf'), (12, 'c', 'cin'), (12, 'd', 'gets'), (12, 'e', 'input'),
(13, 'a', '--'), (13, 'b', '++'), (13, 'c', '**'), (13, 'd', '+='), (13, 'e', '=>'),
(14, 'a', 'main()'), (14, 'b', 'void main()'), (14, 'c', 'int main()'), (14, 'd', 'function main()'), (14, 'e', 'public main()'),
(15, 'a', 'final'), (15, 'b', 'const'), (15, 'c', 'let'), (15, 'd', 'constant'), (15, 'e', 'define'),
(16, 'a', 'while'), (16, 'b', 'do...while'), (16, 'c', 'for'), (16, 'd', 'foreach'), (16, 'e', 'loop'),
(17, 'a', '||'), (17, 'b', '!'), (17, 'c', '&&'), (17, 'd', '&'), (17, 'e', '=='),
(18, 'a', 'stdio.h'), (18, 'b', 'iostream'), (18, 'c', 'conio.h'), (18, 'd', 'system.h'), (18, 'e', 'string.h'),
(19, 'a', 'int'), (19, 'b', 'float'), (19, 'c', 'char'), (19, 'd', 'string'), (19, 'e', 'text'),
(20, 'a', '&'), (20, 'b', '*'), (20, 'c', '->'), (20, 'd', '=>'), (20, 'e', '%');

-- Linguagem de Programação C# (id_questao 21 a 30)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(21, 'a', '.java'), (21, 'b', '.cs'), (21, 'c', '.cpp'), (21, 'd', '.c'), (21, 'e', '.py'),
(22, 'a', 'System'), (22, 'b', 'Main'), (22, 'c', 'CSharp'), (22, 'd', 'Console'), (22, 'e', 'IO'),
(23, 'a', 'print()'), (23, 'b', 'echo()'), (23, 'c', 'Console.WriteLine()'), (23, 'd', 'cout<<'), (23, 'e', 'printf()'),
(24, 'a', 'struct'), (24, 'b', 'object'), (24, 'c', 'class'), (24, 'd', 'define'), (24, 'e', 'entity'),
(25, 'a', 'int'), (25, 'b', 'float'), (25, 'c', 'string'), (25, 'd', 'double'), (25, 'e', 'bool'),
(26, 'a', 'class'), (26, 'b', 'new'), (26, 'c', 'create'), (26, 'd', 'object'), (26, 'e', 'alloc'),
(27, 'a', 'implements'), (27, 'b', 'extends'), (27, 'c', ':'), (27, 'd', '->'), (27, 'e', 'inherit'),
(28, 'a', 'Start()'), (28, 'b', 'Begin()'), (28, 'c', 'Main()'), (28, 'd', 'Run()'), (28, 'e', 'Init()'),
(29, 'a', 'if'), (29, 'b', 'choose'), (29, 'c', 'select'), (29, 'd', 'switch'), (29, 'e', 'case'),
(30, 'a', 'string'), (30, 'b', 'int'), (30, 'c', 'bool'), (30, 'd', 'double'), (30, 'e', 'var');

-- Estrutura de Dados em Java (id_questao 31 a 40)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(31, 'a', 'Pilha'), (31, 'b', 'Fila'), (31, 'c', 'Lista'), (31, 'd', 'Árvore'), (31, 'e', 'Grafo'),
(32, 'a', 'Pilha'), (32, 'b', 'Fila'), (32, 'c', 'Lista'), (32, 'd', 'Árvore'), (32, 'e', 'Hash'),
(33, 'a', 'Array'), (33, 'b', 'ArrayList'), (33, 'c', 'List'), (33, 'd', 'HashMap'), (33, 'e', 'Set'),
(34, 'a', 'Fila'), (34, 'b', 'Pilha'), (34, 'c', 'HashMap'), (34, 'd', 'Árvore'), (34, 'e', 'Grafo'),
(35, 'a', 'BubbleSort'), (35, 'b', 'QuickSort'), (35, 'c', 'SelectionSort'), (35, 'd', 'InsertionSort'), (35, 'e', 'MergeSort'),
(36, 'a', 'O(1)'), (36, 'b', 'O(n)'), (36, 'c', 'O(log n)'), (36, 'd', 'O(n²)'), (36, 'e', 'O(n log n)'),
(37, 'a', 'AVL'), (37, 'b', 'Binária'), (37, 'c', 'B'), (37, 'd', 'Trie'), (37, 'e', 'Heap'),
(38, 'a', 'Fila'), (38, 'b', 'Pilha'), (38, 'c', 'PriorityQueue'), (38, 'd', 'HashSet'), (38, 'e', 'LinkedList'),
(39, 'a', 'insert()'), (39, 'b', 'add()'), (39, 'c', 'push()'), (39, 'd', 'append()'), (39, 'e', 'put()'),
(40, 'a', 'List'), (40, 'b', 'Set'), (40, 'c', 'ArrayList'), (40, 'd', 'LinkedList'), (40, 'e', 'Stack');

-- Programação Orientada a Objetos em Java (id_questao 41 a 50)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(41, 'a', 'class'), (41, 'b', 'object'), (41, 'c', 'struct'), (41, 'd', 'define'), (41, 'e', 'entity'),
(42, 'a', 'object'), (42, 'b', 'new'), (42, 'c', 'create'), (42, 'd', 'alloc'), (42, 'e', 'make'),
(43, 'a', 'public'), (43, 'b', 'private'), (43, 'c', 'protected'), (43, 'd', 'static'), (43, 'e', 'default'),
(44, 'a', 'Polimorfismo'), (44, 'b', 'Herança'), (44, 'c', 'Encapsulamento'), (44, 'd', 'Abstração'), (44, 'e', 'Modularidade'),
(45, 'a', 'Polimorfismo'), (45, 'b', 'Herança'), (45, 'c', 'Encapsulamento'), (45, 'd', 'Abstração'), (45, 'e', 'Modularidade'),
(46, 'a', 'final'), (46, 'b', 'static'), (46, 'c', 'const'), (46, 'd', 'private'), (46, 'e', 'sealed'),
(47, 'a', 'init()'), (47, 'b', 'constructor'), (47, 'c', 'main()'), (47, 'd', 'start()'), (47, 'e', 'void()'),
(48, 'a', '@Override'), (48, 'b', '@Over'), (48, 'c', '@Rewrite'), (48, 'd', '@Replace'), (48, 'e', '@Super'),
(49, 'a', 'this'), (49, 'b', 'self'), (49, 'c', 'super'), (49, 'd', 'me'), (49, 'e', 'obj'),
(50, 'a', 'Polimorfismo'), (50, 'b', 'Encapsulamento'), (50, 'c', 'Herança'), (50, 'd', 'Modularidade'), (50, 'e', 'Sobrecarga');

-- Engenharia de Software (id_questao 51 a 60)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(51, 'a', 'Incremental'), (51, 'b', 'Espiral'), (51, 'c', 'Waterfall'), (51, 'd', 'Ágil'), (51, 'e', 'RAD'),
(52, 'a', 'Cascata'), (52, 'b', 'Scrum'), (52, 'c', 'Espiral'), (52, 'd', 'RAD'), (52, 'e', 'Prototipação'),
(53, 'a', 'DFD'), (53, 'b', 'DER'), (53, 'c', 'Documento de Requisitos'), (53, 'd', 'UML'), (53, 'e', 'Casos de Teste'),
(54, 'a', 'Classe'), (54, 'b', 'Sequência'), (54, 'c', 'Caso de Uso'), (54, 'd', 'Atividade'), (54, 'e', 'Estado'),
(55, 'a', 'TDD'), (55, 'b', 'CI'), (55, 'c', 'Refatoração'), (55, 'd', 'BDD'), (55, 'e', 'Scrum'),
(56, 'a', 'Testar código'), (56, 'b', 'Definir o que o sistema deve fazer'), (56, 'c', 'Criar diagramas UML'), (56, 'd', 'Implementar banco'), (56, 'e', 'Codificar'),
(57, 'a', 'Documento de Arquitetura'), (57, 'b', 'Documento de Requisitos'), (57, 'c', 'DER'), (57, 'd', 'Diagrama de Sequência'), (57, 'e', 'Plano de Testes'),
(58, 'a', 'PERT'), (58, 'b', 'CPM'), (58, 'c', 'Planning Poker'), (58, 'd', 'Function Points'), (58, 'e', 'Gantt'),
(59, 'a', 'Processos acima de pessoas'), (59, 'b', 'Indivíduos acima de processos'), (59, 'c', 'Documentação acima de software'), (59, 'd', 'Contratos acima de colaboração'), (59, 'e', 'Ferramentas acima de adaptação'),
(60, 'a', 'Análise'), (60, 'b', 'Projeto'), (60, 'c', 'Implementação'), (60, 'd', 'Validação'), (60, 'e', 'Codificação');

-- Arquitetura e Organização de Computadores (id_disciplina = 7)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(61, 'a', 'ULA'), (61, 'b', 'UC'), (61, 'c', 'CPU'), (61, 'd', 'Registradores'), (61, 'e', 'Memória'),
(62, 'a', 'Byte'), (62, 'b', 'Bit'), (62, 'c', 'Palavra'), (62, 'd', 'Nibble'), (62, 'e', 'Registro'),
(63, 'a', 'ROM'), (63, 'b', 'Flash'), (63, 'c', 'HD'), (63, 'd', 'RAM'), (63, 'e', 'SSD'),
(64, 'a', 'Controle'), (64, 'b', 'Endereço'), (64, 'c', 'Dados'), (64, 'd', 'Cache'), (64, 'e', 'Clock'),
(65, 'a', 'L1'), (65, 'b', 'L2'), (65, 'c', 'L3'), (65, 'd', 'RAM'), (65, 'e', 'Disco'),
(66, 'a', 'PC'), (66, 'b', 'IR'), (66, 'c', 'MAR'), (66, 'd', 'MDR'), (66, 'e', 'ACC'),
(67, 'a', 'RISC'), (67, 'b', 'CISC'), (67, 'c', 'Von Neumann'), (67, 'd', 'Harvard'), (67, 'e', 'MIPS'),
(68, 'a', 'Registradores'), (68, 'b', 'Memória Secundária'), (68, 'c', 'Cache'), (68, 'd', 'HD'), (68, 'e', 'ROM'),
(69, 'a', 'ULA'), (69, 'b', 'UC'), (69, 'c', 'Registrador'), (69, 'd', 'Cache'), (69, 'e', 'BIOS'),
(70, 'a', 'RAM'), (70, 'b', 'ROM'), (70, 'c', 'Cache'), (70, 'd', 'Flash'), (70, 'e', 'Registrador');

-- Redes (id_disciplina = 8)
INSERT INTO opcoes_questao (id_questao, letra, texto_opcao) VALUES
(71, 'a', 'Física'), (71, 'b', 'Enlace'), (71, 'c', 'Rede'), (71, 'd', 'Transporte'), (71, 'e', 'Aplicação'),
(72, 'a', 'HTTP'), (72, 'b', 'SMTP'), (72, 'c', 'FTP'), (72, 'd', 'SNMP'), (72, 'e', 'POP'),
(73, 'a', 'FTP'), (73, 'b', 'HTTP'), (73, 'c', 'SMTP'), (73, 'd', 'DNS'), (73, 'e', 'DHCP'),
(74, 'a', 'Switch'), (74, 'b', 'Hub'), (74, 'c', 'Roteador'), (74, 'd', 'Bridge'), (74, 'e', 'Modem'),
(75, 'a', '192.168.0.1'), (75, 'b', '8.8.8.8'), (75, 'c', '172.217.3.110'), (75, 'd', '54.23.12.1'), (75, 'e', '1.1.1.1'),
(76, 'a', 'DNS'), (76, 'b', 'DHCP'), (76, 'c', 'SMTP'), (76, 'd', 'ARP'), (76, 'e', 'ICMP'),
(77, 'a', 'Rede'), (77, 'b', 'Transporte'), (77, 'c', 'Aplicação'), (77, 'd', 'Enlace'), (77, 'e', 'Física'),
(78, 'a', 'Ethernet'), (78, 'b', 'Wi-Fi'), (78, 'c', 'Token Ring'), (78, 'd', 'Bluetooth'), (78, 'e', 'DSL'),
(79, 'a', 'FTP'), (79, 'b', 'HTTP'), (79, 'c', 'SNMP'), (79, 'd', 'DHCP'), (79, 'e', 'SMTP'),
(80, 'a', 'HTTPS'), (80, 'b', 'SHTTP'), (80, 'c', 'FTPS'), (80, 'd', 'SMTPS'), (80, 'e', 'SSH');

-- Garante que o próximo ID de opção gerado automaticamente será 401 (80 questões * 5 opções = 400 IDs inseridos)
ALTER TABLE opcoes_questao AUTO_INCREMENT = 401;

-- FIM DO SCRIPT