package com.integrador.Pittzeria.controller;

import com.integrador.Pittzeria.service.IProductoService;
import com.integrador.Pittzeria.service.IVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private IProductoService productoService;

    // Renderiza la pantalla de login personalizada
    @GetMapping("/login")
    public String login() {
        return "login"; // Busca login.html en templates/
    }

    // Página de inicio o bienvenida del sistema
    @GetMapping({"/", "/index"})
    public String index(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

            if (isAdmin) {
                // Obtener datos del día actual para el Dashboard
                Map<String, Object> datosCaja = ventaService.generarReporteCierreCaja(LocalDateTime.now());
                model.addAttribute("ventasCount", datosCaja.get("ventasCount"));
                model.addAttribute("totalSistema", datosCaja.get("totalSistema"));
                model.addAttribute("ventas", datosCaja.get("ventas"));
                model.addAttribute("alertas", productoService.verificarAlertasStock());
                model.addAttribute("productosCount", productoService.listarTodos().size());
                return "index"; // Carga index.html
            } else {
                return "redirect:/venta/nueva"; // Vista inicial para cajero
            }
        }
        return "index";
    }
}