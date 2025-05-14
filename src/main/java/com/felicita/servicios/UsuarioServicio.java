package com.felicita.servicios;

import com.felicita.dto.UsuarioDTO;
import com.felicita.entidades.Usuario;
import java.util.List;

public interface UsuarioServicio {
    List<UsuarioDTO> listarTodos();
    UsuarioDTO buscarPorId(Long id);
    UsuarioDTO buscarPorEmail(String email);
    UsuarioDTO guardar(UsuarioDTO usuarioDTO);
    UsuarioDTO actualizar(Long id, UsuarioDTO usuarioDTO);
    void eliminar(Long id);
    boolean existeEmail(String email);
}