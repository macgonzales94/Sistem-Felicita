package com.felicita.servicios.impl;

import com.felicita.dto.EstablecimientoClienteDTO;
import com.felicita.dto.ServicioClienteDTO;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.Servicio;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ServicioRepositorio;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.servicios.EstablecimientoClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstablecimientoClienteServicioImpl implements EstablecimientoClienteServicio {

    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private ServicioRepositorio servicioRepositorio;
    
    @Autowired
    private CitaRepositorio citaRepositorio;

    @Override
    @Transactional(readOnly = true)
    public Page<EstablecimientoClienteDTO> obtenerEstablecimientosActivos(Pageable pageable) {
        // Obtener solo establecimientos activos
        return establecimientoRepositorio.findAll(pageable)
                .map(this::convertirAEstablecimientoClienteDTO)
                .filter(est -> est.isAbierto()); // Filtrar solo los abiertos
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoClienteDTO> buscarPorCiudad(String ciudad) {
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByCiudadIgnoreCase(ciudad);
        
        return establecimientos.stream()
                .filter(est -> est.isEstaActivo())
                .map(this::convertirAEstablecimientoClienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoClienteDTO> buscarPorNombre(String nombre) {
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByNombreContainingIgnoreCase(nombre);
        
        return establecimientos.stream()
                .filter(est -> est.isEstaActivo())
                .map(this::convertirAEstablecimientoClienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoClienteDTO> buscarPorTipo(String tipo) {
        List<Establecimiento> todosLosEstablecimientos = establecimientoRepositorio.findAll();
        
        return todosLosEstablecimientos.stream()
                .filter(est -> est.isEstaActivo())
                .filter(est -> {
                    if ("BARBERIA".equals(tipo)) {
                        return est.getClass().getSimpleName().equals("Barberia");
                    } else if ("SALON_BELLEZA".equals(tipo)) {
                        return est.getClass().getSimpleName().equals("SalonBelleza");
                    }
                    return true;
                })
                .map(this::convertirAEstablecimientoClienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstablecimientoClienteDTO obtenerDetalleEstablecimiento(Long id) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado con ID: " + id));
        
        if (!establecimiento.isEstaActivo()) {
            throw new RecursoNoEncontradoExcepcion("El establecimiento no está disponible");
        }
        
        EstablecimientoClienteDTO dto = convertirAEstablecimientoClienteDTO(establecimiento);
        
        // Agregar servicios destacados - CORREGIDO: usar el método correcto
        List<Servicio> servicios = servicioRepositorio.findDisponiblesByEstablecimientoId(id);
        List<ServicioClienteDTO> serviciosDestacados = servicios.stream()
                .limit(6) // Máximo 6 servicios destacados
                .map(this::convertirAServicioClienteDTO)
                .collect(Collectors.toList());
        
        dto.setServiciosDestacados(serviciosDestacados);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioClienteDTO> obtenerServiciosDeEstablecimiento(Long establecimientoId) {
        // CORREGIDO: usar el método correcto
        List<Servicio> servicios = servicioRepositorio.findDisponiblesByEstablecimientoId(establecimientoId);
        
        return servicios.stream()
                .map(this::convertirAServicioClienteDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoClienteDTO> obtenerEstablecimientosCercanos(String ciudadCliente) {
        // Por simplicidad, buscamos por la misma ciudad
        return buscarPorCiudad(ciudadCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstablecimientoClienteDTO> obtenerMejorCalificados(int limite) {
        List<Establecimiento> establecimientos = establecimientoRepositorio.findAll();
        
        return establecimientos.stream()
                .filter(est -> est.isEstaActivo())
                .map(this::convertirAEstablecimientoClienteDTO)
                .sorted((a, b) -> Double.compare(b.getCalificacionPromedio(), a.getCalificacionPromedio()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaAbierto(Long establecimientoId) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(establecimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));
        
        if (!establecimiento.isEstaActivo()) {
            return false;
        }
        
        // Verificar día de la semana y horario actual
        LocalTime ahora = LocalTime.now();
        String diaActual = LocalDate.now().getDayOfWeek().name();
        
        boolean diaValido = establecimiento.getDiasAtencion() != null && 
                           establecimiento.getDiasAtencion().contains(diaActual);
        
        if (!diaValido) {
            return false;
        }
        
        // Verificar horario
        if (establecimiento.getHoraApertura() != null && establecimiento.getHoraCierre() != null) {
            try {
                LocalTime apertura = LocalTime.parse(establecimiento.getHoraApertura());
                LocalTime cierre = LocalTime.parse(establecimiento.getHoraCierre());
                
                return ahora.isAfter(apertura) && ahora.isBefore(cierre);
            } catch (Exception e) {
                // Si hay error en el parseo, asumir que está abierto
                return true;
            }
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerHorariosDisponibles(Long establecimientoId, String fecha) {
        Establecimiento establecimiento = establecimientoRepositorio.findById(establecimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));
        
        List<String> horariosDisponibles = new ArrayList<>();
        
        if (establecimiento.getHoraApertura() == null || establecimiento.getHoraCierre() == null) {
            return horariosDisponibles;
        }
        
        try {
            LocalDate fechaSeleccionada = LocalDate.parse(fecha);
            LocalTime horaInicio = LocalTime.parse(establecimiento.getHoraApertura());
            LocalTime horaFin = LocalTime.parse(establecimiento.getHoraCierre());
            int intervalo = establecimiento.getIntervalosCitas() != null ? establecimiento.getIntervalosCitas() : 30;
            
            LocalTime horaActual = horaInicio;
            while (horaActual.isBefore(horaFin)) {
                LocalDateTime fechaHoraCompleta = LocalDateTime.of(fechaSeleccionada, horaActual);
                
                // Verificar si ya hay una cita en ese horario
                boolean ocupado = citaRepositorio.findByEstablecimientoAndRangoFecha(
                        establecimientoId, 
                        fechaHoraCompleta.minusMinutes(15), 
                        fechaHoraCompleta.plusMinutes(15)
                ).stream().anyMatch(cita -> 
                    cita.getEstado().name().equals("PENDIENTE") || 
                    cita.getEstado().name().equals("CONFIRMADA")
                );
                
                if (!ocupado) {
                    horariosDisponibles.add(horaActual.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                
                horaActual = horaActual.plusMinutes(intervalo);
            }
        } catch (Exception e) {
            // Si hay error, retornar lista vacía
            return new ArrayList<>();
        }
        
        return horariosDisponibles;
    }
    
    // Métodos de conversión
    private EstablecimientoClienteDTO convertirAEstablecimientoClienteDTO(Establecimiento establecimiento) {
        EstablecimientoClienteDTO dto = new EstablecimientoClienteDTO();
        
        try {
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
            
            // Determinar tipo
            if (establecimiento.getClass().getSimpleName().equals("Barberia")) {
                dto.setTipo("BARBERIA");
            } else if (establecimiento.getClass().getSimpleName().equals("SalonBelleza")) {
                dto.setTipo("SALON_BELLEZA");
            } else {
                dto.setTipo("GENERICO");
            }
            
            // Características
            if (establecimiento.getCaracteristicas() != null) {
                dto.setCaracteristicas(new ArrayList<>(establecimiento.getCaracteristicas()));
            } else {
                dto.setCaracteristicas(new ArrayList<>());
            }
            
            // Calcular calificación promedio (simulada por ahora)
            dto.setCalificacionPromedio(4.2 + (Math.random() * 0.8)); // Entre 4.2 y 5.0
            dto.setCantidadResenas((int) (Math.random() * 100) + 10); // Entre 10 y 110
            
            // Verificar si está abierto
            dto.setAbierto(estaAbierto(establecimiento.getId()));
            
        } catch (Exception e) {
            // Log del error pero continuar con valores por defecto
            System.err.println("Error convirtiendo establecimiento: " + e.getMessage());
            dto.setCalificacionPromedio(4.0);
            dto.setCantidadResenas(0);
            dto.setAbierto(false);
        }
        
        return dto;
    }
    
    private ServicioClienteDTO convertirAServicioClienteDTO(Servicio servicio) {
        ServicioClienteDTO dto = new ServicioClienteDTO();
        
        try {
            dto.setId(servicio.getId());
            dto.setNombre(servicio.getNombre());
            dto.setDescripcion(servicio.getDescripcion());
            dto.setPrecio(servicio.getPrecio());
            dto.setDuracionMinutos(servicio.getDuracionMinutos());
            dto.setCategoria(servicio.getCategoria());
            dto.setImagenUrl(servicio.getImagenUrl());
            dto.setEstablecimientoId(servicio.getEstablecimiento().getId());
            dto.setEstablecimientoNombre(servicio.getEstablecimiento().getNombre());
            dto.setDisponible(servicio.isEstaDisponible());
        } catch (Exception e) {
            System.err.println("Error convirtiendo servicio: " + e.getMessage());
            // Valores por defecto en caso de error
            dto.setDisponible(false);
        }
        
        return dto;
    }
}