package com.felicita.servicios.impl;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.GestionUsuariosDTO;
import com.felicita.entidades.Administrador;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Rol;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.excepciones.UsuarioExcepcion;
import com.felicita.repositorios.AdministradorRepositorio;
import com.felicita.repositorios.ClienteRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ProAdminRepositorio;
import com.felicita.repositorios.RolRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.AdministradorServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdministradorServicioImpl implements AdministradorServicio {

    @Autowired
    private AdministradorRepositorio administradorRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private RolRepositorio rolRepositorio;
    
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    
    @Autowired
    private ProAdminRepositorio proAdminRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Administrador registrarAdministrador(Usuario usuario) {
        // Verificar si ya existe un administrador para este usuario
        if (administradorRepositorio.existsByUsuarioId(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un administrador para este usuario");
        }
        
        // Crear nuevo administrador
        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setNivelAcceso(1); // Nivel básico por defecto
        administrador.setPuedeGestionarUsuarios(true);
        administrador.setPuedeGestionarEstablecimientos(true);
        administrador.setPuedeGestionarReportes(true);
        administrador.setPuedeGestionarConfiguracion(true);
        
        return administradorRepositorio.save(administrador);
    }

    @Override
    @Transactional(readOnly = true)
    public Administrador obtenerAdministradorPorUsuarioId(Long usuarioId) {
        return administradorRepositorio.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Administrador no encontrado para el usuario con ID: " + usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public Administrador obtenerAdministradorPorUsuarioEmail(String email) {
        return administradorRepositorio.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Administrador no encontrado para el email: " + email));
    }

    @Override
    @Transactional
    public Administrador actualizarAdministrador(Long id, Administrador administrador) {
        Administrador adminExistente = administradorRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Administrador no encontrado con ID: " + id));
        
        adminExistente.setNivelAcceso(administrador.getNivelAcceso());
        adminExistente.setDepartamento(administrador.getDepartamento());
        adminExistente.setCargo(administrador.getCargo());
        adminExistente.setPuedeGestionarUsuarios(administrador.isPuedeGestionarUsuarios());
        adminExistente.setPuedeGestionarEstablecimientos(administrador.isPuedeGestionarEstablecimientos());
        adminExistente.setPuedeGestionarReportes(administrador.isPuedeGestionarReportes());
        adminExistente.setPuedeGestionarConfiguracion(administrador.isPuedeGestionarConfiguracion());
        
        return administradorRepositorio.save(adminExistente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GestionUsuariosDTO> listarTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioRepositorio.findAll();
        return usuarios.stream()
                .map(this::convertirAGestionUsuariosDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GestionUsuariosDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        return convertirAGestionUsuariosDTO(usuario);
    }

    @Override
    @Transactional
    public GestionUsuariosDTO crearUsuario(GestionUsuariosDTO usuarioDTO) {
        // Verificar si el email ya existe
        if (usuarioRepositorio.existsByEmail(usuarioDTO.getEmail())) {
            throw new UsuarioExcepcion("El email ya está registrado: " + usuarioDTO.getEmail());
        }
        
        // Crear el usuario base
        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setEstaActivo(usuarioDTO.isEstaActivo());
        usuario.setFechaRegistro(LocalDateTime.now());
        
        // Asignar roles
        Set<Rol> roles = new HashSet<>();
        for (String rolNombre : usuarioDTO.getRoles()) {
            Rol rol = rolRepositorio.findByNombre(rolNombre)
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Rol no encontrado: " + rolNombre));
            roles.add(rol);
        }
        usuario.setRoles(roles);
        
        // Guardar el usuario
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);
        
        // Crear entidad específica según el rol
        if (usuarioDTO.getRoles().contains("ADMINISTRADOR")) {
            Administrador administrador = new Administrador();
            administrador.setUsuario(usuarioGuardado);
            administrador.setNivelAcceso(usuarioDTO.getNivelAcceso());
            administrador.setDepartamento(usuarioDTO.getDepartamento());
            administrador.setCargo(usuarioDTO.getCargo());
            administrador.setPuedeGestionarUsuarios(true);
            administrador.setPuedeGestionarEstablecimientos(true);
            administrador.setPuedeGestionarReportes(true);
            administrador.setPuedeGestionarConfiguracion(true);
            administradorRepositorio.save(administrador);
        } else if (usuarioDTO.getRoles().contains("CLIENTE")) {
            Cliente cliente = new Cliente();
            cliente.setUsuario(usuarioGuardado);
            cliente.setDireccion(usuarioDTO.getDireccion());
            cliente.setCiudad(usuarioDTO.getCiudad());
            cliente.setCodigoPostal(usuarioDTO.getCodigoPostal());
            clienteRepositorio.save(cliente);
        } else if (usuarioDTO.getRoles().contains("PROADMIN")) {
            ProAdmin proAdmin = new ProAdmin();
            proAdmin.setUsuario(usuarioGuardado);
            proAdmin.setDocumentoIdentidad(usuarioDTO.getDocumentoIdentidad());
            proAdmin.setPlanSubscripcion(usuarioDTO.getPlanSubscripcion());
            proAdminRepositorio.save(proAdmin);
        }
        
        return convertirAGestionUsuariosDTO(usuarioGuardado);
    }

    @Override
    @Transactional
    public GestionUsuariosDTO actualizarUsuario(Long id, GestionUsuariosDTO usuarioDTO) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        // Actualizar campos básicos
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellido(usuarioDTO.getApellido());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setEstaActivo(usuarioDTO.isEstaActivo());
        
        // Actualizar contraseña si se proporciona
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }
        
        // Actualizar roles si se proporcionan
        if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
            Set<Rol> roles = new HashSet<>();
            for (String rolNombre : usuarioDTO.getRoles()) {
                Rol rol = rolRepositorio.findByNombre(rolNombre)
                        .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Rol no encontrado: " + rolNombre));
                roles.add(rol);
            }
            usuario.setRoles(roles);
        }
        
        // Guardar usuario actualizado
        Usuario usuarioActualizado = usuarioRepositorio.save(usuario);
        
        // Actualizar entidad específica según el rol
        if (usuario.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ADMINISTRADOR"))) {
            administradorRepositorio.findByUsuario(usuario).ifPresent(admin -> {
                admin.setNivelAcceso(usuarioDTO.getNivelAcceso());
                admin.setDepartamento(usuarioDTO.getDepartamento());
                admin.setCargo(usuarioDTO.getCargo());
                administradorRepositorio.save(admin);
            });
        } else if (usuario.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("CLIENTE"))) {
            clienteRepositorio.findByUsuario(usuario).ifPresent(cliente -> {
                cliente.setDireccion(usuarioDTO.getDireccion());
                cliente.setCiudad(usuarioDTO.getCiudad());
                cliente.setCodigoPostal(usuarioDTO.getCodigoPostal());
                clienteRepositorio.save(cliente);
            });
        } else if (usuario.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("PROADMIN"))) {
            proAdminRepositorio.findByUsuario(usuario).ifPresent(proAdmin -> {
                proAdmin.setDocumentoIdentidad(usuarioDTO.getDocumentoIdentidad());
                proAdmin.setPlanSubscripcion(usuarioDTO.getPlanSubscripcion());
                proAdminRepositorio.save(proAdmin);
            });
        }
        
        return convertirAGestionUsuariosDTO(usuarioActualizado);
    }

    @Override
    @Transactional
    public void activarUsuario(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        usuario.setEstaActivo(true);
        usuarioRepositorio.save(usuario);
    }

    @Override
    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        usuario.setEstaActivo(false);
        usuarioRepositorio.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con ID: " + id));
        
        // Desactivar en lugar de eliminar para mantener integridad referencial
        usuario.setEstaActivo(false);
        usuarioRepositorio.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticasGenerales() {
        EstadisticasDTO estadisticas = new EstadisticasDTO();
        
        // Contar usuarios por tipo
        long totalUsuarios = usuarioRepositorio.count();
        long totalClientes = clienteRepositorio.count();
        long totalProAdmins = proAdminRepositorio.count();
        long totalAdministradores = administradorRepositorio.count();
        
        // Contar establecimientos
        long totalEstablecimientos = establecimientoRepositorio.count();
        
        // Agregar estadísticas
        estadisticas.agregarDatoAdicional("totalUsuarios", totalUsuarios);
        estadisticas.agregarDatoAdicional("totalClientes", totalClientes);
        estadisticas.agregarDatoAdicional("totalProAdmins", totalProAdmins);
        estadisticas.agregarDatoAdicional("totalAdministradores", totalAdministradores);
        estadisticas.agregarDatoAdicional("totalEstablecimientos", totalEstablecimientos);
        
        // Otros datos relevantes podrían agregarse aquí
        
        return estadisticas;
    }

    @Override
    @Transactional(readOnly = true)
    public Administrador obtenerAdministradorEntidadPorUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con email: " + email));
        
        return administradorRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Administrador no encontrado para el usuario: " + email));
    }
    
    // Método privado para convertir Usuario a GestionUsuariosDTO
    private GestionUsuariosDTO convertirAGestionUsuariosDTO(Usuario usuario) {
        GestionUsuariosDTO dto = new GestionUsuariosDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());
        dto.setEstaActivo(usuario.isEstaActivo());
        
        // Convertir roles
        Set<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .collect(Collectors.toSet());
        dto.setRoles(roles);
        
        // Intentar cargar datos específicos según el rol
        if (roles.contains("ADMINISTRADOR")) {
            administradorRepositorio.findByUsuario(usuario).ifPresent(admin -> {
                dto.setNivelAcceso(admin.getNivelAcceso());
                dto.setDepartamento(admin.getDepartamento());
                dto.setCargo(admin.getCargo());
            });
        } else if (roles.contains("CLIENTE")) {
            clienteRepositorio.findByUsuario(usuario).ifPresent(cliente -> {
                dto.setDireccion(cliente.getDireccion());
                dto.setCiudad(cliente.getCiudad());
                dto.setCodigoPostal(cliente.getCodigoPostal());
            });
        } else if (roles.contains("PROADMIN")) {
            proAdminRepositorio.findByUsuario(usuario).ifPresent(proAdmin -> {
                dto.setDocumentoIdentidad(proAdmin.getDocumentoIdentidad());
                dto.setPlanSubscripcion(proAdmin.getPlanSubscripcion());
            });
        }
        
        return dto;
    }
}