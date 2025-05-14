package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "administradores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "nivel_acceso")
    private Integer nivelAcceso;
    
    @Column(name = "departamento")
    private String departamento;
    
    @Column(name = "cargo")
    private String cargo;
    
    @Column(name = "puede_gestionar_usuarios")
    private boolean puedeGestionarUsuarios;
    
    @Column(name = "puede_gestionar_establecimientos")
    private boolean puedeGestionarEstablecimientos;
    
    @Column(name = "puede_gestionar_reportes")
    private boolean puedeGestionarReportes;
    
    @Column(name = "puede_gestionar_configuracion")
    private boolean puedeGestionarConfiguracion;
}