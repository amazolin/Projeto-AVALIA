package com.example.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.model.Disciplina;
import com.example.model.ProfessorDisciplina;
import com.example.model.TipoUsuario;
import com.example.model.Usuario;
import com.example.repository.DisciplinaRepository;
import com.example.repository.ProfessorDisciplinaRepository;
import com.example.repository.TipoUsuarioRepository;
import com.example.repository.UsuarioRepository;

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

    /** 🔹 Lista todos os usuários */
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    /** 🔹 Salva qualquer tipo de usuário */
    public Usuario salvarUsuario(Usuario novoUsuario) {
        return usuarioRepository.save(novoUsuario);
    }

    /** 🔹 Busca usuário por e-mail e senha (login) */
    public Usuario buscarPorEmailSenha(String email, String senha) {
        return usuarioRepository.findByEmailAndSenha(email, senha);
    }

    /** 🔹 Busca usuário por e-mail */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /** 🔹 Busca usuário por ID */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    /** 🔹 Busca tipo de usuário por ID */
    public TipoUsuario buscarTipoUsuarioPorId(Long id) {
        return tipoUsuarioRepository.findById(id).orElse(null);
    }

    /** 🔹 Apaga um usuário do sistema */
    public void apagarUsuario(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

    /**
     * 🔹 Edita um usuário (dados + disciplinas associadas)
     */
    @Transactional
    public Usuario editarUsuario(Long idUsuario,
                                 String nome,
                                 String email,
                                 String rgm,
                                 String senha,
                                 List<Integer> idsDisciplinas) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Atualiza dados básicos
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setRgm(rgm);

        // Atualiza senha apenas se informada
        if (senha != null && !senha.trim().isEmpty()) {
            usuario.setSenha(senha);
        }

        // Salva alterações principais
        usuario = usuarioRepository.save(usuario);

        // Atualiza disciplinas vinculadas
        if (idsDisciplinas != null) {
            atualizarDisciplinasDoUsuario(usuario, idsDisciplinas);
        }

        return usuario;
    }

    /**
     * 🔹 Atualiza as disciplinas associadas a um professor
     */
    @Transactional
    public void atualizarDisciplinasDoUsuario(Usuario usuario, List<Integer> idsDisciplinas) {

        // Remove vínculos antigos
        professorDisciplinaRepository.deleteByUsuario(usuario);

        // Cria novos vínculos
        if (idsDisciplinas != null && !idsDisciplinas.isEmpty()) {
            for (Integer idDisciplina : idsDisciplinas) {
                Disciplina disciplina = disciplinaRepository.findById(Long.valueOf(idDisciplina))
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada: " + idDisciplina));

                ProfessorDisciplina pd = new ProfessorDisciplina(usuario, disciplina);
                professorDisciplinaRepository.save(pd);
            }
        }
    }

    /**
     * 🔹 Retorna todas as disciplinas associadas a um usuário
     */
    public List<Disciplina> buscarDisciplinasDoUsuario(Long idUsuario) {
        List<ProfessorDisciplina> vinculos = professorDisciplinaRepository.findByUsuarioId(idUsuario);
        return vinculos.stream()
                       .map(ProfessorDisciplina::getDisciplina)
                       .collect(Collectors.toList());
    }

    /**
     * 🔹 Retorna todas as disciplinas do sistema
     */
    public List<Disciplina> buscarTodasDisciplinas() {
        return disciplinaRepository.findAll();
    }
}
