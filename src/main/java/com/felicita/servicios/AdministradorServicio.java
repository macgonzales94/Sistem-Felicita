package com.felicita.servicios;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.GestionUsuariosDTO;
import com.felicita.entidades.Administrador;
import com.felicita.entidades.Usuario;

import java.util.List;

public interface AdministradorServicio {
    Administrador registrarAdministrador(Usuario usuario);
    
    Administrador obtenerAdministradorPorUsuarioId(Long usuarioId);
    
    Administrador obtenerAdministradorPorUsuarioEmail(String email);
    
    Administrador actualizarAdministrador(Long id, Administrador administrador);
    
    // Métodos de gestión de usuarios
    List<GestionUsuariosDTO> listarTodosLosUsuarios();
    
    GestionUsuariosDTO obtenerUsuarioPorId(Long id);
    
    GestionUsuariosDTO crearUsuario(GestionUsuariosDTO usuarioDTO);
    
    GestionUsuariosDTO actualizarUsuario(Long id, GestionUsuariosDTO usuarioDTO);
    
    void activarUsuario(Long id);
    
    void desactivarUsuario(Long id);
    
    void eliminarUsuario(Long id);
    
    // Métodos para estadísticas y reportes
    EstadisticasDTO obtenerEstadisticasGenerales();
    
    // Método para obtener el administrador autenticado
    Administrador obtenerAdministradorEntidadPorUsuarioAutenticado();
}