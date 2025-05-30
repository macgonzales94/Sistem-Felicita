package com.felicita.servicios;

import com.felicita.dto.CitaProAdminDTO;
import java.util.List;

public interface CitaProAdminServicio {
    List<CitaProAdminDTO> obtenerCitasProAdmin(Long establecimientoId, String estado, String fecha);
    List<CitaProAdminDTO> obtenerProximasCitas();
    List<CitaProAdminDTO> obtenerCitasHoyPorEstablecimiento(Long establecimientoId);
    CitaProAdminDTO obtenerCitaPorId(Long id);
    CitaProAdminDTO confirmarCita(Long id);
    CitaProAdminDTO completarCita(Long id);
    CitaProAdminDTO cancelarCita(Long id);
    CitaProAdminDTO marcarNoAsistio(Long id);
}