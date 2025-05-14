package com.felicita.dto;

import com.felicita.entidades.Cita;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaClienteDTO {
    private Long id;
    
    private Long clienteId;
    
    @NotNull(message = "El establecimiento es obligatorio")
    private Long establecimientoId;
    private String nombreEstablecimiento;
    
    @NotNull(message = "El servicio es obligatorio")
    private Long servicioId;
    private String nombreServicio;
    private double precioServicio;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha y hora deben ser futuras")
    private LocalDateTime fechaHora;
    
    private Integer duracionMinutos;
    
    @Size(max = 500, message = "Los comentarios no pueden exceder los 500 caracteres")
    private String comentarios;
    
    private Cita.EstadoCita estado;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String codigoUnico;
    
    // Constructor adicional para crear desde entidad
    public CitaClienteDTO(Cita cita) {
        this.id = cita.getId();
        this.clienteId = cita.getCliente().getId();
        this.establecimientoId = cita.getEstablecimiento().getId();
        this.nombreEstablecimiento = cita.getEstablecimiento().getNombre();
        this.servicioId = cita.getServicio().getId();
        this.nombreServicio = cita.getServicio().getNombre();
        this.precioServicio = cita.getServicio().getPrecio().doubleValue();
        this.fechaHora = cita.getFechaHora();
        this.duracionMinutos = cita.getDuracionMinutos();
        this.comentarios = cita.getComentarios();
        this.estado = cita.getEstado();
        this.fechaCreacion = cita.getFechaCreacion();
        this.fechaActualizacion = cita.getFechaActualizacion();
        this.codigoUnico = cita.getCodigoUnico();
    }
}