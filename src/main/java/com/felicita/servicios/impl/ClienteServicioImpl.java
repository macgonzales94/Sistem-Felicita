package com.felicita.servicios.impl;

import com.felicita.dto.ClienteDTO;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.repositorios.ClienteRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.ClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ClienteServicioImpl implements ClienteServicio {

    @Autowired
    private ClienteRepositorio clienteRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private CitaRepositorio citaRepositorio;

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado con ID: " + id));
        return convertirADTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorUsuarioId(Long usuarioId) {
        Cliente cliente = clienteRepositorio.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado para el usuario con ID: " + usuarioId));
        return convertirADTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO buscarPorUsuarioEmail(String email) {
        Cliente cliente = clienteRepositorio.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado para el email: " + email));
        return convertirADTO(cliente);
    }

    @Override
    @Transactional
    public ClienteDTO registrarCliente(Usuario usuario) {
        // Verificar si ya existe un cliente para este usuario
        if (clienteRepositorio.existsByUsuarioId(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un cliente para este usuario");
        }
        
        // Crear un nuevo cliente
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        
        Cliente guardado = clienteRepositorio.save(cliente);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado con ID: " + id));
        
        // Solo actualizar los campos del cliente, no del usuario asociado
        cliente.setDireccion(clienteDTO.getDireccion());
        cliente.setCiudad(clienteDTO.getCiudad());
        cliente.setCodigoPostal(clienteDTO.getCodigoPostal());
        cliente.setPreferencias(clienteDTO.getPreferencias());
        
        Cliente actualizado = clienteRepositorio.save(cliente);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO obtenerPerfilCliente(Long usuarioId) {
        // Este método obtiene datos más completos que incluyen información del usuario
        Cliente cliente = clienteRepositorio.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado para el usuario con ID: " + usuarioId));
        
        ClienteDTO dto = convertirADTO(cliente);
        
        // Contar las citas futuras del cliente
        long cantidadCitas = citaRepositorio.countCitasFuturasByCliente(cliente.getId(), LocalDateTime.now());
        dto.setCantidadCitas(cantidadCitas);
        
        return dto;
    }

    @Override
    @Transactional
    public void eliminarCliente(Long id) {
        if (!clienteRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("Cliente no encontrado con ID: " + id);
        }
        clienteRepositorio.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsuarioId(Long usuarioId) {
        return clienteRepositorio.existsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDTO obtenerClienteAutenticado() {
        Cliente cliente = obtenerClienteEntidadPorUsuarioAutenticado();
        return convertirADTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerClienteEntidadPorUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con email: " + email));
        
        return clienteRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cliente no encontrado para el usuario: " + email));
    }
    
    // Métodos privados para conversión entre entidad y DTO
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setUsuarioId(cliente.getUsuario().getId());
        dto.setNombre(cliente.getUsuario().getNombre());
        dto.setApellido(cliente.getUsuario().getApellido());
        dto.setEmail(cliente.getUsuario().getEmail());
        dto.setTelefono(cliente.getUsuario().getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setCiudad(cliente.getCiudad());
        dto.setCodigoPostal(cliente.getCodigoPostal());
        dto.setPreferencias(cliente.getPreferencias());
        
        return dto;
    }
}