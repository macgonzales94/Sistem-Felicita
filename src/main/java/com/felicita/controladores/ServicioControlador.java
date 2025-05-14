package com.felicita.controladores;

import com.felicita.dto.ServicioDTO;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.excepciones.ServicioExcepcion;
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

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proadmin/servicios")
public class ServicioControlador {

    @Autowired
    private ServicioServicio servicioServicio;

    @GetMapping
    public String listarServicios(@RequestParam(required = false) Long establecimientoId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        
        if (establecimientoId == null) {
            return "redirect:/proadmin/establecimientos";
        }
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
            Page<ServicioDTO> serviciosPaginados = servicioServicio.obtenerServiciosPorEstablecimientoPaginados(establecimientoId, pageable);
            
            model.addAttribute("servicios", serviciosPaginados.getContent());
            model.addAttribute("currentPage", serviciosPaginados.getNumber());
            model.addAttribute("totalItems", serviciosPaginados.getTotalElements());
            model.addAttribute("totalPages", serviciosPaginados.getTotalPages());
            model.addAttribute("establecimientoId", establecimientoId);
            
            return "proadmin/servicios";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "proadmin/servicios";
        }
    }
    
    @GetMapping("/{id}")
    public String verServicio(@PathVariable Long id, Model model) {
        try {
            ServicioDTO servicio = servicioServicio.obtenerServicioPorId(id);
            model.addAttribute("servicio", servicio);
            return "proadmin/detalle-servicio";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (ServicioExcepcion e) {
            return "redirect:/error/403";
        }
    }
    
    @GetMapping("/nuevo")
    public String nuevoServicioForm(@RequestParam Long establecimientoId, Model model) {
        ServicioDTO servicioForm = new ServicioDTO();
        servicioForm.setEstablecimientoId(establecimientoId);
        
        model.addAttribute("servicioForm", servicioForm);
        model.addAttribute("esModo", "crear");
        model.addAttribute("establecimientoId", establecimientoId);
        
        return "proadmin/gestionar-servicios";
    }
    
    @PostMapping("/nuevo")
    public String crearServicio(@Valid @ModelAttribute("servicioForm") ServicioDTO servicioDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "proadmin/gestionar-servicios";
        }
        
        try {
            ServicioDTO nuevoServicio = servicioServicio.crearServicio(servicioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio creado correctamente");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/servicios/nuevo?establecimientoId=" + servicioDTO.getEstablecimientoId();
        }
    }
    
    @GetMapping("/{id}/editar")
    public String editarServicioForm(@PathVariable Long id, Model model) {
        try {
            ServicioDTO servicio = servicioServicio.obtenerServicioPorId(id);
            
            model.addAttribute("servicioForm", servicio);
            model.addAttribute("esModo", "editar");
            model.addAttribute("establecimientoId", servicio.getEstablecimientoId());
            
            return "proadmin/gestionar-servicios";
        } catch (RecursoNoEncontradoExcepcion e) {
            return "redirect:/error/404";
        } catch (ServicioExcepcion e) {
            return "redirect:/error/403";
        }
    }
    
    @PostMapping("/{id}/editar")
    public String actualizarServicio(@PathVariable Long id,
                                    @Valid @ModelAttribute("servicioForm") ServicioDTO servicioDTO,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "proadmin/gestionar-servicios";
        }
        
        try {
            ServicioDTO servicioActualizado = servicioServicio.actualizarServicio(id, servicioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Servicio actualizado correctamente");
            return "redirect:/proadmin/servicios?establecimientoId=" + servicioDTO.getEstablecimientoId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/servicios/" + id + "/editar";
        }
    }
    
    @PostMapping("/{id}/imagen")
    public String subirImagen(@PathVariable Long id,
                             @RequestParam("imagen") MultipartFile imagen,
                             RedirectAttributes redirectAttributes) {
        try {
            ServicioDTO servicio = servicioServicio.obtenerServicioPorId(id);
            String imageUrl = servicioServicio.subirImagenServicio(id, imagen);
            
            redirectAttributes.addFlashAttribute("mensaje", "Imagen subida correctamente");
            return "redirect:/proadmin/servicios/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proadmin/servicios/" + id;
        }
    }
    
    // API REST endpoints para acceso desde JavaScript
    
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
    
    @GetMapping("/api/disponibles/{establecimientoId}")
    @ResponseBody
    public ResponseEntity<List<ServicioDTO>> listarServiciosDisponiblesApi(@PathVariable Long establecimientoId) {
        try {
            List<ServicioDTO> servicios = servicioServicio.obtenerServiciosDisponibles(establecimientoId);
            return ResponseEntity.ok(servicios);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/api/categoria/{establecimientoId}")
    @ResponseBody
    public ResponseEntity<List<ServicioDTO>> listarServiciosPorCategoriaApi(
            @PathVariable Long establecimientoId,
            @RequestParam String categoria) {
        try {
            List<ServicioDTO> servicios = servicioServicio.obtenerServiciosPorCategoria(establecimientoId, categoria);
            return ResponseEntity.ok(servicios);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/api/precio/{establecimientoId}")
    @ResponseBody
    public ResponseEntity<List<ServicioDTO>> listarServiciosPorRangoPrecioApi(
            @PathVariable Long establecimientoId,
            @RequestParam double min,
            @RequestParam double max) {
        try {
            List<ServicioDTO> servicios = servicioServicio.obtenerServiciosPorRangoPrecio(establecimientoId, min, max);
            return ResponseEntity.ok(servicios);
        } catch (RecursoNoEncontradoExcepcion e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}