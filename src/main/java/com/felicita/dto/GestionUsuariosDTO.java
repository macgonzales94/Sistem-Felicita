package com.felicita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GestionUsuariosDTO {
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    private String telefono;
    
    private String password;
    
    private boolean estaActivo;
    
    @NotNull(message = "El rol es obligatorio")
    private Set<String> roles = new HashSet<>();
    
    // Campos adicionales para administradores
    private String departamento;
    private String cargo;
    private Integer nivelAcceso;
    
    // Campos adicionales para clientes
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    
    // Campos adicionales para ProAdmin
    private String documentoIdentidad;
    private String planSubscripcion;
}