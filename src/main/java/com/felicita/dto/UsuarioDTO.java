package com.felicita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoLogin;
    private boolean estaActivo;
    private Set<String> roles = new HashSet<>();
}