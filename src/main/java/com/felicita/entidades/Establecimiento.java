package com.felicita.entidades;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "establecimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_establecimiento")
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    @Column(name = "horarios_atencion", length = 300)
    private String horariosAtencion;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "esta_activo")
    private boolean estaActivo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_admin_id")
    private ProAdmin proAdmin;

    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Servicio> servicios = new HashSet<>();

    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL)
    private Set<Cita> citas = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "establecimiento_caracteristicas", joinColumns = @JoinColumn(name = "establecimiento_id"))
    @Column(name = "caracteristica")
    private Set<String> caracteristicas = new HashSet<>();

    // ===== CAMPOS AGREGADOS PARA CORREGIR EL PROBLEMA =====
    
    @Column(name = "hora_apertura", length = 5)
    private String horaApertura = "09:00";

    @Column(name = "hora_cierre", length = 5)
    private String horaCierre = "18:00";

    @ElementCollection
    @CollectionTable(name = "establecimiento_dias_atencion", joinColumns = @JoinColumn(name = "establecimiento_id"))
    @Column(name = "dia")
    private Set<String> diasAtencion = new HashSet<>();

    @Column(name = "duracion_cita_defecto")
    private Integer duracionCitaDefecto = 30;

    @Column(name = "intervalos_citas")
    private Integer intervalosCitas = 15;

    @Column(name = "referencias", length = 300)
    private String referencias;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = fechaRegistro;
        
        // Establecer valores por defecto si no se han establecido
        if (horaApertura == null) {
            horaApertura = "09:00";
        }
        if (horaCierre == null) {
            horaCierre = "18:00";
        }
        if (duracionCitaDefecto == null) {
            duracionCitaDefecto = 30;
        }
        if (intervalosCitas == null) {
            intervalosCitas = 15;
        }
        if (diasAtencion == null) {
            diasAtencion = new HashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Método para agregar un servicio
    public void agregarServicio(Servicio servicio) {
        servicios.add(servicio);
        servicio.setEstablecimiento(this);
    }

    // Método para eliminar un servicio
    public void eliminarServicio(Servicio servicio) {
        servicios.remove(servicio);
        servicio.setEstablecimiento(null);
    }

    // Método para agregar una característica
    public void agregarCaracteristica(String caracteristica) {
        if (caracteristicas == null) {
            caracteristicas = new HashSet<>();
        }
        caracteristicas.add(caracteristica);
    }

    // Método para eliminar una característica
    public void eliminarCaracteristica(String caracteristica) {
        if (caracteristicas != null) {
            caracteristicas.remove(caracteristica);
        }
    }
    
    // Método para agregar día de atención
    public void agregarDiaAtencion(String dia) {
        if (diasAtencion == null) {
            diasAtencion = new HashSet<>();
        }
        diasAtencion.add(dia);
    }
    
    // Método para eliminar día de atención
    public void eliminarDiaAtencion(String dia) {
        if (diasAtencion != null) {
            diasAtencion.remove(dia);
        }
    }
}