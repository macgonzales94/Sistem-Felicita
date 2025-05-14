package com.felicita.servicios;

import com.felicita.dto.CitaClienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CitaClienteServicio {
    CitaClienteDTO crearCita(CitaClienteDTO citaDTO);
    CitaClienteDTO obtenerCitaPorId(Long id);
    Page<CitaClienteDTO> obtenerCitasCliente(Pageable pageable);
    List<CitaClienteDTO> obtenerCitasPendientes();
    List<CitaClienteDTO> obtenerHistorialCitas();
    CitaClienteDTO cancelarCita(Long id);
    CitaClienteDTO confirmarCita(Long id);
    CitaClienteDTO obtenerCitaPorCodigo(String codigo);
    boolean verificarDisponibilidad(CitaClienteDTO citaDTO);
    boolean puedeReservarCita();
}