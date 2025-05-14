package com.felicita.dto;

import com.felicita.entidades.Cita;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaProAdminDTO {
    private Long id;
    
    private Long clienteId;
    private String nombreCliente;
    private String apellidoCliente;
    private String emailCliente;
    private String telefonoCliente;
    
    private Long establecimientoId;
    private String nombreEstablecimiento;
    
    private Long servicioId;
    private String nombreServicio;
    private double precioServicio;
    
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private String comentarios;
    private Cita.EstadoCita estado;
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String codigoUnico;
    
    private boolean estaCompleta;
    private boolean esCancelable;
    
    // Constructor adicional para crear desde entidad
    public CitaProAdminDTO(Cita cita) {
        this.id = cita.getId();
        this.clienteId = cita.getCliente().getId();
        this.nombreCliente = cita.getCliente().getUsuario().getNombre();
        this.apellidoCliente = cita.getCliente().getUsuario().getApellido();
        this.emailCliente = cita.getCliente().getUsuario().getEmail();
        this.telefonoCliente = cita.getCliente().getUsuario().getTelefono();
        
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
        
        this.estaCompleta = (cita.getEstado() == Cita.EstadoCita.COMPLETADA);
        this.esCancelable = (cita.getEstado() == Cita.EstadoCita.PENDIENTE || cita.getEstado() == Cita.EstadoCita.CONFIRMADA) 
                            && cita.getFechaHora().isAfter(LocalDateTime.now().plusHours(24));
    }
}