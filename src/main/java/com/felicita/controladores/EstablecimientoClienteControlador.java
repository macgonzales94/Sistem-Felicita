package com.felicita.controladores;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import com.felicita.servicios.EstablecimientoClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente/establecimientos")
public class EstablecimientoClienteControlador {

    @Autowired
    private EstablecimientoClienteServicio establecimientoClienteServicio;

    @GetMapping
    public String listarEstablecimientos(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "12") int size,
                                       @RequestParam(required = false) String ciudad,
                                       @RequestParam(required = false) String tipo,
                                       @RequestParam(required = false) String busqueda,
                                       Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        
        // Aplicar filtros si existen
        List<EstablecimientoClienteDTO> establecimientos;
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorNombre(busqueda);
        } else if (ciudad != null && !ciudad.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorCiudad(ciudad);
        } else if (tipo != null && !tipo.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorTipo(tipo);
        } else {
            Page<EstablecimientoClienteDTO> establecimientosPaginados = 
                establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
            establecimientos = establecimientosPaginados.getContent();
            
            // Agregar información de paginación
            model.addAttribute("currentPage", establecimientosPaginados.getNumber());
            model.addAttribute("totalPages", establecimientosPaginados.getTotalPages());
            model.addAttribute("totalItems", establecimientosPaginados.getTotalElements());
        }
        
        // Obtener establecimientos mejor calificados para el sidebar
        List<EstablecimientoClienteDTO> mejorCalificados = 
            establecimientoClienteServicio.obtenerMejorCalificados(5);
        
        model.addAttribute("establecimientos", establecimientos);
        model.addAttribute("mejorCalificados", mejorCalificados);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("tipo", tipo);
        model.addAttribute("busqueda", busqueda);
        
        return "cliente/establecimientos";
    }

    @GetMapping("/{id}")
    public String detalleEstablecimiento(@PathVariable Long id, Model model) {
        try {
            EstablecimientoClienteDTO establecimiento = 
                establecimientoClienteServicio.obtenerDetalleEstablecimiento(id);
            
            List<ServicioClienteDTO> servicios = 
                establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(id);
            
            model.addAttribute("establecimiento", establecimiento);
            model.addAttribute("servicios", servicios);
            
            return "cliente/detalle-establecimiento";
        } catch (Exception e) {
            return "redirect:/cliente/establecimientos?error=establecimiento-no-encontrado";
        }
    }
    
    // API REST endpoints para acceso desde JavaScript
    
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<EstablecimientoClienteDTO>> listarEstablecimientosApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<EstablecimientoClienteDTO> establecimientos = 
            establecimientoClienteServicio.obtenerEstablecimientosActivos(pageable);
        
        return ResponseEntity.ok(establecimientos);
    }
    
    @GetMapping("/api/buscar")
    @ResponseBody
    public ResponseEntity<List<EstablecimientoClienteDTO>> buscarEstablecimientosApi(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo) {
        
        List<EstablecimientoClienteDTO> establecimientos;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorNombre(nombre);
        } else if (ciudad != null && !ciudad.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorCiudad(ciudad);
        } else if (tipo != null && !tipo.trim().isEmpty()) {
            establecimientos = establecimientoClienteServicio.buscarPorTipo(tipo);
        } else {
            // Devolver lista vacía si no hay parámetros
            return ResponseEntity.ok(List.of());
        }
        
        return ResponseEntity.ok(establecimientos);
    }
    
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<EstablecimientoClienteDTO> obtenerEstablecimientoApi(@PathVariable Long id) {
        try {
            EstablecimientoClienteDTO establecimiento = 
                establecimientoClienteServicio.obtenerDetalleEstablecimiento(id);
            return ResponseEntity.ok(establecimiento);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/api/{id}/servicios")
    @ResponseBody
    public ResponseEntity<List<ServicioClienteDTO>> obtenerServiciosApi(@PathVariable Long id) {
        try {
            List<ServicioClienteDTO> servicios = 
                establecimientoClienteServicio.obtenerServiciosDeEstablecimiento(id);
            return ResponseEntity.ok(servicios);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/api/{id}/horarios-disponibles")
    @ResponseBody
    public ResponseEntity<List<String>> obtenerHorariosDisponiblesApi(
            @PathVariable Long id,
            @RequestParam String fecha) {
        
        try {
            List<String> horarios = 
                establecimientoClienteServicio.obtenerHorariosDisponibles(id, fecha);
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/api/mejor-calificados")
    @ResponseBody
    public ResponseEntity<List<EstablecimientoClienteDTO>> obtenerMejorCalificadosApi(
            @RequestParam(defaultValue = "5") int limite) {
        
        List<EstablecimientoClienteDTO> establecimientos = 
            establecimientoClienteServicio.obtenerMejorCalificados(limite);
        
        return ResponseEntity.ok(establecimientos);
    }
    
    @GetMapping("/api/{id}/esta-abierto")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarSiEstaAbiertoApi(@PathVariable Long id) {
        try {
            boolean estaAbierto = establecimientoClienteServicio.estaAbierto(id);
            return ResponseEntity.ok(Map.of("estaAbierto", estaAbierto));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}