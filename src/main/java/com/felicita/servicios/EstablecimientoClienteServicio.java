package com.felicita.servicios;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EstablecimientoClienteServicio {
    
    /**
     * Obtener todos los establecimientos activos para mostrar a los clientes
     */
    Page<EstablecimientoClienteDTO> obtenerEstablecimientosActivos(Pageable pageable);
    
    /**
     * Buscar establecimientos por ciudad
     */
    List<EstablecimientoClienteDTO> buscarPorCiudad(String ciudad);
    
    /**
     * Buscar establecimientos por nombre
     */
    List<EstablecimientoClienteDTO> buscarPorNombre(String nombre);
    
    /**
     * Buscar establecimientos por tipo (BARBERIA o SALON_BELLEZA)
     */
    List<EstablecimientoClienteDTO> buscarPorTipo(String tipo);
    
    /**
     * Obtener detalle completo de un establecimiento para clientes
     */
    EstablecimientoClienteDTO obtenerDetalleEstablecimiento(Long id);
    
    /**
     * Obtener servicios de un establecimiento específico
     */
    List<ServicioClienteDTO> obtenerServiciosDeEstablecimiento(Long establecimientoId);
    
    /**
     * Obtener establecimientos cercanos (simulado por ciudad)
     */
    List<EstablecimientoClienteDTO> obtenerEstablecimientosCercanos(String ciudadCliente);
    
    /**
     * Obtener establecimientos mejor calificados
     */
    List<EstablecimientoClienteDTO> obtenerMejorCalificados(int limite);
    
    /**
     * Verificar si un establecimiento está abierto actualmente
     */
    boolean estaAbierto(Long establecimientoId);
    
    /**
     * Obtener horarios disponibles para un establecimiento en una fecha específica
     */
    List<String> obtenerHorariosDisponibles(Long establecimientoId, String fecha);
}