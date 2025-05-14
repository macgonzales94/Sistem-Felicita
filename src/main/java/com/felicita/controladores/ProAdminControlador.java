package com.felicita.controladores;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ProAdminDTO;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.servicios.ProAdminServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proadmin")
public class ProAdminControlador {

    @Autowired
    private ProAdminServicio proAdminServicio;

    // ✅ NUEVO: redirige /proadmin → /proadmin/dashboard
    @GetMapping
    public String redirigirDashboard() {
        return "redirect:/proadmin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            EstadisticasDTO estadisticas = proAdminServicio.obtenerEstadisticas();
            model.addAttribute("proAdmin", proAdmin);
            model.addAttribute("estadisticas", estadisticas);
            return "proadmin/dashboard";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @GetMapping("/establecimiento")
    public String perfilEstablecimiento(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            model.addAttribute("proAdmin", proAdmin);
            return "proadmin/perfil-establecimiento";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    /*
    @GetMapping("/servicios")
    public String servicios(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            model.addAttribute("proAdmin", proAdmin);
            return "proadmin/servicios";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }
    */

    @GetMapping("/citas")
    public String citas(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            model.addAttribute("proAdmin", proAdmin);
            return "proadmin/citas";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @GetMapping("/estadisticas")
    public String estadisticas(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            EstadisticasDTO estadisticas = proAdminServicio.obtenerEstadisticas();
            model.addAttribute("proAdmin", proAdmin);
            model.addAttribute("estadisticas", estadisticas);
            return "proadmin/estadisticas";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @GetMapping("/perfil")
    public String perfil(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            model.addAttribute("proAdmin", proAdmin);
            model.addAttribute("perfilForm", proAdmin);
            return "proadmin/perfil";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilForm") ProAdminDTO proAdminDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "proadmin/perfil";
        }

        try {
            ProAdminDTO proAdminActualizado = proAdminServicio.actualizarProAdmin(proAdminDTO.getId(), proAdminDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente");
            return "redirect:/proadmin/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/perfil";
        }
    }

    // API REST

    @GetMapping("/api/perfil")
    @ResponseBody
    public ResponseEntity<ProAdminDTO> obtenerPerfilApi() {
        ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
        return ResponseEntity.ok(proAdmin);
    }

    @GetMapping("/api/estadisticas")
    @ResponseBody
    public ResponseEntity<EstadisticasDTO> obtenerEstadisticasApi() {
        EstadisticasDTO estadisticas = proAdminServicio.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    @PutMapping("/api/perfil")
    @ResponseBody
    public ResponseEntity<ProAdminDTO> actualizarPerfilApi(@Valid @RequestBody ProAdminDTO proAdminDTO) {
        ProAdminDTO proAdminActualizado = proAdminServicio.actualizarProAdmin(proAdminDTO.getId(), proAdminDTO);
        return ResponseEntity.ok(proAdminActualizado);
    }
}
