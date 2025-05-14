package com.felicita.servicios.impl;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ProAdminDTO;
import com.felicita.entidades.Cita;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ProAdminRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.ProAdminServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProAdminServicioImpl implements ProAdminServicio {

    @Autowired
    private ProAdminRepositorio proAdminRepositorio;
    
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private CitaRepositorio citaRepositorio;

    @Override
    @Transactional(readOnly = true)
    public ProAdminDTO buscarPorId(Long id) {
        ProAdmin proAdmin = proAdminRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("ProAdmin no encontrado con ID: " + id));
        return convertirADTO(proAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public ProAdminDTO buscarPorUsuarioId(Long usuarioId) {
        ProAdmin proAdmin = proAdminRepositorio.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("ProAdmin no encontrado para usuario con ID: " + usuarioId));
        return convertirADTO(proAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public ProAdminDTO buscarPorUsuarioEmail(String email) {
        ProAdmin proAdmin = proAdminRepositorio.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("ProAdmin no encontrado para email: " + email));
        return convertirADTO(proAdmin);
    }

    @Override
    @Transactional
    public ProAdminDTO registrarProAdmin(Usuario usuario) {
        // Verificar si ya existe un proAdmin para este usuario
        if (proAdminRepositorio.existsByUsuarioId(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un ProAdmin para este usuario");
        }
        
        // Crear nuevo ProAdmin
        ProAdmin proAdmin = new ProAdmin();
        proAdmin.setUsuario(usuario);
        
        ProAdmin guardado = proAdminRepositorio.save(proAdmin);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public ProAdminDTO actualizarProAdmin(Long id, ProAdminDTO proAdminDTO) {
        ProAdmin proAdmin = proAdminRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("ProAdmin no encontrado con ID: " + id));
        
        // Actualizar campos
        proAdmin.setDocumentoIdentidad(proAdminDTO.getDocumentoIdentidad());
        proAdmin.setDireccion(proAdminDTO.getDireccion());
        proAdmin.setCiudad(proAdminDTO.getCiudad());
        proAdmin.setTelefonoContacto(proAdminDTO.getTelefonoContacto());
        proAdmin.setPlanSubscripcion(proAdminDTO.getPlanSubscripcion());
        proAdmin.setMetodoPago(proAdminDTO.getMetodoPago());
        
        ProAdmin actualizado = proAdminRepositorio.save(proAdmin);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarProAdmin(Long id) {
        if (!proAdminRepositorio.existsById(id)) {
            throw new RecursoNoEncontradoExcepcion("ProAdmin no encontrado con ID: " + id);
        }
        proAdminRepositorio.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorUsuarioId(Long usuarioId) {
        return proAdminRepositorio.existsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProAdminDTO obtenerProAdminAutenticado() {
        ProAdmin proAdmin = obtenerProAdminEntidadPorUsuarioAutenticado();
        return convertirADTO(proAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public ProAdmin obtenerProAdminEntidadPorUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Usuario no encontrado con email: " + email));
        
        return proAdminRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("ProAdmin no encontrado para usuario: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticas() {
        ProAdmin proAdmin = obtenerProAdminEntidadPorUsuarioAutenticado();
        EstadisticasDTO estadisticas = new EstadisticasDTO();
        
        // Obtener cantidad de establecimientos
        long cantidadEstablecimientos = establecimientoRepositorio.countByProAdminId(proAdmin.getId());
        estadisticas.setTotalEstablecimientos(cantidadEstablecimientos);
        
        // Obtener citas de los establecimientos del proAdmin
        List<Long> idsEstablecimientos = establecimientoRepositorio.findIdsByProAdminId(proAdmin.getId());
        
        // Obtener citas totales
        long citasTotales = citaRepositorio.countByEstablecimientoIdIn(idsEstablecimientos);
        estadisticas.setTotalCitas(citasTotales);
        
        // Obtener citas pendientes
        long citasPendientes = citaRepositorio.countByEstablecimientoIdInAndEstado(
                idsEstablecimientos, Cita.EstadoCita.PENDIENTE);
        estadisticas.setCitasPendientes(citasPendientes);
        
        // Obtener citas confirmadas
        long citasConfirmadas = citaRepositorio.countByEstablecimientoIdInAndEstado(
                idsEstablecimientos, Cita.EstadoCita.CONFIRMADA);
        estadisticas.setCitasConfirmadas(citasConfirmadas);
        
        // Obtener citas completadas
        long citasCompletadas = citaRepositorio.countByEstablecimientoIdInAndEstado(
                idsEstablecimientos, Cita.EstadoCita.COMPLETADA);
        estadisticas.setCitasCompletadas(citasCompletadas);
        
        // Obtener citas canceladas
        long citasCanceladas = citaRepositorio.countByEstablecimientoIdInAndEstado(
                idsEstablecimientos, Cita.EstadoCita.CANCELADA);
        estadisticas.setCitasCanceladas(citasCanceladas);
        
        // Obtener citas de hoy
        LocalDateTime hoyInicio = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime hoyFin = hoyInicio.plusDays(1);
        long citasHoy = citaRepositorio.countByEstablecimientoIdInAndFechaHoraBetween(
                idsEstablecimientos, hoyInicio, hoyFin);
        estadisticas.setCitasHoy(citasHoy);
        
        // Obtener citas de esta semana
        LocalDateTime inicioSemana = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime finSemana = inicioSemana.plusDays(7);
        long citasSemana = citaRepositorio.countByEstablecimientoIdInAndFechaHoraBetween(
                idsEstablecimientos, inicioSemana, finSemana);
        estadisticas.setCitasSemana(citasSemana);
        
        // Calcular tasa de asistencia
        if (citasCompletadas + citasCanceladas > 0) {
            double tasaAsistencia = (double) citasCompletadas / (citasCompletadas + citasCanceladas) * 100;
            estadisticas.setTasaAsistencia(Math.round(tasaAsistencia * 100.0) / 100.0);
        }
        
        return estadisticas;
    }
    
    // Métodos de conversión entre entidad y DTO
    private ProAdminDTO convertirADTO(ProAdmin proAdmin) {
        ProAdminDTO dto = new ProAdminDTO();
        dto.setId(proAdmin.getId());
        dto.setUsuarioId(proAdmin.getUsuario().getId());
        dto.setNombre(proAdmin.getUsuario().getNombre());
        dto.setApellido(proAdmin.getUsuario().getApellido());
        dto.setEmail(proAdmin.getUsuario().getEmail());
        dto.setTelefono(proAdmin.getUsuario().getTelefono());
        dto.setDocumentoIdentidad(proAdmin.getDocumentoIdentidad());
        dto.setDireccion(proAdmin.getDireccion());
        dto.setCiudad(proAdmin.getCiudad());
        dto.setTelefonoContacto(proAdmin.getTelefonoContacto());
        dto.setPlanSubscripcion(proAdmin.getPlanSubscripcion());
        dto.setMetodoPago(proAdmin.getMetodoPago());
        
        // Obtener cantidad de establecimientos
        long cantidadEstablecimientos = establecimientoRepositorio.countByProAdminId(proAdmin.getId());
        dto.setCantidadEstablecimientos(cantidadEstablecimientos);
        
        return dto;
    }
}