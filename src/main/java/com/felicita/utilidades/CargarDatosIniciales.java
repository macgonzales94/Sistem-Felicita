package com.felicita.utilidades;

import com.felicita.entidades.Rol;
import com.felicita.entidades.Usuario;
import com.felicita.repositorios.RolRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CargarDatosIniciales implements CommandLineRunner {

    @Autowired
    private RolRepositorio rolRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cargar roles si no existen
        cargarRoles();
        
        // Cargar usuario administrador si no existe
        cargarAdministrador();
    }
    
    private void cargarRoles() {
        // Verificar si ya existen roles
        if (rolRepositorio.count() == 0) {
            // Crear roles
            Rol rolAdmin = new Rol(ConstantesSeguridad.ROLE_ADMINISTRADOR, "Administrador del sistema");
            Rol rolProAdmin = new Rol(ConstantesSeguridad.ROLE_PROADMIN, "Administrador de establecimiento");
            Rol rolCliente = new Rol(ConstantesSeguridad.ROLE_CLIENTE, "Cliente del sistema");
            
            // Guardar roles
            rolRepositorio.save(rolAdmin);
            rolRepositorio.save(rolProAdmin);
            rolRepositorio.save(rolCliente);
            
            System.out.println("Roles creados correctamente");
        }
    }
    
    private void cargarAdministrador() {
        // Verificar si ya existe un administrador
        if (!usuarioRepositorio.existsByEmail("admin@felicita.com")) {
            // Buscar rol administrador
            Rol rolAdmin = rolRepositorio.findByNombre(ConstantesSeguridad.ROLE_ADMINISTRADOR)
                    .orElseThrow(() -> new RuntimeException("Rol ADMINISTRADOR no encontrado"));
            
            // Crear usuario administrador
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@felicita.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setTelefono("999999999");
            admin.setEstaActivo(true);
            admin.setFechaRegistro(LocalDateTime.now());
            
            // Asignar rol
            admin.agregarRol(rolAdmin);
            
            // Guardar administrador
            usuarioRepositorio.save(admin);
            
            System.out.println("Usuario administrador creado correctamente");
        }
    }
}