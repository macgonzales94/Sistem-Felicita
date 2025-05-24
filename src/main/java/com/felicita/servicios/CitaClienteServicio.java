package com.felicita.servicios;

import com.felicita.dto.CitaClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CitaClienteServicio {
    
    // Métodos existentes
    CitaClienteDTO crearCita(CitaClienteDTO citaDTO);
    CitaClienteDTO obtenerCitaPorId(Long id);
    Page<CitaClienteDTO> obtenerCitasCliente(Pageable pageable);
    List<CitaClienteDTO> obtenerCitasPendientes();
    List<CitaClienteDTO> obtenerHistorialCitas();
    CitaClienteDTO cancelarCita(Long id);
    CitaClienteDTO confirmarCita(Long id);
    CitaClienteDTO obtenerCitaPorCodigo(String codigo);
    boolean verificarDisponibilidad(CitaClienteDTO citaDTO);
    boolean puedeReservarCita();
    
    // NUEVOS MÉTODOS SUGERIDOS
    
    /**
     * Obtener citas por rango de fechas
     */
    List<CitaClienteDTO> obtenerCitasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtener próximas citas del cliente (próximos 7 días)
     */
    List<CitaClienteDTO> obtenerProximasCitas();
    
    /**
     * Obtener citas del día actual
     */
    List<CitaClienteDTO> obtenerCitasDelDia();
    
    /**
     * Obtener estadísticas del cliente
     */
    Map<String, Object> obtenerEstadisticasCliente();
    
    /**
     * Buscar citas por servicio o establecimiento
     */
    List<CitaClienteDTO> buscarCitas(String termino);
    
    /**
     * Verificar si el cliente puede cancelar una cita específica
     */
    boolean puedeCancelarCita(Long citaId);
    
    /**
     * Obtener horarios disponibles para un establecimiento en una fecha
     */
    List<String> obtenerHorariosDisponibles(Long establecimientoId, String fecha);
    
    /**
     * Reprogramar una cita existente
     */
    CitaClienteDTO reprogramarCita(Long citaId, LocalDateTime nuevaFechaHora);
    
    /**
     * Obtener citas agrupadas por estado
     */
    Map<String, List<CitaClienteDTO>> obtenerCitasAgrupadasPorEstado();
    
    /**
     * Verificar recordatorios de citas próximas
     */
    List<CitaClienteDTO> obtenerCitasConRecordatorio();
    
    /**
     * Calificar una cita completada
     */
    CitaClienteDTO calificarCita(Long citaId, int calificacion, String comentario);
}