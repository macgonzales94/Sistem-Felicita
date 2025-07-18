package com.felicita.controladores;

import com.felicita.dto.JwtResponseDTO;
import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;
import com.felicita.excepciones.UsuarioExcepcion;
import com.felicita.servicios.AutenticacionServicio;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutenticacionControladorThymeleaf {

    @Autowired
    private AutenticacionServicio autenticacionServicio;

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "logout", required = false) String logout,
                              Model modelo) {
        if (error != null) {
            modelo.addAttribute("mensajeError", "Credenciales incorrectas. Por favor intenta de nuevo.");
        }
        if (logout != null) {
            modelo.addAttribute("mensajeInfo", "Has cerrado sesión correctamente.");
        }
        modelo.addAttribute("loginDTO", new LoginDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                               BindingResult resultado,
                               RedirectAttributes atributosRedireccion,
                               HttpServletResponse respuesta) {
        
        if (resultado.hasErrors()) {
            return "auth/login";
        }
        
        try {
            // Autenticar y obtener el token JWT
            JwtResponseDTO respuestaJwt = autenticacionServicio.autenticar(loginDTO);
            
            // Crear cookie JWT con configuración mejorada
            Cookie cookieJwt = crearCookieJwtSegura(respuestaJwt.getToken());
            respuesta.addCookie(cookieJwt);
            
            // Log para debugging
            System.out.println("=== LOGIN EXITOSO ===");
            System.out.println("Usuario: " + respuestaJwt.getEmail());
            System.out.println("Roles: " + respuestaJwt.getRoles());
            System.out.println("Token creado y guardado en cookie");
            
            // Redireccionar según el rol del usuario
            return redirigirSegunRol(respuestaJwt.getRoles());
            
        } catch (Exception e) {
            atributosRedireccion.addFlashAttribute("mensajeError", 
                "Credenciales incorrectas. Por favor intenta de nuevo.");
            return "redirect:/login?error";
        }
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model modelo) {
        modelo.addAttribute("registroDTO", new RegistroDTO());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroDTO registroDTO,
                                  BindingResult resultado,
                                  RedirectAttributes atributosRedireccion,
                                  HttpServletResponse respuesta) {
        
        // Validar que las contraseñas coincidan
        if (!registroDTO.getPassword().equals(registroDTO.getConfirmarPassword())) {
            resultado.rejectValue("confirmarPassword", "error.registroDTO", "Las contraseñas no coinciden");
        }

        if (resultado.hasErrors()) {
            return "auth/registro";
        }

        try {
            // Registrar el usuario
            autenticacionServicio.registrar(registroDTO);
            
            // Autenticar automáticamente después del registro
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setEmail(registroDTO.getEmail());
            loginDTO.setPassword(registroDTO.getPassword());
            
            JwtResponseDTO respuestaJwt = autenticacionServicio.autenticar(loginDTO);
            
            // Crear cookie JWT
            Cookie cookieJwt = crearCookieJwtSegura(respuestaJwt.getToken());
            respuesta.addCookie(cookieJwt);
            
            // Log para debugging
            System.out.println("=== REGISTRO Y LOGIN AUTOMÁTICO ===");
            System.out.println("Usuario: " + respuestaJwt.getEmail());
            System.out.println("Roles: " + respuestaJwt.getRoles());
            
            return redirigirSegunRol(respuestaJwt.getRoles());
            
        } catch (UsuarioExcepcion e) {
            atributosRedireccion.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/registro";
        } catch (Exception e) {
            atributosRedireccion.addFlashAttribute("mensajeError", 
                "Error durante el registro. Por favor intenta de nuevo.");
            return "redirect:/registro";
        }
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }

    @GetMapping("/redirect-dashboard")
    public String redirigirDashboard() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        
        if (autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        } else if (autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROADMIN"))) {
            return "redirect:/proadmin/dashboard";
        } else if (autenticacion.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
            return "redirect:/cliente/inicio";
        } else {
            return "redirect:/";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletResponse respuesta) {
        // Eliminar la cookie JWT
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Cambiar a true en producción con HTTPS
        respuesta.addCookie(cookie);
        
        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();
        
        System.out.println("=== LOGOUT ===");
        System.out.println("Cookie JWT eliminada y contexto limpiado");
        
        return "redirect:/login?logout";
    }
    
    /**
     * Crear cookie JWT con configuración de seguridad mejorada
     */
    private Cookie crearCookieJwtSegura(String token) {
        Cookie cookieJwt = new Cookie("jwtToken", token);
        cookieJwt.setPath("/");                    // Disponible en toda la aplicación
        cookieJwt.setHttpOnly(true);              // No accesible via JavaScript (seguridad)
        cookieJwt.setMaxAge(86400);               // 1 día en segundos
        cookieJwt.setSecure(false);               // true para HTTPS en producción
        // cookieJwt.setDomain("localhost");      // Especificar dominio si es necesario
        
        return cookieJwt;
    }
    
    /**
     * Redireccionar según los roles del usuario
     */
    private String redirigirSegunRol(java.util.Set<String> roles) {
        if (roles.contains("ADMINISTRADOR")) {
            return "redirect:/admin/dashboard";
        } else if (roles.contains("PROADMIN")) {
            return "redirect:/proadmin/dashboard";
        } else if (roles.contains("CLIENTE")) {
            return "redirect:/cliente/inicio";
        } else {
            return "redirect:/";
        }
    }
}