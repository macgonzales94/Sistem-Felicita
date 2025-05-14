package com.felicita.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private Set<String> roles;
    
    public JwtResponseDTO(String token, Long id, String nombre, String apellido, String email, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.roles = roles;
    }
}