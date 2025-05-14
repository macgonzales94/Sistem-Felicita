package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100, nullable = false)
    private String nombre;
    
    @Column(length = 100, nullable = false)
    private String apellido;
    
    @Column(length = 100, unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(length = 20)
    private String telefono;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;
    
    @Column(name = "esta_activo")
    private boolean estaActivo = true;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuarios_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
    
    @PrePersist
    public void prePersist() {
        fechaRegistro = LocalDateTime.now();
    }
    
    public void agregarRol(Rol rol) {
        this.roles.add(rol);
    }
    
    public void eliminarRol(Rol rol) {
        this.roles.remove(rol);
    }
}