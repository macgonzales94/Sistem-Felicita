package com.felicita.controladores;

import com.felicita.dto.EstablecimientoDTO;
import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ProAdminDTO;
import com.felicita.dto.ServicioDTO;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ProAdminServicio;
import com.felicita.servicios.ServicioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set; 

@Controller
@RequestMapping("/proadmin")
public class ProAdminControlador {

    @Autowired
    private ProAdminServicio proAdminServicio;
    
    @Autowired
    private EstablecimientoServicio establecimientoServicio;
    
    @Autowired
    private ServicioServicio servicioServicio;

    // Redirige /proadmin → /proadmin/dashboard
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

    // CORREGIDO: Ahora recibe el ID del establecimiento
    @GetMapping("/establecimiento/{id}")
    public String perfilEstablecimiento(@PathVariable Long id, Model model) {
        try {
            EstablecimientoDTO establecimiento = establecimientoServicio.obtenerEstablecimientoPorId(id);
            List<ServicioDTO> serviciosDestacados = servicioServicio.obtenerServiciosPorEstablecimiento(id);
            
            // Limitar a 5 servicios destacados
            if (serviciosDestacados.size() > 5) {
                serviciosDestacados = serviciosDestacados.subList(0, 5);
            }
            
            // Obtener estadísticas específicas del establecimiento
            EstadisticasDTO estadisticas = proAdminServicio.obtenerEstadisticasEstablecimiento(id);
            
            model.addAttribute("establecimiento", establecimiento);
            model.addAttribute("serviciosDestacados", serviciosDestacados);
            model.addAttribute("estadisticas", estadisticas);
            
            return "proadmin/perfil-establecimiento";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    // NUEVO: Redirige a la página de servicios con selección de establecimiento
    @GetMapping("/servicios")
    public String servicios(Model model) {
        try {
            List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
            
            // Crear un mapa de servicios por establecimiento
            Map<Long, List<ServicioDTO>> serviciosPorEstablecimiento = new HashMap<>();
            for (EstablecimientoDTO est : establecimientos) {
                List<ServicioDTO> servicios = servicioServicio.obtenerServiciosPorEstablecimiento(est.getId());
                serviciosPorEstablecimiento.put(est.getId(), servicios);
            }
            
            model.addAttribute("establecimientos", establecimientos);
            model.addAttribute("serviciosPorEstablecimiento", serviciosPorEstablecimiento);
            
            // Obtener categorías únicas
            Set<String> categorias = new HashSet<>();
            categorias.add("Cortes");
            categorias.add("Barba");
            categorias.add("Tratamientos");
            categorias.add("Coloración");
            categorias.add("Peinados");
            categorias.add("Otros");
            model.addAttribute("categorias", categorias);
            
            return "proadmin/servicios";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @GetMapping("/citas")
    public String citas(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
            
            model.addAttribute("proAdmin", proAdmin);
            model.addAttribute("establecimientos", establecimientos);
            
            return "proadmin/citas";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        }
    }

    @GetMapping("/calendario")
    public String calendario(Model model) {
        try {
            ProAdminDTO proAdmin = proAdminServicio.obtenerProAdminAutenticado();
            model.addAttribute("proAdmin", proAdmin);
            return "proadmin/calendario";
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