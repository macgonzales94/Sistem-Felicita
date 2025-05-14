package com.felicita.controladores;

import com.felicita.dto.JwtResponseDTO;
import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;
import com.felicita.servicios.AutenticacionServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    @Autowired
    private AutenticacionServicio autenticacionServicio;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        JwtResponseDTO respuesta = autenticacionServicio.autenticar(loginDTO);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registro(@Valid @RequestBody RegistroDTO registroDTO) {
        String respuesta = autenticacionServicio.registrar(registroDTO);
        return ResponseEntity.ok(respuesta);
    }
}