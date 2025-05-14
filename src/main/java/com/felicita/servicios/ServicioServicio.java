package com.felicita.servicios;

import com.felicita.dto.ServicioDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ServicioServicio {
    ServicioDTO crearServicio(ServicioDTO servicioDTO);
    ServicioDTO obtenerServicioPorId(Long id);
    List<ServicioDTO> obtenerServiciosPorEstablecimiento(Long establecimientoId);
    Page<ServicioDTO> obtenerServiciosPorEstablecimientoPaginados(Long establecimientoId, Pageable pageable);
    ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO);
    void eliminarServicio(Long id);
    boolean existeServicio(Long id);
    String subirImagenServicio(Long id, MultipartFile imagen);
    
    // Métodos adicionales para filtrar servicios
    List<ServicioDTO> obtenerServiciosPorCategoria(Long establecimientoId, String categoria);
    List<ServicioDTO> obtenerServiciosDisponibles(Long establecimientoId);
    List<ServicioDTO> obtenerServiciosPorRangoPrecio(Long establecimientoId, double precioMin, double precioMax);
}