package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 30;
    
    @Column(name = "comentarios", length = 500)
    private String comentarios;
    
    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PENDIENTE;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @Column(name = "codigo_unico", length = 10)
    private String codigoUnico;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
        if (codigoUnico == null) {
            codigoUnico = generarCodigoUnico();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
    
    private String generarCodigoUnico() {
        // Generar un código único alfanumérico de 10 caracteres
        return "C" + System.currentTimeMillis() % 10000000000L;
    }
    
    public enum EstadoCita {
        PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, NO_ASISTIO
    }
}