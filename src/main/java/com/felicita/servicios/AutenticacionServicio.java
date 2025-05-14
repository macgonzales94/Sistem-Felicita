package com.felicita.servicios;

import com.felicita.dto.JwtResponseDTO;
import com.felicita.dto.LoginDTO;
import com.felicita.dto.RegistroDTO;

public interface AutenticacionServicio {
    JwtResponseDTO autenticar(LoginDTO loginDTO);
    String registrar(RegistroDTO registroDTO);
}