package com.felicita.controladores;

import com.felicita.dto.CitaProAdminDTO;
import com.felicita.dto.EstablecimientoDTO;
import com.felicita.entidades.Cita;
import com.felicita.servicios.CitaProAdminServicio;
import com.felicita.servicios.EstablecimientoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proadmin/citas")
public class CitaProAdminControlador {

    @Autowired
    private CitaProAdminServicio citaProAdminServicio;
    
    @Autowired
    private EstablecimientoServicio establecimientoServicio;

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<CitaProAdminDTO>> obtenerCitas(
            @RequestParam(required = false) Long establecimientoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fecha) {
        
        List<CitaProAdminDTO> citas = citaProAdminServicio.obtenerCitasProAdmin(establecimientoId, estado, fecha);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/api/proximas")
    @ResponseBody
    public ResponseEntity<List<CitaProAdminDTO>> obtenerProximasCitas() {
        List<CitaProAdminDTO> citas = citaProAdminServicio.obtenerProximasCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<CitaProAdminDTO> obtenerCitaPorId(@PathVariable Long id) {
        CitaProAdminDTO cita = citaProAdminServicio.obtenerCitaPorId(id);
        return ResponseEntity.ok(cita);
    }

    @PostMapping("/api/{id}/confirmar")
    @ResponseBody
    public ResponseEntity<?> confirmarCita(@PathVariable Long id) {
        try {
            CitaProAdminDTO citaConfirmada = citaProAdminServicio.confirmarCita(id);
            return ResponseEntity.ok(citaConfirmada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/completar")
    @ResponseBody
    public ResponseEntity<?> completarCita(@PathVariable Long id) {
        try {
            CitaProAdminDTO citaCompletada = citaProAdminServicio.completarCita(id);
            return ResponseEntity.ok(citaCompletada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/cancelar")
    @ResponseBody
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            CitaProAdminDTO citaCancelada = citaProAdminServicio.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/{id}/no-asistio")
    @ResponseBody
    public ResponseEntity<?> marcarNoAsistio(@PathVariable Long id) {
        try {
            CitaProAdminDTO citaActualizada = citaProAdminServicio.marcarNoAsistio(id);
            return ResponseEntity.ok(citaActualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/estadisticas/hoy")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasHoy() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        List<EstablecimientoDTO> establecimientos = establecimientoServicio.obtenerEstablecimientosProAdmin();
        long totalCitasHoy = 0;
        long citasPendientes = 0;
        long citasCompletadas = 0;
        
        for (EstablecimientoDTO est : establecimientos) {
            List<CitaProAdminDTO> citasEstablecimiento = citaProAdminServicio.obtenerCitasHoyPorEstablecimiento(est.getId());
            totalCitasHoy += citasEstablecimiento.size();
            
            for (CitaProAdminDTO cita : citasEstablecimiento) {
                if (cita.getEstado() == Cita.EstadoCita.PENDIENTE || cita.getEstado() == Cita.EstadoCita.CONFIRMADA) {
                    citasPendientes++;
                } else if (cita.getEstado() == Cita.EstadoCita.COMPLETADA) {
                    citasCompletadas++;
                }
            }
        }
        
        estadisticas.put("totalCitasHoy", totalCitasHoy);
        estadisticas.put("citasPendientes", citasPendientes);
        estadisticas.put("citasCompletadas", citasCompletadas);
        estadisticas.put("porcentajeCompletado", totalCitasHoy > 0 ? (citasCompletadas * 100.0 / totalCitasHoy) : 0);
        
        return ResponseEntity.ok(estadisticas);
    }
}