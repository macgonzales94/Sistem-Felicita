package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salones_belleza")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("SALON_BELLEZA")
public class SalonBelleza extends Establecimiento {

    @Column(name = "especialidad")
    private String especialidad;
    
    @Column(name = "tiene_servicios_maquillaje")
    private boolean tieneServiciosMaquillaje;
    
    @Column(name = "tiene_servicios_unas")
    private boolean tieneServiciosUnas;
    
    @Column(name = "tiene_tratamientos_capilares")
    private boolean tieneTratamientosCapilares;
    
    @Column(name = "aforo_maximo")
    private Integer aforoMaximo;
}