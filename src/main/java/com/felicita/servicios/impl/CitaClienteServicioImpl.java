package com.felicita.servicios.impl;

import com.felicita.dto.CitaClienteDTO;
import com.felicita.entidades.Cita;
import com.felicita.entidades.Cliente;
import com.felicita.entidades.Establecimiento;
import com.felicita.entidades.Servicio;
import com.felicita.excepciones.CitaExcepcion;
import com.felicita.excepciones.RecursoNoEncontradoExcepcion;
import com.felicita.repositorios.CitaRepositorio;
import com.felicita.repositorios.EstablecimientoRepositorio;
import com.felicita.repositorios.ServicioRepositorio;
import com.felicita.servicios.CitaClienteServicio;
import com.felicita.servicios.ClienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaClienteServicioImpl implements CitaClienteServicio {

    private static final int MAX_CITAS_PENDIENTES = 5;
    private static final int MIN_HORAS_ANTES_CANCELACION = 24;
    
    @Autowired
    private CitaRepositorio citaRepositorio;
    
    @Autowired
    private EstablecimientoRepositorio establecimientoRepositorio;
    
    @Autowired
    private ServicioRepositorio servicioRepositorio;
    
    @Autowired
    private ClienteServicio clienteServicio;

    @Override
    @Transactional
    public CitaClienteDTO crearCita(CitaClienteDTO citaDTO) {
        // Verificar si el cliente puede reservar más citas
        if (!puedeReservarCita()) {
            throw new CitaExcepcion("Has alcanzado el límite de citas pendientes: " + MAX_CITAS_PENDIENTES);
        }
        
        // Verificar disponibilidad
        if (!verificarDisponibilidad(citaDTO)) {
            throw new CitaExcepcion("La hora seleccionada no está disponible");
        }
        
        // Obtener entidades necesarias
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        
        Establecimiento establecimiento = establecimientoRepositorio.findById(citaDTO.getEstablecimientoId())
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Establecimiento no encontrado"));
        
        Servicio servicio = servicioRepositorio.findById(citaDTO.getServicioId())
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado"));
        
        // Validar que el servicio pertenece al establecimiento
        if (!servicio.getEstablecimiento().getId().equals(establecimiento.getId())) {
            throw new CitaExcepcion("El servicio no pertenece al establecimiento seleccionado");
        }
        
        // Crear la cita
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setEstablecimiento(establecimiento);
        cita.setServicio(servicio);
        cita.setFechaHora(citaDTO.getFechaHora());
        cita.setDuracionMinutos(servicio.getDuracionMinutos());
        cita.setComentarios(citaDTO.getComentarios());
        cita.setEstado(Cita.EstadoCita.PENDIENTE);
        
        // Guardar la cita
        // Guardar la cita
        Cita citaGuardada = citaRepositorio.save(cita);
        
        return new CitaClienteDTO(citaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaClienteDTO obtenerCitaPorId(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!cita.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new CitaExcepcion("No tienes permiso para ver esta cita");
        }
        
        return new CitaClienteDTO(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CitaClienteDTO> obtenerCitasCliente(Pageable pageable) {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        Page<Cita> citas = citaRepositorio.findByClienteOrderByFechaHoraDesc(cliente, pageable);
        
        return citas.map(CitaClienteDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaClienteDTO> obtenerCitasPendientes() {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        List<Cita> citas = citaRepositorio.findByClienteAndEstadoOrderByFechaHoraDesc(
                cliente, Cita.EstadoCita.PENDIENTE);
        
        // Filtrar solo citas futuras
        LocalDateTime ahora = LocalDateTime.now();
        return citas.stream()
                .filter(cita -> cita.getFechaHora().isAfter(ahora))
                .map(CitaClienteDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaClienteDTO> obtenerHistorialCitas() {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        
        // Obtener citas completadas o canceladas
        List<Cita> citasCompletadas = citaRepositorio.findByClienteAndEstadoOrderByFechaHoraDesc(
                cliente, Cita.EstadoCita.COMPLETADA);
        
        List<Cita> citasCanceladas = citaRepositorio.findByClienteAndEstadoOrderByFechaHoraDesc(
                cliente, Cita.EstadoCita.CANCELADA);
        
        List<Cita> citasNoAsistio = citaRepositorio.findByClienteAndEstadoOrderByFechaHoraDesc(
                cliente, Cita.EstadoCita.NO_ASISTIO);
        
        // Unir las listas y ordenar por fecha
        List<Cita> todasLasCitas = Stream.of(citasCompletadas, citasCanceladas, citasNoAsistio)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Cita::getFechaHora).reversed())
                .collect(Collectors.toList());
        
        return todasLasCitas.stream()
                .map(CitaClienteDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CitaClienteDTO cancelarCita(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!cita.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new CitaExcepcion("No tienes permiso para cancelar esta cita");
        }
        
        // Verificar que la cita esté pendiente
        if (cita.getEstado() != Cita.EstadoCita.PENDIENTE && cita.getEstado() != Cita.EstadoCita.CONFIRMADA) {
            throw new CitaExcepcion("Solo se pueden cancelar citas pendientes o confirmadas");
        }
        
        // Verificar tiempo mínimo antes de la cita
        LocalDateTime limiteCancelacion = cita.getFechaHora().minusHours(MIN_HORAS_ANTES_CANCELACION);
        if (LocalDateTime.now().isAfter(limiteCancelacion)) {
            throw new CitaExcepcion("Las citas deben cancelarse al menos " + MIN_HORAS_ANTES_CANCELACION + " horas antes");
        }
        
        // Cancelar la cita
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaClienteDTO(citaActualizada);
    }

    @Override
    @Transactional
    public CitaClienteDTO confirmarCita(Long id) {
        Cita cita = citaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con ID: " + id));
        
        // Verificar que la cita pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!cita.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new CitaExcepcion("No tienes permiso para confirmar esta cita");
        }
        
        // Verificar que la cita esté pendiente
        if (cita.getEstado() != Cita.EstadoCita.PENDIENTE) {
            throw new CitaExcepcion("Solo se pueden confirmar citas pendientes");
        }
        
        // Confirmar la cita
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        Cita citaActualizada = citaRepositorio.save(cita);
        
        return new CitaClienteDTO(citaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaClienteDTO obtenerCitaPorCodigo(String codigo) {
        Cita cita = citaRepositorio.findByCodigoUnico(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Cita no encontrada con código: " + codigo));
        
        // Verificar que la cita pertenezca al cliente autenticado
        Cliente clienteAutenticado = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        if (!cita.getCliente().getId().equals(clienteAutenticado.getId())) {
            throw new CitaExcepcion("No tienes permiso para ver esta cita");
        }
        
        return new CitaClienteDTO(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(CitaClienteDTO citaDTO) {
        // Verificar que la fecha no sea pasada
        if (citaDTO.getFechaHora().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Obtener servicio para conocer la duración
        Servicio servicio = servicioRepositorio.findById(citaDTO.getServicioId())
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("Servicio no encontrado"));
        
        // Calcular fecha fin de la cita
        LocalDateTime fechaInicio = citaDTO.getFechaHora();
        LocalDateTime fechaFin = fechaInicio.plusMinutes(servicio.getDuracionMinutos());
        
        // Buscar citas que puedan solaparse
        List<Cita> citasEnRango = citaRepositorio.findByEstablecimientoAndRangoFecha(
                citaDTO.getEstablecimientoId(), 
                fechaInicio.minusMinutes(servicio.getDuracionMinutos()),
                fechaFin.plusMinutes(servicio.getDuracionMinutos()));
        
        // Verificar si alguna cita se solapa
        for (Cita cita : citasEnRango) {
            // Solo considerar citas pendientes o confirmadas
            if (cita.getEstado() != Cita.EstadoCita.PENDIENTE && cita.getEstado() != Cita.EstadoCita.CONFIRMADA) {
                continue;
            }
            
            LocalDateTime otroInicio = cita.getFechaHora();
            LocalDateTime otroFin = otroInicio.plusMinutes(cita.getDuracionMinutos());
            
            // Verificar solapamiento
            if (!(fechaFin.isBefore(otroInicio) || fechaInicio.isAfter(otroFin))) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean puedeReservarCita() {
        Cliente cliente = clienteServicio.obtenerClienteEntidadPorUsuarioAutenticado();
        
        // Contar citas pendientes o confirmadas
        long citasPendientes = citaRepositorio.countCitasFuturasByCliente(cliente.getId(), LocalDateTime.now());
        
        return citasPendientes < MAX_CITAS_PENDIENTES;
    }
}