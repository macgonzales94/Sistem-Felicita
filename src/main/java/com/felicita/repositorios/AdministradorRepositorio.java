package com.felicita.repositorios;

import com.felicita.entidades.Administrador;
import com.felicita.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepositorio extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByUsuario(Usuario usuario);
    
    Optional<Administrador> findByUsuarioId(Long usuarioId);
    
    Optional<Administrador> findByUsuarioEmail(String email);
    
    boolean existsByUsuarioId(Long usuarioId);
}