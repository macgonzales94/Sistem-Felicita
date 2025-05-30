package com.felicita.controladores;

import com.felicita.dto.EstablecimientoDTO;
import com.felicita.dto.ServicioDTO;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.excepciones.ServicioExcepcion;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ServicioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/proadmin/gestionar-servicios")
public class ServicioControlador {

    @Autowired
    private ServicioServicio servicioServicio;
    
    @Autowired
    private EstablecimientoServicio establecimientoServicio;

    /**
     * Listar servicios con selección de establecimiento
     */
    @GetMapping
    public String listarServicios(@RequestParam(required = false) Long establecimientoId,
                                 Model model) {
        try {
            // Obtener todos los establecimientos del ProAdmin
            List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
            
            if (establecimientos.isEmpty()) {
                model.addAttribute("error", "Primero debes crear un establecimiento antes de gestionar servicios");
                model.addAttribute("establecimientos", new ArrayList<>());
                model.addAttribute("servicios", new ArrayList<>());
                return "proadmin/servicios";
            }
            
            // Si no se especifica establecimiento, usar el primero
            if (establecimientoId == null && !establecimientos.isEmpty()) {
                establecimientoId = establecimientos.get(0).getId();
            }
            
            // Obtener servicios del establecimiento seleccionado
            List<ServicioDTO> servicios = new ArrayList<>();
            EstablecimientoDTO establecimientoSeleccionado = null;
            
            if (establecimientoId != null) {
                try {
                    establecimientoSeleccionado = establecimientoServicio.obtenerEstablecimientoPorId(establecimientoId);
                    servicios = servicioServicio.obtenerServiciosPorEstablecimiento(establecimientoId);
                } catch (Exception e) {
                    model.addAttribute("error", "Error al cargar servicios: " + e.getMessage());
                }
            }
            
            // Preparar categorías
            Set<String> categorias = prepararCategorias(establecimientoSeleccionado, servicios);
            
            model.addAttribute("establecimientos", establecimientos);
            model.addAttribute("establecimientoSeleccionado", establecimientoSeleccionado);
            model.addAttribute("servicios", servicios);
            model.addAttribute("categorias", categorias);
            model.addAttribute("establecimientoIdSeleccionado", establecimientoId);
            
            return "proadmin/servicios";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
            model.addAttribute("establecimientos", new ArrayList<>());
            model.addAttribute("servicios", new ArrayList<>());
            return "proadmin/servicios";
        }
    }
    
    /**
     * Crear nuevo servicio
     */
    @PostMapping("/nuevo")
    public String crearServicio(@Valid @ModelAttribute ServicioDTO servicioDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        }
        
        try {
            ServicioDTO servicioCreado = servicioServicio.crearServicio(servicioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio creado correctamente");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear servicio: " + e.getMessage());
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        }
    }
    
    /**
     * Actualizar servicio existente
     */
    @PostMapping("/{id}/editar")
    public String actualizarServicio(@PathVariable Long id,
                                    @Valid @ModelAttribute ServicioDTO servicioDTO,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error en los datos del formulario");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        }
        
        try {
            ServicioDTO servicioActualizado = servicioServicio.actualizarServicio(id, servicioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio actualizado correctamente");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar servicio: " + e.getMessage());
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        }
    }
    
    /**
     * Eliminar servicio
     */
    @PostMapping("/{id}/eliminar")
    public String eliminarServicio(@PathVariable Long id,
                                  @RequestParam Long establecimientoId,
                                  RedirectAttributes redirectAttributes) {
        try {
            servicioServicio.eliminarServicio(id);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar servicio: " + e.getMessage());
        }
        
        return "redirect:/proadmin/servicios?establecimientoId=" + establecimientoId;
    }
    
    /**
     * Subir imagen de servicio
     */
    @PostMapping("/{id}/imagen")
    public String subirImagenServicio(@PathVariable Long id,
                                     @RequestParam("imagen") MultipartFile imagen,
                                     @RequestParam Long establecimientoId,
                                     RedirectAttributes redirectAttributes) {
        try {
            String imageUrl = servicioServicio.subirImagenServicio(id, imagen);
            redirectAttributes.addFlashAttribute("mensaje", "Imagen subida correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir imagen: " + e.getMessage());
        }
        
        return "redirect:/proadmin/servicios?establecimientoId=" + establecimientoId;
    }
    
    // ===== API REST ENDPOINTS =====
    
    @GetMapping("/api/establecimiento/{establecimientoId}")
    @ResponseBody
    public ResponseEntity<List<ServicioDTO>> listarServiciosPorEstablecimientoApi(@PathVariable Long establecimientoId) {
        try {
            List<ServicioDTO> servicios = servicioServicio.obtenerServiciosPorEstablecimiento(establecimientoId);
            return ResponseEntity.ok(servicios);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ServicioDTO> obtenerServicioApi(@PathVariable Long id) {
        try {
            ServicioDTO servicio = servicioServicio.obtenerServicioPorId(id);
            return ResponseEntity.ok(servicio);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (ServicioExcepcion e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> crearServicioApi(@Valid @RequestBody ServicioDTO servicioDTO) {
        try {
            ServicioDTO nuevoServicio = servicioServicio.crearServicio(servicioDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoServicio);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarServicioApi(@PathVariable Long id, @Valid @RequestBody ServicioDTO servicioDTO) {
        try {
            ServicioDTO servicioActualizado = servicioServicio.actualizarServicio(id, servicioDTO);
            return ResponseEntity.ok(servicioActualizado);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarServicioApi(@PathVariable Long id) {
        try {
            servicioServicio.eliminarServicio(id);
            return ResponseEntity.ok(Map.of("mensaje", "Servicio eliminado correctamente"));
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Preparar categorías para el formulario
     */
    private Set<String> prepararCategorias(EstablecimientoDTO establecimiento, List<ServicioDTO> servicios) {
        Set<String> categorias = new HashSet<>();
        
        // Categorías predefinidas según tipo de establecimiento
        if (establecimiento != null) {
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
        }
        
        // Agregar categorías de servicios existentes
        servicios.forEach(servicio -> {
            if (servicio.getCategoria() != null && !servicio.getCategoria().isEmpty()) {
                categorias.add(servicio.getCategoria());
            }
        });
        
        // Agregar categoría "Otros"
        categorias.add("Otros");
        
        return categorias;
    }
}