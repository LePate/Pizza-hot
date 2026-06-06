package com.integrador.Pittzeria.controller;

import com.integrador.Pittzeria.model.entity.Producto;
import com.integrador.Pittzeria.repository.CategoriaRepository;
import com.integrador.Pittzeria.service.IProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin") // Cambiamos la base a /admin para flexibilizar las sub-rutas
public class ProductoAdminController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // /admin/inventario redirige ahora a /admin/productos (modulo unificado)
    @GetMapping("/inventario")
    public String listarInventario() {
        return "redirect:/admin/productos";
    }

    // URL: http://localhost:8080/admin/productos
    // RF08 + RF09: Vista unificada de catálogo con precios y control de stock
    @GetMapping("/productos")
    public String listarCatalogoPrecios(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("alertas", productoService.verificarAlertasStock());
        return "admin/precios";
    }

    // Procesa el registro de un nuevo producto
    @PostMapping("/productos/nuevo")
    public String registrarNuevoProducto(@RequestParam String nombreProducto,
                                         @RequestParam BigDecimal precio,
                                         @RequestParam Integer stock,
                                         @RequestParam Integer idCategoria,
                                         RedirectAttributes flash) {
        try {
            productoService.registrarProducto(nombreProducto, precio, stock, idCategoria);
            flash.addFlashAttribute("exito", "Producto registrado en la carta con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar producto: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    // Procesa la actualización del stock desde los modales de la tabla
    @PostMapping("/productos/actualizar-stock")
    public String actualizarStock(@RequestParam Integer idProducto,
                                  @RequestParam Integer nuevoStock,
                                  RedirectAttributes flash) {
        try {
            productoService.actualizarStock(idProducto, nuevoStock);
            flash.addFlashAttribute("exito", "Stock actualizado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al actualizar stock: " + e.getMessage());
        }
        return "redirect:/admin/inventario"; // Redirige a la tabla para ver el cambio
    }

    // RF08: Control de stock con movimiento (Entrada / Salida / Ajuste)
    @PostMapping("/productos/control-stock")
    public String controlStock(@RequestParam Integer idProducto,
                               @RequestParam String tipoMovimiento,
                               @RequestParam Integer cantidad,
                               RedirectAttributes flash) {
        try {
            productoService.controlStock(idProducto, tipoMovimiento, cantidad);
            String msg = switch (tipoMovimiento.toUpperCase()) {
                case "ENTRADA" -> "Entrada de stock registrada correctamente.";
                case "SALIDA"  -> "Salida de stock registrada correctamente.";
                case "AJUSTE"  -> "Ajuste de stock aplicado correctamente.";
                default        -> "Movimiento registrado.";
            };
            flash.addFlashAttribute("exito", msg);
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error en control de stock: " + e.getMessage());
        }
        return "redirect:/admin/productos";
    }

    // Procesa el cambio de precio comercial desde los modales de las cards
    @PostMapping("/productos/modificar-precio")
    public String modificarPrecio(@RequestParam Integer idProducto,
                                  @RequestParam BigDecimal nuevoPrecio,
                                  RedirectAttributes flash) {
        try {
            productoService.modificarPrecio(idProducto, nuevoPrecio);
            flash.addFlashAttribute("exito", "Precio modificado con éxito.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al modificar precio.");
        }
        return "redirect:/admin/productos"; // Redirige al catálogo de cards
    }

    // Desactivar artículos
    @PostMapping("/productos/desactivar/{id}")
    public String desactivarArticulo(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            productoService.cambiarEstadoArticulo(id, false);
            flash.addFlashAttribute("exito", "Artículo desactivado temporalmente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se pudo cambiar el estado del artículo.");
        }
        return "redirect:/admin/productos";
    }
}