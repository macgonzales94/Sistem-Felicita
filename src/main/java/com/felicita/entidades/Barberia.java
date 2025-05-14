package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "barberias")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("BARBERIA")
public class Barberia extends Establecimiento {

    @Column(name = "especialidad_cortes")
    private String especialidadCortes;
    
    @Column(name = "tiene_servicios_barba")
    private boolean tieneServiciosBarba;
    
    @Column(name = "tiene_servicios_faciales")
    private boolean tieneServiciosFaciales;
    
    @Column(name = "estilo_barberia")
    private String estiloBarberia;
    
    @Column(name = "aforo_maximo")
    private Integer aforoMaximo;
}