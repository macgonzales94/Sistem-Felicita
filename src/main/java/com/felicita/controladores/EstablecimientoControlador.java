package com.felicita.controladores;

import com.felicita.dto.EstablecimientoDTO;
import com.felicita.dto.ServicioDTO;
import com.felicita.excepciones.EstablecimientoExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ServicioServicio;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proadmin/establecimientos")
public class EstablecimientoControlador {

    @Autowired
    private EstablecimientoServicio establecimientoServicio;

    @Autowired
    private ServicioServicio servicioServicio;

    @GetMapping
    public String listarEstablecimientos(Model model) {
        List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
        model.addAttribute("establecimientos", establecimientos);
        return "proadmin/establecimientos";
    }

    /**
     * Método para mostrar los servicios de un establecimiento específico
     */
    @GetMapping("/{id}/servicios")
    public String gestionarServicios(@PathVariable Long id, Model model) {
        try {
            // Obtener el establecimiento
            EstablecimientoDTO establecimiento = establecimientoServicio.obtenerEstablecimientoPorId(id);
            model.addAttribute("establecimiento", establecimiento);

            // Obtener los servicios del establecimiento
            List<ServicioDTO> servicios = servicioServicio.obtenerServiciosPorEstablecimiento(id);
            model.addAttribute("servicios", servicios);

            // Preparar categorías disponibles para el filtro
            List<String> categorias = new ArrayList<>();
            // Extraer categorías únicas de los servicios
            servicios.forEach(servicio -> {
                if (servicio.getCategoria() != null && !servicio.getCategoria().isEmpty()
                        && !categorias.contains(servicio.getCategoria())) {
                    categorias.add(servicio.getCategoria());
                }
            });
            model.addAttribute("categorias", categorias);

            // Para el formulario de nuevo servicio
            model.addAttribute("servicioForm", new ServicioDTO());

            return "proadmin/gestionar-servicios";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (EstablecimientoExcepcion e) {
            return "redirect:/error/403";
        }
    }

    @GetMapping("/nuevo")
    public String nuevoEstablecimientoForm(Model model) {
        model.addAttribute("establecimientoForm", new EstablecimientoDTO());
        model.addAttribute("esModo", "crear");
        return "proadmin/editar-establecimiento";
    }

    @PostMapping("/nuevo")
    public String crearEstablecimiento(
            @Valid @ModelAttribute("establecimientoForm") EstablecimientoDTO establecimientoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "proadmin/editar-establecimiento";
        }

        try {
            EstablecimientoDTO nuevoEstablecimiento = establecimientoServicio.crearEstablecimiento(establecimientoDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Establecimiento creado correctamente");
            return "redirect:/proadmin/establecimientos/" + nuevoEstablecimiento.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/establecimientos/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarEstablecimientoForm(@PathVariable Long id, Model model) {
        try {
            EstablecimientoDTO establecimiento = establecimientoServicio.obtenerEstablecimientoPorId(id);
            model.addAttribute("establecimientoForm", establecimiento);
            model.addAttribute("esModo", "editar");
            return "proadmin/editar-establecimiento";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (EstablecimientoExcepcion e) {
            return "redirect:/error/403";
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizarEstablecimiento(@PathVariable Long id,
            @Valid @ModelAttribute("establecimientoForm") EstablecimientoDTO establecimientoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "proadmin/editar-establecimiento";
        }

        try {
            EstablecimientoDTO establecimientoActualizado = establecimientoServicio.actualizarEstablecimiento(id,
                    establecimientoDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Establecimiento actualizado correctamente");
            return "redirect:/proadmin/establecimientos/" + establecimientoActualizado.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/establecimientos/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/imagen")
    public String subirImagen(@PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen,
            RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = establecimientoServicio.subirImagenEstablecimiento(id, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Imagen subida correctamente");
            return "redirect:/proadmin/establecimientos/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/establecimientos/" + id;
        }
    }

    // API REST endpoints para acceso desde JavaScript

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<EstablecimientoDTO>> listarEstablecimientosApi() {
        List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
        return ResponseEntity.ok(establecimientos);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<EstablecimientoDTO> obtenerEstablecimientoApi(@PathVariable Long id) {
        try {
            EstablecimientoDTO establecimiento = establecimientoServicio.obtenerEstablecimientoPorId(id);
            return ResponseEntity.ok(establecimiento);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (EstablecimientoExcepcion e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> crearEstablecimientoApi(@Valid @RequestBody EstablecimientoDTO establecimientoDTO) {
        try {
            EstablecimientoDTO nuevoEstablecimiento = establecimientoServicio.crearEstablecimiento(establecimientoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEstablecimiento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEstablecimientoApi(@PathVariable Long id) {
        try {
            establecimientoServicio.eliminarEstablecimiento(id);
            return ResponseEntity.ok(Map.of("mensaje", "Establecimiento eliminado correctamente"));
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/caracteristicas")
    @ResponseBody
    public ResponseEntity<?> agregarCaracteristicaApi(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String caracteristica = payload.get("caracteristica");
            if (caracteristica == null || caracteristica.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La característica no puede estar vacía"));
            }

            establecimientoServicio.agregarCaracteristica(id, caracteristica);
            return ResponseEntity.ok(Map.of("mensaje", "Característica agregada correctamente"));
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/{id}/caracteristicas/{caracteristica}")
    @ResponseBody
    public ResponseEntity<?> eliminarCaracteristicaApi(@PathVariable Long id, @PathVariable String caracteristica) {
        try {
            establecimientoServicio.eliminarCaracteristica(id, caracteristica);
            return ResponseEntity.ok(Map.of("mensaje", "Característica eliminada correctamente"));
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}