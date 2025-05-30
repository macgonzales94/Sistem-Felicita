package com.felicita.servicios.impl;

import com.felicita.dto.ServicioDTO;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Servicio;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.excepciones.ServicioExcepcion;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ServicioRepositorio;
import com.felicita.servicios.ProAdminServicio;
import com.felicita.servicios.ServicioServicio;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioServicioImpl implements ServicioServicio {

    @Autowired
    private ServicioRepositorio servicioRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private ProAdminServicio proAdminServicio;
    
    private static final String DIRECTORIO_IMAGENES = "uploads/servicios/";
    
    @Override
    public ServicioDTO crearServicio(ServicioDTO servicioDTO) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Verificar que el establecimiento existe y pertenece al ProAdmin
            Establecimiento establecimiento = establecimientoRepositorio
                    .findByIdAndProAdminId(servicioDTO.getEstablecimientoId(), proAdmin.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                            "Establecimiento no encontrado o no tienes permiso para acceder a él"));
            
            // Validar datos del servicio
            validarDatosServicio(servicioDTO);
            
            // Verificar que no existe otro servicio con el mismo nombre en el establecimiento
            if (servicioRepositorio.existsByNombreAndEstablecimientoId(
                    servicioDTO.getNombre(), servicioDTO.getEstablecimientoId())) {
                throw new ServicioExcepcion("Ya existe un servicio con ese nombre en este establecimiento");
            }
            
            // Crear entidad Servicio
            Servicio servicio = new Servicio();
            servicio.setNombre(servicioDTO.getNombre());
            servicio.setDescripcion(servicioDTO.getDescripcion());
            servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
            servicio.setPrecio(servicioDTO.getPrecio());
            servicio.setCategoria(servicioDTO.getCategoria());
            servicio.setEstaDisponible(servicioDTO.isEstaDisponible());
            servicio.setEstablecimiento(establecimiento);
            servicio.setFechaCreacion(LocalDateTime.now());
            servicio.setFechaActualizacion(LocalDateTime.now());
            
            // Guardar servicio
            Servicio servicioGuardado = servicioRepositorio.save(servicio);
            
            return convertirADTO(servicioGuardado);
            
        } catch (ServicioExcepcion | RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al crear el servicio: " + e.getMessage());
        }
    }
    
    @Override
    public ServicioDTO actualizarServicio(Long id, ServicioDTO servicioDTO) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Obtener servicio existente
            Servicio servicio = servicioRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
            
            // Verificar que el servicio pertenece a un establecimiento del ProAdmin
            if (!servicio.getEstablecimiento().getProAdmin().getId().equals(proAdmin.getId())) {
                throw new ServicioExcepcion("No tienes permiso para modificar este servicio");
            }
            
            // Validar datos del servicio
            validarDatosServicio(servicioDTO);
            
            // Verificar que no existe otro servicio con el mismo nombre (excluyendo el actual)
            if (servicioRepositorio.existsByNombreAndEstablecimientoIdAndIdNot(
                    servicioDTO.getNombre(), servicio.getEstablecimiento().getId(), id)) {
                throw new ServicioExcepcion("Ya existe otro servicio con ese nombre en este establecimiento");
            }
            
            // Actualizar datos del servicio
            servicio.setNombre(servicioDTO.getNombre());
            servicio.setDescripcion(servicioDTO.getDescripcion());
            servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
            servicio.setPrecio(servicioDTO.getPrecio());
            servicio.setCategoria(servicioDTO.getCategoria());
            servicio.setEstaDisponible(servicioDTO.isEstaDisponible());
            servicio.setFechaActualizacion(LocalDateTime.now());
            
            // Guardar cambios
            Servicio servicioActualizado = servicioRepositorio.save(servicio);
            
            return convertirADTO(servicioActualizado);
            
        } catch (ServicioExcepcion | RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al actualizar el servicio: " + e.getMessage());
        }
    }
    
    @Override
    public void eliminarServicio(Long id) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Obtener servicio
            Servicio servicio = servicioRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
            
            // Verificar permisos
            if (!servicio.getEstablecimiento().getProAdmin().getId().equals(proAdmin.getId())) {
                throw new ServicioExcepcion("No tienes permiso para eliminar este servicio");
            }
            
            // Verificar si el servicio tiene citas asociadas
            if (!servicio.getCitas().isEmpty()) {
                throw new ServicioExcepcion("No se puede eliminar el servicio porque tiene citas asociadas. " +
                        "Primero cancela o completa todas las citas de este servicio.");
            }
            
            // Eliminar imagen del servicio si existe
            if (servicio.getImagenUrl() != null && !servicio.getImagenUrl().isEmpty()) {
                eliminarImagenServicio(servicio.getImagenUrl());
            }
            
            // Eliminar servicio
            servicioRepositorio.delete(servicio);
            
        } catch (ServicioExcepcion | RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al eliminar el servicio: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public ServicioDTO obtenerServicioPorId(Long id) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Obtener servicio
            Servicio servicio = servicioRepositorio.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + id));
            
            // Verificar permisos
            if (!servicio.getEstablecimiento().getProAdmin().getId().equals(proAdmin.getId())) {
                throw new ServicioExcepcion("No tienes permiso para ver este servicio");
            }
            
            return convertirADTO(servicio);
            
        } catch (ServicioExcepcion | RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al obtener el servicio: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosPorEstablecimiento(Long establecimientoId) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Verificar que el establecimiento pertenece al ProAdmin
            Establecimiento establecimiento = establecimientoRepositorio
                    .findByIdAndProAdminId(establecimientoId, proAdmin.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                            "Establecimiento no encontrado o no tienes permiso para acceder a él"));
            
            // Obtener servicios ordenados por categoría y nombre
            List<Servicio> servicios = servicioRepositorio
                    .findByEstablecimientoIdOrderByCategoriaAscNombreAsc(establecimientoId);
            
            return servicios.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            
        } catch (RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al obtener los servicios: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ServicioDTO> obtenerServiciosDisponiblesPorEstablecimiento(Long establecimientoId) {
        try {
            List<Servicio> servicios = servicioRepositorio
                    .findByEstablecimientoIdAndEstaDisponibleTrueOrderByCategoriaAscNombreAsc(establecimientoId);
            
            return servicios.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al obtener los servicios disponibles: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ServicioDTO> obtenerServiciosPaginados(Long establecimientoId, Pageable pageable) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Verificar permisos del establecimiento
            establecimientoRepositorio.findByIdAndProAdminId(establecimientoId, proAdmin.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                            "Establecimiento no encontrado o no tienes permiso para acceder a él"));
            
            Page<Servicio> servicios = servicioRepositorio.findByEstablecimientoId(establecimientoId, pageable);
            
            return servicios.map(this::convertirADTO);
            
        } catch (RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al obtener los servicios paginados: " + e.getMessage());
        }
    }
    
    @Override
    public String subirImagenServicio(Long servicioId, MultipartFile imagen) {
        try {
            // Obtener ProAdmin autenticado
            ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
            
            // Obtener servicio
            Servicio servicio = servicioRepositorio.findById(servicioId)
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado con ID: " + servicioId));
            
            // Verificar permisos
            if (!servicio.getEstablecimiento().getProAdmin().getId().equals(proAdmin.getId())) {
                throw new ServicioExcepcion("No tienes permiso para modificar este servicio");
            }
            
            // Validar imagen
            if (imagen.isEmpty()) {
                throw new ServicioExcepcion("No se ha seleccionado ninguna imagen");
            }
            
            // Validar tipo de archivo
            String tipoContenido = imagen.getContentType();
            if (tipoContenido == null || !tipoContenido.startsWith("image/")) {
                throw new ServicioExcepcion("El archivo debe ser una imagen");
            }
            
            // Validar tamaño (máximo 5MB)
            if (imagen.getSize() > 5 * 1024 * 1024) {
                throw new ServicioExcepcion("La imagen no puede ser mayor a 5MB");
            }
            
            // Crear directorio si no existe
            Path directorioImagenes = Paths.get(DIRECTORIO_IMAGENES);
            if (!Files.exists(directorioImagenes)) {
                Files.createDirectories(directorioImagenes);
            }
            
            // Generar nombre único para la imagen
            String extension = obtenerExtensionArchivo(imagen.getOriginalFilename());
            String nombreArchivo = "servicio_" + servicioId + "_" + UUID.randomUUID().toString() + extension;
            Path rutaArchivo = directorioImagenes.resolve(nombreArchivo);
            
            // Eliminar imagen anterior si existe
            if (servicio.getImagenUrl() != null && !servicio.getImagenUrl().isEmpty()) {
                eliminarImagenServicio(servicio.getImagenUrl());
            }
            
            // Guardar nueva imagen
            Files.copy(imagen.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
            
            // Actualizar URL en la base de datos
            String imagenUrl = "/" + DIRECTORIO_IMAGENES + nombreArchivo;
            servicio.setImagenUrl(imagenUrl);
            servicio.setFechaActualizacion(LocalDateTime.now());
            servicioRepositorio.save(servicio);
            
            return imagenUrl;
            
        } catch (ServicioExcepcion | RecursoNoEncontradoExcepcion e) {
            throw e;
        } catch (IOException e) {
            throw new ServicioExcepcion("Error al guardar la imagen: " + e.getMessage());
        } catch (Exception e) {
            throw new ServicioExcepcion("Error al subir la imagen del servicio: " + e.getMessage());
        }
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private void validarDatosServicio(ServicioDTO servicioDTO) {
        if (servicioDTO.getNombre() == null || servicioDTO.getNombre().trim().isEmpty()) {
            throw new ServicioExcepcion("El nombre del servicio es requerido");
        }
        
        if (servicioDTO.getNombre().length() > 100) {
            throw new ServicioExcepcion("El nombre del servicio no puede exceder 100 caracteres");
        }
        
        if (servicioDTO.getDescripcion() != null && servicioDTO.getDescripcion().length() > 500) {
            throw new ServicioExcepcion("La descripción no puede exceder 500 caracteres");
        }
        
        if (servicioDTO.getDuracionMinutos() == null || servicioDTO.getDuracionMinutos() < 5) {
            throw new ServicioExcepcion("La duración mínima del servicio es de 5 minutos");
        }
        
        if (servicioDTO.getDuracionMinutos() > 480) { // 8 horas máximo
            throw new ServicioExcepcion("La duración máxima del servicio es de 480 minutos (8 horas)");
        }
        
        if (servicioDTO.getPrecio() == null || servicioDTO.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServicioExcepcion("El precio del servicio debe ser mayor a 0");
        }
        
        if (servicioDTO.getPrecio().compareTo(new BigDecimal("9999.99")) > 0) {
            throw new ServicioExcepcion("El precio máximo del servicio es S/ 9999.99");
        }
        
        if (servicioDTO.getCategoria() == null || servicioDTO.getCategoria().trim().isEmpty()) {
            throw new ServicioExcepcion("La categoría del servicio es requerida");
        }
        
        if (servicioDTO.getEstablecimientoId() == null) {
            throw new ServicioExcepcion("El ID del establecimiento es requerido");
        }
    }
    
    private ServicioDTO convertirADTO(Servicio servicio) {
        ServicioDTO dto = new ServicioDTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setDuracionMinutos(servicio.getDuracionMinutos());
        dto.setPrecio(servicio.getPrecio());
        dto.setCategoria(servicio.getCategoria());
        dto.setImagenUrl(servicio.getImagenUrl());
        dto.setEstaDisponible(servicio.isEstaDisponible());
        dto.setEstablecimientoId(servicio.getEstablecimiento().getId());
        dto.setNombreEstablecimiento(servicio.getEstablecimiento().getNombre());
        dto.setFechaCreacion(servicio.getFechaCreacion());
        dto.setFechaActualizacion(servicio.getFechaActualizacion());
        
        return dto;
    }
    
    private String obtenerExtensionArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.lastIndexOf('.') == -1) {
            return ".jpg"; // Extensión por defecto
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.'));
    }
    
    private void eliminarImagenServicio(String imagenUrl) {
        try {
            if (imagenUrl != null && imagenUrl.startsWith("/")) {
                Path rutaArchivo = Paths.get(imagenUrl.substring(1)); // Remover el '/' inicial
                Files.deleteIfExists(rutaArchivo);
            }
        } catch (IOException e) {
            // Log del error pero no fallar la operación principal
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }
    }
}