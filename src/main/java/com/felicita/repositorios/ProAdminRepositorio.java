package com.felicita.repositorios;

import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProAdminRepositorio extends JpaRepository<ProAdmin, Long> {
    Optional<ProAdmin> findByUsuario(Usuario usuario);
    Optional<ProAdmin> findByUsuarioId(Long usuarioId);
    Optional<ProAdmin> findByUsuarioEmail(String email);
    boolean existsByUsuarioId(Long usuarioId);
}