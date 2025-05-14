package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pro_admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @OneToMany(mappedBy = "proAdmin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Establecimiento> establecimientos = new HashSet<>();
    
    @Column(name = "documento_identidad")
    private String documentoIdentidad;
    
    @Column(name = "direccion")
    private String direccion;
    
    @Column(name = "ciudad")
    private String ciudad;
    
    @Column(name = "telefono_contacto")
    private String telefonoContacto;
    
    @Column(name = "plan_subscripcion")
    private String planSubscripcion;
    
    @Column(name = "metodo_pago")
    private String metodoPago;
    
    // Método para agregar un establecimiento a este proAdmin
    public void agregarEstablecimiento(Establecimiento establecimiento) {
        establecimientos.add(establecimiento);
        establecimiento.setProAdmin(this);
    }
    
    // Método para eliminar un establecimiento de este proAdmin
    public void eliminarEstablecimiento(Establecimiento establecimiento) {
        establecimientos.remove(establecimiento);
        establecimiento.setProAdmin(null);
    }
}