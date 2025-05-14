package com.felicita.servicios;

import com.felicita.dto.ClienteDTO;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.Usuario;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface ClienteServicio {
    ClienteDTO buscarPorId(Long id);
    ClienteDTO buscarPorUsuarioId(Long usuarioId);
    ClienteDTO buscarPorUsuarioEmail(String email);
    ClienteDTO registrarCliente(Usuario usuario);
    ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO);
    ClienteDTO obtenerPerfilCliente(Long usuarioId);
    void eliminarCliente(Long id);
    boolean existePorUsuarioId(Long usuarioId);
    
    // Obtener el perfil del cliente autenticado
    ClienteDTO obtenerClienteAutenticado();
    
    // Obtener la entidad Cliente a partir del usuario autenticado
    Cliente obtenerClienteEntidadPorUsuarioAutenticado();
}