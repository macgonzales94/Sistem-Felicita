package com.felicita.controladores;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ControladorErrorPersonalizado implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String mensajeError = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        if (mensajeError == null || mensajeError.isEmpty()) {
            mensajeError = "Ha ocurrido un error inesperado";
        }
        
        model.addAttribute("mensajeError", mensajeError);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            model.addAttribute("codigoError", statusCode);
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("titulo", "Página no encontrada");
                return "error/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("titulo", "Acceso denegado");
                return "error/403";
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                model.addAttribute("titulo", "No autorizado");
                return "error/403";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("titulo", "Error interno del servidor");
                return "error/500";
            }
        }
        
        model.addAttribute("titulo", "Error");
        return "error/error";
    }
}