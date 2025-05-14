package com.felicita.servicios.impl;

import com.felicita.dto.UsuarioDTO;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServicioImpl implements UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepositorio.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        return convertirADTO(usuario);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con email: " + email));
        return convertirADTO(usuario);
    }
    
    @Override
    @Transactional
    public UsuarioDTO guardar(UsuarioDTO usuarioDTO) {
        if (usuarioRepositorio.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Usuario usuario = convertirAEntidad(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        Usuario guardado = usuarioRepositorio.save(usuario);
        return convertirADTO(guardado);
    }
    
    @Override
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setTelefono(usuarioDTO.getTelefono());
        
        // Solo actualizar contraseña si se proporciona
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }
        
        Usuario actualizado = usuarioRepositorio.save(usuario);
        return convertirADTO(actualizado);
    }
    
    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id);
        }
        usuarioRepositorio.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepositorio.existsByEmail(email);
    }
    
    // Métodos de conversión entre entidad y DTO
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setEstaActivo(usuario.isEstaActivo());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        dto.setUltimoLogin(usuario.getUltimoLogin());
        
        // Convertir roles a strings
        Set<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());
        dto.setRoles(roles);
        
        return dto;
    }
    
    private Usuario convertirAEntidad(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setEstaActivo(dto.isEstaActivo());
        
        return usuario;
    }
}