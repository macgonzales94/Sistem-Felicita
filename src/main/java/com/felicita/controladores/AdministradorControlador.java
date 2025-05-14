package com.felicita.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdministradorControlador {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "admin/dashboard";
    }
    
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        return "admin/usuarios";
    }
    
    @GetMapping("/establecimientos")
    public String establecimientos(Model model) {
        return "admin/establecimientos";
    }
    
    @GetMapping("/reportes")
    public String reportes(Model model) {
        return "admin/reportes";
    }
    
    @GetMapping("/configuracion")
    public String configuracion(Model model) {
        return "admin/configuracion";
    }
}