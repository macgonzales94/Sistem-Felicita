package com.felicita.servicios;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ProAdminDTO;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Usuario;

import java.util.Optional;

public interface ProAdminServicio {
    ProAdminDTO buscarPorId(Long id);
    ProAdminDTO buscarPorUsuarioId(Long usuarioId);
    ProAdminDTO buscarPorUsuarioEmail(String email);
    ProAdminDTO registrarProAdmin(Usuario usuario);
    ProAdminDTO actualizarProAdmin(Long id, ProAdminDTO proAdminDTO);
    void eliminarProAdmin(Long id);
    boolean existePorUsuarioId(Long usuarioId);
    
    // Obtener el proAdmin autenticado
    ProAdminDTO obtenerProAdminAutenticado();
    
    // Obtener la entidad ProAdmin a partir del usuario autenticado
    ProAdmin obtenerProAdminEntidadPorUsuarioAutenticado();
    
    // Obtener estadísticas para el dashboard
    EstadisticasDTO obtenerEstadisticas();
}