package com.felicita.utilidades;

import com.felicita.entidades.Cliente;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Rol;
import com.felicita.entidades.Usuario;
import com.felicita.repositorios.ClienteRepositorio;
import com.felicita.repositorios.ProAdminRepositorio;
import com.felicita.repositorios.RolRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class CargarDatosIniciales implements CommandLineRunner {

    @Autowired
    private RolRepositorio rolRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    
    @Autowired
    private ProAdminRepositorio proAdminRepositorio;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Cargar roles si no existen
        cargarRoles();
        
        // Cargar usuario administrador si no existe
        cargarAdministrador();
        
        // Crear clientes faltantes
        crearClientesFaltantes();
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
    
    @Transactional
    private void crearClientesFaltantes() {
        System.out.println("Verificando usuarios sin cliente asociado...");
        
        // Obtener el rol CLIENTE
        Rol rolCliente = rolRepositorio.findByNombre(ConstantesSeguridad.ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));
        
        // Obtener todos los usuarios
        for (Usuario usuario : usuarioRepositorio.findAll()) {
            // Verificar si el usuario tiene el rol CLIENTE
            boolean esCliente = usuario.getRoles().stream()
                    .anyMatch(rol -> rolCliente.getNombre().equals(rol.getNombre()));
            
            if (esCliente) {
                // Verificar si ya existe un Cliente para este usuario
                Optional<Cliente> clienteOpt = clienteRepositorio.findByUsuario(usuario);
                
                if (clienteOpt.isEmpty()) {
                    // Crear un nuevo Cliente si no existe
                    Cliente cliente = new Cliente();
                    cliente.setUsuario(usuario);
                    clienteRepositorio.save(cliente);
                    System.out.println("Cliente creado para usuario: " + usuario.getEmail());
                }
            }
            
            // Verificar si el usuario tiene el rol PROADMIN
            boolean esProAdmin = usuario.getRoles().stream()
                    .anyMatch(rol -> ConstantesSeguridad.ROLE_PROADMIN.equals(rol.getNombre()));
            
            if (esProAdmin) {
                // Verificar si ya existe un ProAdmin para este usuario
                Optional<ProAdmin> proAdminOpt = proAdminRepositorio.findByUsuario(usuario);
                
                if (proAdminOpt.isEmpty()) {
                    // Crear un nuevo ProAdmin si no existe
                    ProAdmin proAdmin = new ProAdmin();
                    proAdmin.setUsuario(usuario);
                    proAdminRepositorio.save(proAdmin);
                    System.out.println("ProAdmin creado para usuario: " + usuario.getEmail());
                }
            }
        }
        
        System.out.println("Verificación de usuarios completada");
    }
}