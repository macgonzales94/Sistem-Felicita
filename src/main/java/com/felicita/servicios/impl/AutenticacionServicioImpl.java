package com.felicita.servicios.impl;

import com.felicita.configuracion.JwtTokenUtil;
import com.felicita.dto.JwtResponseDTO;
import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;
import com.felicita.entidades.Rol;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.UsuarioExcepcion;
import com.felicita.repositorios.RolRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.AutenticacionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AutenticacionServicioImpl implements AutenticacionServicio {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private RolRepositorio rolRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Autowired
    private UsuarioDetallesServicioImpl usuarioDetallesServicio;
    
    @Override
    public JwtResponseDTO autenticar(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        UserDetails userDetails = usuarioDetallesServicio.loadUserByUsername(loginDTO.getEmail());
        String jwt = jwtTokenUtil.generarToken(userDetails);
        
        // Actualizar último login
        Usuario usuario = usuarioRepositorio.findByEmail(loginDTO.getEmail()).orElseThrow();
        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
        
        // Extraer roles para la respuesta
        Set<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority().replace("ROLE_", ""))
            .collect(Collectors.toSet());
            
        return new JwtResponseDTO(
            jwt,
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            roles
        );
    }
    
    @Override
    @Transactional
    public String registrar(RegistroDTO registroDTO) {
        // Validar que las contraseñas coincidan
        if (!registroDTO.getPassword().equals(registroDTO.getConfirmarPassword())) {
            throw new UsuarioExcepcion("Las contraseñas no coinciden");
        }
        
        // Validar que el email no exista
        if (usuarioRepositorio.existsByEmail(registroDTO.getEmail())) {
            throw new UsuarioExcepcion("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.getNombre());
        usuario.setApellido(registroDTO.getApellido());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setTelefono(registroDTO.getTelefono());
        usuario.setEstaActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        
        // Asignar rol según el tipo de usuario
        Rol rol;
        if ("PROADMIN".equals(registroDTO.getTipoUsuario())) {
            rol = rolRepositorio.findByNombre("PROADMIN")
                .orElseThrow(() -> new UsuarioExcepcion("Rol PROADMIN no encontrado"));
        } else {
            rol = rolRepositorio.findByNombre("CLIENTE")
                .orElseThrow(() -> new UsuarioExcepcion("Rol CLIENTE no encontrado"));
        }
        
        usuario.agregarRol(rol);
        usuarioRepositorio.save(usuario);
        
        return "Usuario registrado exitosamente";
    }
}