package com.felicita.controladores;

import com.felicita.dto.EstablecimientoDTO;
import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ServicioDTO;
import com.felicita.excepciones.EstablecimientoExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ProAdminServicio;
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

    @Autowired
    private ProAdminServicio proAdminServicio;

    @GetMapping
    public String listarEstablecimientos(Model model) {
        List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
        model.addAttribute("establecimientos", establecimientos);
        return "proadmin/establecimientos";
    }

    @GetMapping("/{id}")
    public String detalleEstablecimiento(@PathVariable Long id, Model model) {
        try {
            // Obtener el establecimiento
            EstablecimientoDTO establecimiento = establecimientoServicio.obtenerEstablecimientoPorId(id);
            model.addAttribute("establecimiento", establecimiento);

            // Obtener los servicios destacados (máximo 5)
            List<ServicioDTO> serviciosDestacados = servicioServicio.obtenerServiciosPorEstablecimiento(id);
            if (serviciosDestacados.size() > 5) {
                serviciosDestacados = serviciosDestacados.subList(0, 5);
            }
            model.addAttribute("serviciosDestacados", serviciosDestacados);

            // Obtener estadísticas específicas del establecimiento
            EstadisticasDTO estadisticas = proAdminServicio.obtenerEstadisticasEstablecimiento(id);
            model.addAttribute("estadisticas", estadisticas);

            return "proadmin/detalle-establecimiento";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (EstablecimientoExcepcion e) {
            return "redirect:/error/403";
        }
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

            // Agregar categorías predefinidas según el tipo de establecimiento
            if ("BARBERIA".equals(establecimiento.getTipoEstablecimiento())) {
                categorias.add("Corte de Cabello");
                categorias.add("Arreglo de Barba");
                categorias.add("Corte y Barba");
                categorias.add("Afeitado Clásico");
                categorias.add("Tratamientos Capilares");
                categorias.add("Servicios Faciales");
            } else if ("SALON_BELLEZA".equals(establecimiento.getTipoEstablecimiento())) {
                categorias.add("Corte y Peinado");
                categorias.add("Coloración");
                categorias.add("Tratamientos Capilares");
                categorias.add("Maquillaje");
                categorias.add("Manicure");
                categorias.add("Pedicure");
                categorias.add("Depilación");
                categorias.add("Tratamientos Faciales");
            }

            // Agregar categorías únicas de servicios existentes
            servicios.forEach(servicio -> {
                if (servicio.getCategoria() != null && !servicio.getCategoria().isEmpty()
                        && !categorias.contains(servicio.getCategoria())) {
                    categorias.add(servicio.getCategoria());
                }
            });

            // Agregar categoría "Otros" si no existe
            if (!categorias.contains("Otros")) {
                categorias.add("Otros");
            }

            model.addAttribute("categorias", categorias);

            // Para el formulario de nuevo servicio
            ServicioDTO servicioForm = new ServicioDTO();
            servicioForm.setEstablecimientoId(id);
            servicioForm.setEstaDisponible(true); // Por defecto activo
            model.addAttribute("servicioForm", servicioForm);

            return "proadmin/gestionar-servicios";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (EstablecimientoExcepcion e) {
            return "redirect:/error/403";
        }
    }

    // Agregar este método al EstablecimientoControlador.java

    @PostMapping("/{establecimientoId}/servicios")
    public String guardarServicio(@PathVariable Long establecimientoId,
            @Valid @ModelAttribute ServicioDTO servicioDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
            return "redirect:/proadmin/establecimientos/" + establecimientoId + "/servicios";
        }

        try {
            // Establecer el ID del establecimiento
            servicioDTO.setEstablecimientoId(establecimientoId);

            if (servicioDTO.getId() != null && servicioDTO.getId() > 0) {
                // Actualizar servicio existente
                servicioServicio.actualizarServicio(servicioDTO.getId(), servicioDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Servicio actualizado correctamente");
            } else {
                // Crear nuevo servicio
                servicioServicio.crearServicio(servicioDTO);
                redirectAttributes.addFlashAttribute("mensaje", "Servicio creado correctamente");
            }

            return "redirect:/proadmin/establecimientos/" + establecimientoId + "/servicios";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el servicio: " + e.getMessage());
            return "redirect:/proadmin/establecimientos/" + establecimientoId + "/servicios";
        }
    }

    @PostMapping("/{establecimientoId}/servicios/eliminar")
    public String eliminarServicio(@PathVariable Long establecimientoId,
            @RequestParam Long id,
            RedirectAttributes redirectAttributes) {
        try {
            servicioServicio.eliminarServicio(id);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el servicio: " + e.getMessage());
        }

        return "redirect:/proadmin/establecimientos/" + establecimientoId + "/servicios";
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