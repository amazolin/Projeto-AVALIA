package com.example.service;

import com.example.model.Usuario;
import com.example.model.TipoUsuario;
import com.example.model.Disciplina;
import com.example.model.ProfessorDisciplina;
import com.example.repository.UsuarioRepository;
import com.example.repository.TipoUsuarioRepository;
import com.example.repository.DisciplinaRepository;
import com.example.repository.ProfessorDisciplinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProfessorDisciplinaRepository professorDisciplinaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, 
                         TipoUsuarioRepository tipoUsuarioRepository,
                         DisciplinaRepository disciplinaRepository,
                         ProfessorDisciplinaRepository professorDisciplinaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.professorDisciplinaRepository = professorDisciplinaRepository;
    }

    // 🔹 Lista todos os usuários
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    // 🔹 Salva qualquer tipo de usuário (completo ou só e-mail)
    public Usuario salvarUsuario(Usuario novoUsuario) {
        return usuarioRepository.save(novoUsuario);
    }

    // 🔹 Busca usuário por e-mail e senha (para login)
    public Usuario buscarPorEmailSenha(String email, String senha) {
        return usuarioRepository.findByEmailAndSenha(email, senha);
    }

    // 🔹 Busca usuário apenas pelo e-mail
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // 🔹 Busca usuário por ID
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // 🔹 Busca TipoUsuario por ID
    public TipoUsuario buscarTipoUsuarioPorId(Long id) {
        return tipoUsuarioRepository.findById(id).orElse(null);
    }

    // 🔹 Apagar usuário
    public void apagarUsuario(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

    // 🔹 NOVO: Editar usuário COM disciplinas
    @Transactional
    public Usuario editarUsuario(Long idUsuario, String nome, String email, String rgm, 
                                 String senha, List<Integer> idsDisciplinas) {
        
        // Busca o usuário existente
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Atualiza os dados básicos
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setRgm(rgm);
        
        // Atualiza senha apenas se foi fornecida
        if (senha != null && !senha.trim().isEmpty()) {
            usuario.setSenha(senha);
        }

        // Salva as alterações do usuário
        usuario = usuarioRepository.save(usuario);

        // Atualiza as disciplinas se foram fornecidas
        if (idsDisciplinas != null) {
            atualizarDisciplinasDoUsuario(usuario, idsDisciplinas);
        }

        return usuario;
    }

    // 🔹 NOVO: Atualizar disciplinas de um usuário
    @Transactional
    public void atualizarDisciplinasDoUsuario(Usuario usuario, List<Integer> idsDisciplinas) {
        
        // Remove todas as disciplinas antigas
        professorDisciplinaRepository.deleteByUsuario(usuario);

        // Adiciona as novas disciplinas
        if (idsDisciplinas != null && !idsDisciplinas.isEmpty()) {
            for (Integer idDisciplina : idsDisciplinas) {
                Disciplina disciplina = disciplinaRepository.findById(idDisciplina)
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada: " + idDisciplina));
                
                ProfessorDisciplina pd = new ProfessorDisciplina(usuario, disciplina);
                professorDisciplinaRepository.save(pd);
            }
        }
    }

    // 🔹 NOVO: Buscar disciplinas de um usuário
    public List<Disciplina> buscarDisciplinasDoUsuario(Long idUsuario) {
        List<ProfessorDisciplina> vinculos = professorDisciplinaRepository.findByUsuarioId(idUsuario);
        return vinculos.stream()
                       .map(ProfessorDisciplina::getDisciplina)
                       .collect(Collectors.toList());
    }

    // 🔹 NOVO: Buscar todas as disciplinas
    public List<Disciplina> buscarTodasDisciplinas() {
        return disciplinaRepository.findAll();
    }
}