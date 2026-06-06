package com.integrador.Pittzeria.controller;

import com.integrador.Pittzeria.model.dto.CarritoTemporal;
import com.integrador.Pittzeria.model.entity.Producto;
import com.integrador.Pittzeria.model.entity.Venta;
import com.integrador.Pittzeria.service.IProductoService;
import com.integrador.Pittzeria.service.IVentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/venta")
@SessionAttributes("carrito") // El carrito se mantendrá vivo durante la sesión del usuario
public class VentaController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IVentaService ventaService;

    // Inicializa el carrito en la sesión si no existe
    @ModelAttribute("carrito")
    public CarritoTemporal inicializarCarrito() {
        return new CarritoTemporal();
    }

    // RF02: Cargar el catálogo actualizado de pizzas y bebidas
    @GetMapping("/nueva")
    public String nuevaVenta(Model model, @ModelAttribute("carrito") CarritoTemporal carrito, HttpSession session) {
        Boolean cajaAbierta = (Boolean) session.getAttribute("cajaAbierta");
        if (cajaAbierta == null || !cajaAbierta) {
            return "cajero/apertura-caja"; // Busca apertura-caja.html en templates/cajero/
        }
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("carrito", carrito);
        return "cajero/nueva-venta"; // Busca nueva-venta.html en templates/cajero/
    }

    // RF03 y RF04: Agregar producto al carrito validando stock
    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Integer idProducto,
                                  @RequestParam Integer cantidad,
                                  @ModelAttribute("carrito") CarritoTemporal carrito,
                                  RedirectAttributes flash) {

        // Validación en tiempo real
        if (!productoService.validarStock(idProducto, cantidad)) {
            flash.addFlashAttribute("error", "¡Stock insuficiente en base de datos local!");
            return "redirect:/venta/nueva";
        }

        Producto producto = productoService.buscarPorId(idProducto);
        carrito.agregarProducto(producto, cantidad);
        flash.addFlashAttribute("exito", "Producto agregado al pedido.");
        return "redirect:/venta/nueva";
    }

    // Eliminar un artículo del carrito temporal
    @GetMapping("/remover/{id}")
    public String removerProducto(@PathVariable Integer id, @ModelAttribute("carrito") CarritoTemporal carrito) {
        carrito.removerProducto(id);
        return "redirect:/venta/nueva";
    }

    // RF05: Registrar la transacción final
    @PostMapping("/confirmar")
    public String confirmarVenta(@RequestParam String metodoPago,
                                 @ModelAttribute("carrito") CarritoTemporal carrito,
                                 Principal principal, // Obtiene el empleado logueado
                                 HttpSession session,
                                 RedirectAttributes flash) {

        // Bloquear si la caja no está abierta
        Boolean cajaAbierta = (Boolean) session.getAttribute("cajaAbierta");
        if (cajaAbierta == null || !cajaAbierta) {
            flash.addFlashAttribute("error", "Debe abrir la caja antes de procesar una venta.");
            return "redirect:/venta/nueva";
        }

        if (carrito.getItems().isEmpty()) {
            flash.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/venta/nueva";
        }

        try {
            // Registrar venta y vaciar stock en BD
            Venta ventaGuardada = ventaService.registrarVenta(carrito, metodoPago, principal.getName());
            carrito.limpiar(); // Reseteamos el carrito de sesión
            return "redirect:/venta/ticket/" + ventaGuardada.getIdVenta();
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al procesar la venta: " + e.getMessage());
            return "redirect:/venta/nueva";
        }
    }

    // RF06: Mostrar la boleta o ticket digital listo para imprimir
    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, Model model) {
        Venta venta = ventaService.buscarPorId(id);
        model.addAttribute("venta", venta);
        return "cajero/ticket"; // Busca ticket.html en templates/cajero/
    }

    // RF07: Anulación de pedidos erróneos
    @PostMapping("/anular/{id}")
    public String anularPedido(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            ventaService.anularVenta(id);
            flash.addFlashAttribute("exito", "Venta anulada correctamente. El stock ha sido restaurado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo anular la venta: " + e.getMessage());
        }
        return "redirect:/venta/nueva";
    }

    // Procesa la apertura de la caja
    @PostMapping("/abrir-caja")
    public String abrirCaja(@RequestParam BigDecimal montoApertura, HttpSession session, RedirectAttributes flash) {
        if (montoApertura == null || montoApertura.compareTo(BigDecimal.ZERO) < 0) {
            flash.addFlashAttribute("error", "El monto de apertura debe ser un número positivo.");
            return "redirect:/venta/nueva";
        }
        session.setAttribute("cajaAbierta", true);
        session.setAttribute("montoApertura", montoApertura);
        flash.addFlashAttribute("exito", "Caja abierta con éxito. Monto inicial: S/ " + montoApertura);
        return "redirect:/venta/nueva";
    }

    // Procesa el cierre de la caja
    @PostMapping("/cerrar-caja")
    public String cerrarCaja(HttpSession session, RedirectAttributes flash) {
        session.removeAttribute("cajaAbierta");
        session.removeAttribute("montoApertura");
        flash.addFlashAttribute("exito", "Caja cerrada correctamente. Sesión de arqueo finalizada.");
        return "redirect:/venta/nueva";
    }
}