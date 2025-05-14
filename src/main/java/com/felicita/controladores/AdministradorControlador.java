package com.felicita.controladores;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.GestionUsuariosDTO;
import com.felicita.entidades.Administrador;
import com.felicita.servicios.AdministradorServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdministradorControlador {

    @Autowired
    private AdministradorServicio administradorServicio;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        EstadisticasDTO estadisticas = administradorServicio.obtenerEstadisticasGenerales();
        model.addAttribute("estadisticas", estadisticas);
        return "admin/dashboard";
    }
    
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        List<GestionUsuariosDTO> usuarios = administradorServicio.listarTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
    
    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioForm(Model model) {
        model.addAttribute("usuarioForm", new GestionUsuariosDTO());
        model.addAttribute("modo", "crear");
        return "admin/gestionar-usuario";
    }
    
    @PostMapping("/usuarios/nuevo")
    public String crearUsuario(@Valid @ModelAttribute("usuarioForm") GestionUsuariosDTO usuarioDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/gestionar-usuario";
        }
        
        try {
            GestionUsuariosDTO nuevoUsuario = administradorServicio.crearUsuario(usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios/nuevo";
        }
    }
    
    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuarioForm(@PathVariable Long id, Model model) {
        GestionUsuariosDTO usuario = administradorServicio.obtenerUsuarioPorId(id);
        model.addAttribute("usuarioForm", usuario);
        model.addAttribute("modo", "editar");
        return "admin/gestionar-usuario";
    }
    
    @PostMapping("/usuarios/{id}/editar")
    public String actualizarUsuario(@PathVariable Long id,
                                   @Valid @ModelAttribute("usuarioForm") GestionUsuariosDTO usuarioDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/gestionar-usuario";
        }
        
        try {
            GestionUsuariosDTO usuarioActualizado = administradorServicio.actualizarUsuario(id, usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios/" + id + "/editar";
        }
    }
    
    @PostMapping("/usuarios/{id}/activar")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            administradorServicio.activarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario activado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
    
    @PostMapping("/usuarios/{id}/desactivar")
    public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            administradorServicio.desactivarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario desactivado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
    
    @GetMapping("/establecimientos")
    public String establecimientos(Model model) {
        return "admin/establecimientos";
    }
    
    @GetMapping("/reportes")
    public String reportes(Model model) {
        return "admin/reportes";
    }
    
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        return "admin/configuracion";
    }
    
    // API REST endpoints para acceso desde JavaScript
    
    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticasApi() {
        EstadisticasDTO estadisticas = administradorServicio.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(estadisticas);
    }
    
    @GetMapping("/api/usuarios")
    @ResponseBody
    public ResponseEntity<List<GestionUsuariosDTO>> listarUsuariosApi() {
        List<GestionUsuariosDTO> usuarios = administradorServicio.listarTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    @GetMapping("/api/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<GestionUsuariosDTO> obtenerUsuarioApi(@PathVariable Long id) {
        GestionUsuariosDTO usuario = administradorServicio.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }
    
    @PostMapping("/api/usuarios")
    @ResponseBody
    public ResponseEntity<?> crearUsuarioApi(@Valid @RequestBody GestionUsuariosDTO usuarioDTO) {
        try {
            GestionUsuariosDTO nuevoUsuario = administradorServicio.crearUsuario(usuarioDTO);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarUsuarioApi(@PathVariable Long id, 
                                                @Valid @RequestBody GestionUsuariosDTO usuarioDTO) {
        try {
            GestionUsuariosDTO usuarioActualizado = administradorServicio.actualizarUsuario(id, usuarioDTO);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/usuarios/{id}/activar")
    @ResponseBody
    public ResponseEntity<?> activarUsuarioApi(@PathVariable Long id) {
        try {
            administradorServicio.activarUsuario(id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario activado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/usuarios/{id}/desactivar")
    @ResponseBody
    public ResponseEntity<?> desactivarUsuarioApi(@PathVariable Long id) {
        try {
            administradorServicio.desactivarUsuario(id);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario desactivado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}