package com.felicita.servicios.impl;

import com.felicita.dto.EstadisticasDTO;
import com.felicita.dto.ProAdminDTO;
import com.felicita.entidades.Cita;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.ProAdmin;
import com.felicita.entidades.Usuario;
import com.felicita.excepciones.EstablecimientoExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ServicioRepositorio;
import com.felicita.repositorios.ProAdminRepositorio;
import com.felicita.repositorios.UsuarioRepositorio;
import com.felicita.servicios.ProAdminServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    @Autowired
    private ServicioRepositorio servicioRepositorio;

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
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion(
                        "ProAdmin no encontrado para usuario con ID: " + usuarioId));
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

    @Override
    @Transactional(readOnly = true)
    public EstadisticasDTO obtenerEstadisticasEstablecimiento(Long establecimientoId) {
        EstadisticasDTO estadisticas = new EstadisticasDTO();

        // Verificar que el establecimiento pertenece al ProAdmin
        ProAdmin proAdmin = obtenerProAdminEntidadPorUsuarioAutenticado();
        Establecimiento establecimiento = establecimientoRepositorio.findById(establecimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));

        if (!establecimiento.getProAdmin().getId().equals(proAdmin.getId())) {
            throw new EstablecimientoExcepcion("No tienes permiso para ver las estadísticas de este establecimiento");
        }

        // Contar servicios del establecimiento
        long totalServicios = servicioRepositorio.findByEstablecimiento(establecimiento).size();
        estadisticas.agregarDatoAdicional("totalServicios", totalServicios);

        // Contar citas del establecimiento
        List<Long> establecimientoIds = Arrays.asList(establecimientoId);

        long citasTotales = citaRepositorio.countByEstablecimientoIdIn(establecimientoIds);
        estadisticas.setTotalCitas(citasTotales);

        // Citas pendientes
        long citasPendientes = citaRepositorio.countByEstablecimientoIdInAndEstado(
                establecimientoIds, Cita.EstadoCita.PENDIENTE);
        estadisticas.setCitasPendientes(citasPendientes);

        // Citas confirmadas
        long citasConfirmadas = citaRepositorio.countByEstablecimientoIdInAndEstado(
                establecimientoIds, Cita.EstadoCita.CONFIRMADA);
        estadisticas.setCitasConfirmadas(citasConfirmadas);

        // Citas completadas
        long citasCompletadas = citaRepositorio.countByEstablecimientoIdInAndEstado(
                establecimientoIds, Cita.EstadoCita.COMPLETADA);
        estadisticas.setCitasCompletadas(citasCompletadas);

        // Contar clientes únicos
        Set<Long> clientesUnicos = new HashSet<>();
        List<Cita> todasLasCitas = citaRepositorio.findByEstablecimiento(establecimiento);
        for (Cita cita : todasLasCitas) {
            clientesUnicos.add(cita.getCliente().getId());
        }
        estadisticas.agregarDatoAdicional("clientesTotales", clientesUnicos.size());

        // Citas por día para gráfico (últimos 7 días)
        List<Map<String, Object>> citasPorDia = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);
            LocalDateTime inicioDelDia = fecha.atStartOfDay();
            LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

            long citasDelDia = citaRepositorio.countByEstablecimientoIdInAndFechaHoraBetween(
                    establecimientoIds, inicioDelDia, finDelDia);

            Map<String, Object> diaData = new HashMap<>();
            diaData.put("fecha", fecha.format(DateTimeFormatter.ofPattern("dd/MM")));
            diaData.put("cantidad", citasDelDia);
            citasPorDia.add(diaData);
        }
        estadisticas.agregarDatoAdicional("citasPorDia", citasPorDia);

        return estadisticas;
    }

}