package com.felicita.controladores;

import com.felicita.dto.CitaClienteDTO;
import com.felicita.entidades.Cita;
import com.felicita.excepciones.CitaExcepcion;
import com.felicita.servicios.CitaClienteServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente/citas")
public class CitaClienteControlador {

    @Autowired
    private CitaClienteServicio citaClienteServicio;
    
    @GetMapping
    public String misCitas(Model model) {
        List<CitaClienteDTO> citasPendientes = citaClienteServicio.obtenerCitasPendientes();
        List<CitaClienteDTO> historialCitas = citaClienteServicio.obtenerHistorialCitas();
        
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("historialCitas", historialCitas);
        model.addAttribute("puedeReservar", citaClienteServicio.puedeReservarCita());
        
        return "cliente/mis-citas";
    }
    
    @GetMapping("/reservar")
    public String mostrarFormularioReserva(Model model) {
        model.addAttribute("citaForm", new CitaClienteDTO());
        model.addAttribute("puedeReservar", citaClienteServicio.puedeReservarCita());
        
        return "cliente/reservar-cita";
    }
    
    @PostMapping("/reservar")
    public String reservarCita(@Valid @ModelAttribute("citaForm") CitaClienteDTO citaDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/reservar-cita";
        }
        
        try {
            CitaClienteDTO citaCreada = citaClienteServicio.crearCita(citaDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Cita reservada correctamente. Código: " + citaCreada.getCodigoUnico());
            return "redirect:/cliente/citas";
        } catch (CitaExcepcion e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cliente/citas/reservar";
        }
    }
    
    @GetMapping("/{id}")
    public String detalleCita(@PathVariable Long id, Model model) {
        CitaClienteDTO cita = citaClienteServicio.obtenerCitaPorId(id);
        model.addAttribute("cita", cita);
        
        return "cliente/detalle-cita";
    }
    
    @PostMapping("/{id}/cancelar")
    public String cancelarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CitaClienteDTO citaCancelada = citaClienteServicio.cancelarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita cancelada correctamente");
        } catch (CitaExcepcion e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/citas";
    }
    
    @PostMapping("/{id}/confirmar")
    public String confirmarCita(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CitaClienteDTO citaConfirmada = citaClienteServicio.confirmarCita(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cita confirmada correctamente");
        } catch (CitaExcepcion e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/cliente/citas";
    }
    
    // API REST endpoints para acceso desde JavaScript
    
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<CitaClienteDTO>> listarCitasApi(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<CitaClienteDTO> citas = citaClienteServicio.obtenerCitasCliente(pageable);
        return ResponseEntity.ok(citas);
    }
    
    @GetMapping("/api/pendientes")
    @ResponseBody
    public ResponseEntity<List<CitaClienteDTO>> listarCitasPendientesApi() {
        List<CitaClienteDTO> citasPendientes = citaClienteServicio.obtenerCitasPendientes();
        return ResponseEntity.ok(citasPendientes);
    }
    
    @GetMapping("/api/historial")
    @ResponseBody
    public ResponseEntity<List<CitaClienteDTO>> listarHistorialCitasApi() {
        List<CitaClienteDTO> historialCitas = citaClienteServicio.obtenerHistorialCitas();
        return ResponseEntity.ok(historialCitas);
    }
    
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> crearCitaApi(@Valid @RequestBody CitaClienteDTO citaDTO) {
        try {
            CitaClienteDTO citaCreada = citaClienteServicio.crearCita(citaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
        } catch (CitaExcepcion e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarCitaApi(@PathVariable Long id) {
        try {
            CitaClienteDTO citaCancelada = citaClienteServicio.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (CitaExcepcion e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/api/{id}/confirmar")
    @ResponseBody
    public ResponseEntity<?> confirmarCitaApi(@PathVariable Long id) {
        try {
            CitaClienteDTO citaConfirmada = citaClienteServicio.confirmarCita(id);
            return ResponseEntity.ok(citaConfirmada);
        } catch (CitaExcepcion e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/api/verificar-disponibilidad")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verificarDisponibilidadApi(
            @RequestParam Long establecimientoId,
            @RequestParam Long servicioId,
            @RequestParam String fechaHora) {
        
        // Convertir fechaHora (formato ISO) a LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(fechaHora);
        
        CitaClienteDTO citaDTO = new CitaClienteDTO();
        citaDTO.setEstablecimientoId(establecimientoId);
        citaDTO.setServicioId(servicioId);
        citaDTO.setFechaHora(dateTime);
        
        boolean disponible = citaClienteServicio.verificarDisponibilidad(citaDTO);
        
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }
}