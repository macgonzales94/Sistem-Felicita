package com.felicita.servicios.impl;

import com.felicita.dto.EstablecimientoDTO;
import com.felicita.entidades.Barberia;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.SalonBelleza;
import com.felicita.excepciones.EstablecimientoExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.BarberiaRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.SalonBellezaRepositorio;
import com.felicita.servicios.EstablecimientoServicio;
import com.felicita.servicios.ProAdminServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EstablecimientoServicioImpl implements EstablecimientoServicio {

    @Value("${app.upload.dir:${user.home}/uploads/felicita}")
    private String uploadDir;

    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private BarberiaRepositorio barberiaRepositorio;
    
    @Autowired
    private SalonBellezaRepositorio salonBellezaRepositorio;
    
    @Autowired
    private ProAdminServicio proAdminServicio;

    @Override
    @Transactional
    public EstablecimientoDTO crearEstablecimiento(EstablecimientoDTO establecimientoDTO) {
        if (establecimientoDTO.getTipoEstablecimiento() == null) {
            throw new EstablecimientoExcepcion("El tipo de establecimiento es obligatorio");
        }
        
        if ("BARBERIA".equals(establecimientoDTO.getTipoEstablecimiento())) {
            return crearBarberia(establecimientoDTO);
        } else if ("SALON_BELLEZA".equals(establecimientoDTO.getTipoEstablecimiento())) {
            return crearSalonBelleza(establecimientoDTO);
        } else {
            throw new EstablecimientoExcepcion("Tipo de establecimiento no válido");
        }
    }

    @Override
    @Transactional
    public EstablecimientoDTO crearBarberia(EstablecimientoDTO barberiaDTO) {
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        
        Barberia barberia = new Barberia();
        barberia.setNombre(barberiaDTO.getNombre());
        barberia.setDescripcion(barberiaDTO.getDescripcion());
        barberia.setDireccion(barberiaDTO.getDireccion());
        barberia.setCiudad(barberiaDTO.getCiudad());
        barberia.setCodigoPostal(barberiaDTO.getCodigoPostal());
        barberia.setTelefono(barberiaDTO.getTelefono());
        barberia.setEmail(barberiaDTO.getEmail());
        barberia.setSitioWeb(barberiaDTO.getSitioWeb());
        barberia.setHorariosAtencion(barberiaDTO.getHorariosAtencion());
        barberia.setImagenUrl(barberiaDTO.getImagenUrl());
        barberia.setEstaActivo(true);
        barberia.setProAdmin(proAdmin);
        
        // Campos específicos de Barbería
        barberia.setEspecialidadCortes(barberiaDTO.getEspecialidadCortes());
        barberia.setTieneServiciosBarba(barberiaDTO.isTieneServiciosBarba());
        barberia.setTieneServiciosFaciales(barberiaDTO.isTieneServiciosFaciales());
        barberia.setEstiloBarberia(barberiaDTO.getEstiloBarberia());
        barberia.setAforoMaximo(barberiaDTO.getAforoMaximo());
        
        // Agregar características
        if (barberiaDTO.getCaracteristicas() != null) {
            barberia.setCaracteristicas(barberiaDTO.getCaracteristicas());
        }
        
        Barberia guardada = barberiaRepositorio.save(barberia);
        return convertirADTO(guardada);
    }

    @Override
    @Transactional
    public EstablecimientoDTO crearSalonBelleza(EstablecimientoDTO salonDTO) {
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        
        SalonBelleza salon = new SalonBelleza();
        salon.setNombre(salonDTO.getNombre());
        salon.setDescripcion(salonDTO.getDescripcion());
        salon.setDireccion(salonDTO.getDireccion());
        salon.setCiudad(salonDTO.getCiudad());
        salon.setCodigoPostal(salonDTO.getCodigoPostal());
        salon.setTelefono(salonDTO.getTelefono());
        salon.setEmail(salonDTO.getEmail());
        salon.setSitioWeb(salonDTO.getSitioWeb());
        salon.setHorariosAtencion(salonDTO.getHorariosAtencion());
        salon.setImagenUrl(salonDTO.getImagenUrl());
        salon.setEstaActivo(true);
        salon.setProAdmin(proAdmin);
        
        // Campos específicos de Salón de Belleza
        salon.setEspecialidad(salonDTO.getEspecialidad());
        salon.setTieneServiciosMaquillaje(salonDTO.isTieneServiciosMaquillaje());
        salon.setTieneServiciosUnas(salonDTO.isTieneServiciosUnas());
        salon.setTieneTratamientosCapilares(salonDTO.isTieneTratamientosCapilares());
        salon.setAforoMaximo(salonDTO.getAforoMaximo());
        
        // Agregar características
        if (salonDTO.getCaracteristicas() != null) {
            salon.setCaracteristicas(salonDTO.getCaracteristicas());
        }
        
        SalonBelleza guardado = salonBellezaRepositorio.save(salon);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EstablecimientoDTO obtenerEstablecimientoPorId(Long id) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para acceder a este establecimiento");
        }
        
        return convertirADTO(establecimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoDTO> obtenerEstablecimientosProAdmin() {
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByProAdmin(proAdmin);
        
        return establecimientos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EstablecimientoDTO actualizarEstablecimiento(Long id, EstablecimientoDTO establecimientoDTO) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar este establecimiento");
        }
        
        // Determinar qué tipo de establecimiento es para actualizar
        if (establecimiento instanceof Barberia) {
            return actualizarBarberia(id, establecimientoDTO);
        } else if (establecimiento instanceof SalonBelleza) {
            return actualizarSalonBelleza(id, establecimientoDTO);
        } else {
            throw new EstablecimientoExcepcion("Tipo de establecimiento no reconocido");
        }
    }

    @Override
    @Transactional
    public EstablecimientoDTO actualizarBarberia(Long id, EstablecimientoDTO barberiaDTO) {
        Barberia barberia = barberiaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Barbería no encontrada con ID: " + id));
        
        // Verificar que la barbería pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!barberia.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar esta barbería");
        }
        
        // Actualizar campos generales
        barberia.setNombre(barberiaDTO.getNombre());
        barberia.setDescripcion(barberiaDTO.getDescripcion());
        barberia.setDireccion(barberiaDTO.getDireccion());
        barberia.setCiudad(barberiaDTO.getCiudad());
        barberia.setCodigoPostal(barberiaDTO.getCodigoPostal());
        barberia.setTelefono(barberiaDTO.getTelefono());
        barberia.setEmail(barberiaDTO.getEmail());
        barberia.setSitioWeb(barberiaDTO.getSitioWeb());
        barberia.setHorariosAtencion(barberiaDTO.getHorariosAtencion());
        barberia.setEstaActivo(barberiaDTO.isEstaActivo());
        
        // Actualizar campos específicos de Barbería
        barberia.setEspecialidadCortes(barberiaDTO.getEspecialidadCortes());
        barberia.setTieneServiciosBarba(barberiaDTO.isTieneServiciosBarba());
        barberia.setTieneServiciosFaciales(barberiaDTO.isTieneServiciosFaciales());
        barberia.setEstiloBarberia(barberiaDTO.getEstiloBarberia());
        barberia.setAforoMaximo(barberiaDTO.getAforoMaximo());
        
        Barberia actualizada = barberiaRepositorio.save(barberia);
        return convertirADTO(actualizada);
    }

    @Override
    @Transactional
    public EstablecimientoDTO actualizarSalonBelleza(Long id, EstablecimientoDTO salonDTO) {
        SalonBelleza salon = salonBellezaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Salón de belleza no encontrado con ID: " + id));
        
        // Verificar que el salón pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!salon.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar este salón de belleza");
        }
        
        // Actualizar campos generales
        salon.setNombre(salonDTO.getNombre());
        salon.setDescripcion(salonDTO.getDescripcion());
        salon.setDireccion(salonDTO.getDireccion());
        salon.setCiudad(salonDTO.getCiudad());
        salon.setCodigoPostal(salonDTO.getCodigoPostal());
        salon.setTelefono(salonDTO.getTelefono());
        salon.setEmail(salonDTO.getEmail());
        salon.setSitioWeb(salonDTO.getSitioWeb());
        salon.setHorariosAtencion(salonDTO.getHorariosAtencion());
        salon.setEstaActivo(salonDTO.isEstaActivo());
        
        // Actualizar campos específicos de Salón de Belleza
        salon.setEspecialidad(salonDTO.getEspecialidad());
        salon.setTieneServiciosMaquillaje(salonDTO.isTieneServiciosMaquillaje());
        salon.setTieneServiciosUnas(salonDTO.isTieneServiciosUnas());
        salon.setTieneTratamientosCapilares(salonDTO.isTieneTratamientosCapilares());
        salon.setAforoMaximo(salonDTO.getAforoMaximo());
        
        SalonBelleza actualizado = salonBellezaRepositorio.save(salon);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarEstablecimiento(Long id) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para eliminar este establecimiento");
        }
        
        // Por seguridad, en lugar de eliminar, marcar como inactivo
        establecimiento.setEstaActivo(false);
        establecimientoRepositorio.save(establecimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEstablecimiento(Long id) {
        return establecimientoRepositorio.existsById(id);
    }

    @Override
    @Transactional
    public String subirImagenEstablecimiento(Long id, MultipartFile imagen) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar este establecimiento");
        }
        
        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(uploadDir, "establecimientos");
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            // Generar nombre único para el archivo
            String nombreArchivo = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
            Path rutaCompleta = dirPath.resolve(nombreArchivo);
            
            // Guardar la imagen
            Files.copy(imagen.getInputStream(), rutaCompleta);
            
            // Construir URL de la imagen
            String urlImagen = "/uploads/establecimientos/" + nombreArchivo;
            
            // Actualizar URL de la imagen en el establecimiento
            establecimiento.setImagenUrl(urlImagen);
            establecimientoRepositorio.save(establecimiento);
            
            return urlImagen;
        } catch (IOException e) {
            throw new EstablecimientoExcepcion("Error al subir la imagen: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void agregarCaracteristica(Long id, String caracteristica) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar este establecimiento");
        }
        
        establecimiento.agregarCaracteristica(caracteristica);
        establecimientoRepositorio.save(establecimiento);
    }

    @Override
    @Transactional
    public void eliminarCaracteristica(Long id, String caracteristica) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        // Verificar que el establecimiento pertenece al proAdmin autenticado
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para modificar este establecimiento");
        }
        
        establecimiento.eliminarCaracteristica(caracteristica);
        establecimientoRepositorio.save(establecimiento);
    }
    
    // Método para convertir entidad a DTO
    private EstablecimientoDTO convertirADTO(Establecimiento establecimiento) {
        EstablecimientoDTO dto = new EstablecimientoDTO();
        dto.setId(establecimiento.getId());
        dto.setNombre(establecimiento.getNombre());
        dto.setDescripcion(establecimiento.getDescripcion());
        dto.setDireccion(establecimiento.getDireccion());
        dto.setCiudad(establecimiento.getCiudad());
        dto.setCodigoPostal(establecimiento.getCodigoPostal());
        dto.setTelefono(establecimiento.getTelefono());
        dto.setEmail(establecimiento.getEmail());
        dto.setSitioWeb(establecimiento.getSitioWeb());
        dto.setHorariosAtencion(establecimiento.getHorariosAtencion());
        dto.setImagenUrl(establecimiento.getImagenUrl());
        dto.setFechaRegistro(establecimiento.getFechaRegistro());
        dto.setFechaActualizacion(establecimiento.getFechaActualizacion());
        dto.setEstaActivo(establecimiento.isEstaActivo());
        dto.setProAdminId(establecimiento.getProAdmin().getId());
        dto.setCaracteristicas(establecimiento.getCaracteristicas());
        
        // Determinar tipo y campos específicos
        if (establecimiento instanceof Barberia) {
            Barberia barberia = (Barberia) establecimiento;
            dto.setTipoEstablecimiento("BARBERIA");
            dto.setEspecialidadCortes(barberia.getEspecialidadCortes());
            dto.setTieneServiciosBarba(barberia.isTieneServiciosBarba());
            dto.setTieneServiciosFaciales(barberia.isTieneServiciosFaciales());
            dto.setEstiloBarberia(barberia.getEstiloBarberia());
            dto.setAforoMaximo(barberia.getAforoMaximo());
        } else if (establecimiento instanceof SalonBelleza) {
            SalonBelleza salon = (SalonBelleza) establecimiento;
            dto.setTipoEstablecimiento("SALON_BELLEZA");
            dto.setEspecialidad(salon.getEspecialidad());
            dto.setTieneServiciosMaquillaje(salon.isTieneServiciosMaquillaje());
            dto.setTieneServiciosUnas(salon.isTieneServiciosUnas());
            dto.setTieneTratamientosCapilares(salon.isTieneTratamientosCapilares());
            dto.setAforoMaximo(salon.getAforoMaximo());
        }
        
        return dto;
    }
}