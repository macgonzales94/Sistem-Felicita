package com.felicita.servicios.impl;

import com.felicita.dto.ServicioDTO;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.Servicio;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.excepciones.ServicioExcepcion;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ServicioRepositorio;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ServicioServicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicioServicioImpl implements ServicioServicio {

    @Autowired
    private ServicioRepositorio servicioRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;

    @Value("${app.upload.dir:${user.home}/uploads/felicita}")
    private String uploadDir;
    
    @Override
    @Transactional
    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        // Verificar que el establecimiento existe
        Establecimiento establecimiento = establecimientoRepositorio.findById(servicioDTO.getEstablecimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + servicioDTO.getEstablecimientoId()));
        
        // Crear nuevo servicio
        Servicio servicio = new Servicio();
        servicio.setNombre(servicioDTO.getNombre());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setPrecio(servicioDTO.getPrecio());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
        servicio.setCategoria(servicioDTO.getCategoria());
        servicio.setImagenUrl(servicioDTO.getImagenUrl());
        servicio.setEstaDisponible(servicioDTO.isEstaDisponible());
        servicio.setEstablecimiento(establecimiento);
        
        Servicio servicioGuardado = servicioRepositorio.save(servicio);
        
        return convertirADTO(servicioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioDTO obtenerServicioPorId(Long id) {
        Servicio servicio = servicioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
        
        return convertirADTO(servicio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosPorEstablecimiento(Long establecimientoId) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(establecimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + establecimientoId));
        
        List<Servicio> servicios = servicioRepositorio.findByEstablecimiento(establecimiento);
        
        return servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioDTO> obtenerServiciosPorEstablecimientoPaginados(Long establecimientoId, Pageable pageable) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(establecimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + establecimientoId));
        
        Page<Servicio> servicios = servicioRepositorio.findByEstablecimiento(establecimiento, pageable);
        
        return servicios.map(this::convertirADTO);
    }

    @Override
    @Transactional
    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        Servicio servicio = servicioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
        
        // Verificar que el servicio pertenece al establecimiento correcto
        if (!servicio.getEstablecimiento().getId().equals(servicioDTO.getEstablecimientoId())) {
            throw new ServicioExcepcion("El servicio no pertenece al establecimiento especificado");
        }
        
        // Actualizar campos
        servicio.setNombre(servicioDTO.getNombre());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setPrecio(servicioDTO.getPrecio());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
        servicio.setCategoria(servicioDTO.getCategoria());
        servicio.setEstaDisponible(servicioDTO.isEstaDisponible());
        
        Servicio servicioActualizado = servicioRepositorio.save(servicio);
        
        return convertirADTO(servicioActualizado);
    }

    @Override
    @Transactional
    public void eliminarServicio(Long id) {
        if (!servicioRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id);
        }
        
        servicioRepositorio.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeServicio(Long id) {
        return servicioRepositorio.existsById(id);
    }

    @Override
    @Transactional
    public String subirImagenServicio(Long id, MultipartFile imagen) {
        Servicio servicio = servicioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
        
        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(uploadDir, "servicios");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Generar nombre único para el archivo
            String nombreArchivo = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
            Path rutaCompleta = dirPath.resolve(nombreArchivo);
            
            // Guardar la imagen
            Files.copy(imagen.getInputStream(), rutaCompleta);
            
            // Construir URL de la imagen
            String urlImagen = "/uploads/servicios/" + nombreArchivo;
            
            // Actualizar URL de la imagen en el servicio
            servicio.setImagenUrl(urlImagen);
            servicioRepositorio.save(servicio);
            
            return urlImagen;
        } catch (IOException e) {
            throw new ServicioExcepcion("Error al subir la imagen: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosPorCategoria(Long establecimientoId, String categoria) {
        List<Servicio> servicios = servicioRepositorio.findByEstablecimientoIdAndCategoria(establecimientoId, categoria);
        
        return servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosDisponibles(Long establecimientoId) {
        List<Servicio> servicios = servicioRepositorio.findDisponiblesByEstablecimientoId(establecimientoId);
        
        return servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosPorRangoPrecio(Long establecimientoId, double precioMin, double precioMax) {
        List<Servicio> servicios = servicioRepositorio.findByEstablecimientoIdAndPrecioBetween(
                establecimientoId, 
                BigDecimal.valueOf(precioMin), 
                BigDecimal.valueOf(precioMax));
        
        return servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // Método de conversión de entidad a DTO
    private ServicioDTO convertirADTO(Servicio servicio) {
        ServicioDTO dto = new ServicioDTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecio(servicio.getPrecio());
        dto.setDuracionMinutos(servicio.getDuracionMinutos());
        dto.setCategoria(servicio.getCategoria());
        dto.setImagenUrl(servicio.getImagenUrl());
        dto.setEstaDisponible(servicio.isEstaDisponible());
        dto.setFechaCreacion(servicio.getFechaCreacion());
        dto.setFechaActualizacion(servicio.getFechaActualizacion());
        dto.setEstablecimientoId(servicio.getEstablecimiento().getId());
        dto.setNombreEstablecimiento(servicio.getEstablecimiento().getNombre());
        
        return dto;
    }
}