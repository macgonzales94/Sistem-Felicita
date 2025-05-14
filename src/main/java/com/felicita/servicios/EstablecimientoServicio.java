package com.felicita.servicios;

import com.felicita.dto.EstablecimientoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EstablecimientoServicio {
    EstablecimientoDTO crearEstablecimiento(EstablecimientoDTO establecimientoDTO);
    EstablecimientoDTO obtenerEstablecimientoPorId(Long id);
    List<EstablecimientoDTO> obtenerEstablecimientosProAdmin();
    EstablecimientoDTO actualizarEstablecimiento(Long id, EstablecimientoDTO establecimientoDTO);
    void eliminarEstablecimiento(Long id);
    boolean existeEstablecimiento(Long id);
    String subirImagenEstablecimiento(Long id, MultipartFile imagen);
    
    // Métodos específicos para barberias
    EstablecimientoDTO crearBarberia(EstablecimientoDTO barberiaDTO);
    EstablecimientoDTO actualizarBarberia(Long id, EstablecimientoDTO barberiaDTO);
    
    // Métodos específicos para salones de belleza
    EstablecimientoDTO crearSalonBelleza(EstablecimientoDTO salonDTO);
    EstablecimientoDTO actualizarSalonBelleza(Long id, EstablecimientoDTO salonDTO);
    
    // Métodos para gestionar características
    void agregarCaracteristica(Long id, String caracteristica);
    void eliminarCaracteristica(Long id, String caracteristica);
}