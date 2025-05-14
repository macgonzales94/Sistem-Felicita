package com.felicita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablecimientoDTO {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;
    
    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
    private String ciudad;
    
    @Size(max = 10, message = "El código postal no puede exceder los 10 caracteres")
    private String codigoPostal;
    
    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;
    
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;
    
    @Size(max = 200, message = "El sitio web no puede exceder los 200 caracteres")
    private String sitioWeb;
    
    @Size(max = 300, message = "Los horarios de atención no pueden exceder los 300 caracteres")
    private String horariosAtencion;
    
    private String imagenUrl;
    
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private boolean estaActivo;
    
    private Long proAdminId;
    
    private Set<String> caracteristicas = new HashSet<>();
    
    // Campos específicos para definir el tipo de establecimiento
    private String tipoEstablecimiento; // "BARBERIA" o "SALON_BELLEZA"
    
    // Campos específicos para Barbería
    private String especialidadCortes;
    private boolean tieneServiciosBarba;
    private boolean tieneServiciosFaciales;
    private String estiloBarberia;
    private Integer aforoMaximo;
    
    // Campos específicos para Salón de Belleza
    private String especialidad;
    private boolean tieneServiciosMaquillaje;
    private boolean tieneServiciosUnas;
    private boolean tieneTratamientosCapilares;
}