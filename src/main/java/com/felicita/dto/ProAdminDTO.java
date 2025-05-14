package com.felicita.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProAdminDTO {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    
    @Size(max = 20, message = "El documento de identidad no puede exceder los 20 caracteres")
    private String documentoIdentidad;
    
    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;
    
    @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
    private String ciudad;
    
    @Size(max = 20, message = "El teléfono de contacto no puede exceder los 20 caracteres")
    private String telefonoContacto;
    
    private String planSubscripcion;
    private String metodoPago;
    
    // Información adicional
    private long cantidadEstablecimientos;
}