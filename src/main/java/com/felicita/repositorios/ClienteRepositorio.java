package com.felicita.repositorios;

import com.felicita.entidades.Cliente;
import com.felicita.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuario(Usuario usuario);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    Optional<Cliente> findByUsuarioEmail(String email);
    boolean existsByUsuarioId(Long usuarioId);
}