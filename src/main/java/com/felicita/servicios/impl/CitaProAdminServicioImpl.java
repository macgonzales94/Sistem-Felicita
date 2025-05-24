package com.felicita.servicios.impl;

import com.felicita.dto.CitaProAdminDTO;
import com.felicita.entidades.Cita;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.ProAdmin;
import com.felicita.excepciones.CitaExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.servicios.CitaProAdminServicio;
import com.felicita.servicios.ProAdminServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaProAdminServicioImpl implements CitaProAdminServicio {

    @Autowired
    private CitaRepositorio citaRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private ProAdminServicio proAdminServicio;

    @Override
    @Transactional(readOnly = true)
    public List<CitaProAdminDTO> obtenerCitasProAdmin(Long establecimientoId, String estado, String fecha) {
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByProAdmin(proAdmin);
        
        List<Cita> citas = new ArrayList<>();
        
        // Filtrar por establecimiento si se especifica
        if (establecimientoId != null) {
            Establecimiento establecimiento = establecimientos.stream()
                    .filter(e -> e.getId().equals(establecimientoId))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));
            
            citas = citaRepositorio.findByEstablecimiento(establecimiento);
        } else {
            // Obtener citas de todos los establecimientos del ProAdmin
            for (Establecimiento est : establecimientos) {
                citas.addAll(citaRepositorio.findByEstablecimiento(est));
            }
        }
        
        // Filtrar por estado si se especifica
        if (estado != null && !estado.equals("todos")) {
            Cita.EstadoCita estadoCita = Cita.EstadoCita.valueOf(estado);
            citas = citas.stream()
                    .filter(c -> c.getEstado() == estadoCita)
                    .collect(Collectors.toList());
        }
        
        // Filtrar por fecha si se especifica
        if (fecha != null && !fecha.isEmpty()) {
            LocalDate fechaFiltro = LocalDate.parse(fecha);
            LocalDateTime inicioDelDia = fechaFiltro.atStartOfDay();
            LocalDateTime finDelDia = fechaFiltro.atTime(LocalTime.MAX);
            
            citas = citas.stream()
                    .filter(c -> c.getFechaHora().isAfter(inicioDelDia) && c.getFechaHora().isBefore(finDelDia))
                    .collect(Collectors.toList());
        }
        
        // Ordenar por fecha descendente
        citas.sort((c1, c2) -> c2.getFechaHora().compareTo(c1.getFechaHora()));
        
        return citas.stream()
                .map(CitaProAdminDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaProAdminDTO> obtenerProximasCitas() {
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByProAdmin(proAdmin);
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime enUnaSemana = ahora.plusDays(7);
        
        List<Cita> proximasCitas = new ArrayList<>();
        
        for (Establecimiento est : establecimientos) {
            List<Cita> citasEstablecimiento = citaRepositorio.findByEstablecimientoAndRangoFecha(
                    est.getId(), ahora, enUnaSemana);
            
            // Filtrar solo citas pendientes o confirmadas
            citasEstablecimiento = citasEstablecimiento.stream()
                    .filter(c -> c.getEstado() == Cita.EstadoCita.PENDIENTE || 
                               c.getEstado() == Cita.EstadoCita.CONFIRMADA)
                    .collect(Collectors.toList());
            
            proximasCitas.addAll(citasEstablecimiento);
        }
        
        // Ordenar por fecha ascendente y limitar a 10
        proximasCitas.sort((c1, c2) -> c1.getFechaHora().compareTo(c2.getFechaHora()));
        
        return proximasCitas.stream()
                .limit(10)
                .map(CitaProAdminDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaProAdminDTO> obtenerCitasHoyPorEstablecimiento(Long establecimientoId) {
        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDelDia = LocalDate.now().atTime(LocalTime.MAX);
        
        List<Cita> citasHoy = citaRepositorio.findByEstablecimientoAndRangoFecha(
                establecimientoId, inicioDelDia, finDelDia);
        
        return citasHoy.stream()
                .map(CitaProAdminDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CitaProAdminDTO obtenerCitaPorId(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita pertenece a un establecimiento del ProAdmin
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByProAdmin(proAdmin);
        
        boolean perteneceAlProAdmin = establecimientos.stream()
                .anyMatch(e -> e.getId().equals(cita.getEstablecimiento().getId()));
        
        if (!perteneceAlProAdmin) {
            throw new CitaExcepcion("No tienes permiso para ver esta cita");
        }
        
        return new CitaProAdminDTO(cita);
    }

    @Override
    @Transactional
    public CitaProAdminDTO confirmarCita(Long id) {
        Cita cita = obtenerCitaVerificada(id);
        
        if (cita.getEstado() != Cita.EstadoCita.PENDIENTE) {
            throw new CitaExcepcion("Solo se pueden confirmar citas pendientes");
        }
        
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaProAdminDTO(citaActualizada);
    }

    @Override
    @Transactional
    public CitaProAdminDTO completarCita(Long id) {
        Cita cita = obtenerCitaVerificada(id);
        
        if (cita.getEstado() != Cita.EstadoCita.CONFIRMADA) {
            throw new CitaExcepcion("Solo se pueden completar citas confirmadas");
        }
        
        cita.setEstado(Cita.EstadoCita.COMPLETADA);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaProAdminDTO(citaActualizada);
    }

    @Override
    @Transactional
    public CitaProAdminDTO cancelarCita(Long id) {
        Cita cita = obtenerCitaVerificada(id);
        
        if (cita.getEstado() == Cita.EstadoCita.COMPLETADA || cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new CitaExcepcion("No se puede cancelar una cita completada o ya cancelada");
        }
        
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaProAdminDTO(citaActualizada);
    }

    @Override
    @Transactional
    public CitaProAdminDTO marcarNoAsistio(Long id) {
        Cita cita = obtenerCitaVerificada(id);
        
        if (cita.getEstado() != Cita.EstadoCita.CONFIRMADA) {
            throw new CitaExcepcion("Solo se puede marcar como no asistió a citas confirmadas");
        }
        
        if (cita.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new CitaExcepcion("No se puede marcar como no asistió a una cita futura");
        }
        
        cita.setEstado(Cita.EstadoCita.NO_ASISTIO);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaProAdminDTO(citaActualizada);
    }
    
    private Cita obtenerCitaVerificada(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita pertenece a un establecimiento del ProAdmin
        ProAdmin proAdmin = proAdminServicio.obtenerProAdminEntidadPorUsuarioAutenticado();
        List<Establecimiento> establecimientos = establecimientoRepositorio.findByProAdmin(proAdmin);
        
        boolean perteneceAlProAdmin = establecimientos.stream()
                .anyMatch(e -> e.getId().equals(cita.getEstablecimiento().getId()));
        
        if (!perteneceAlProAdmin) {
            throw new CitaExcepcion("No tienes permiso para modificar esta cita");
        }
        
        return cita;
    }
}