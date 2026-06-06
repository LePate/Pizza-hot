package com.integrador.Pittzeria.model.dto;

import com.google.common.base.Preconditions;
import com.integrador.Pittzeria.model.entity.Producto;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Data
public class CarritoTemporal {

    // Logback: logger para operaciones del carrito
    private static final Logger log = LoggerFactory.getLogger(CarritoTemporal.class);

    private List<ItemCarrito> items = new ArrayList<>();
    private BigDecimal total    = BigDecimal.ZERO;
    private BigDecimal igv      = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    // ─────────────────────────────────────────────
    // Agregar un producto o aumentar cantidad si ya existe
    // ─────────────────────────────────────────────
    public void agregarProducto(Producto producto, Integer cantidad) {

        // Guava Preconditions: valida que los argumentos sean válidos
        // antes de operar sobre el carrito — lanza IllegalArgumentException si falla
        Preconditions.checkNotNull(producto, "El producto no puede ser nulo");
        Preconditions.checkArgument(cantidad != null && cantidad > 0,
                "La cantidad debe ser mayor a 0, se recibió: %s", cantidad);
        Preconditions.checkArgument(producto.getStock() >= cantidad,
                "Stock insuficiente para '%s': disponible %s, solicitado %s",
                producto.getNombreProducto(), producto.getStock(), cantidad);

        for (ItemCarrito item : items) {
            if (item.getProducto().getIdProducto().equals(producto.getIdProducto())) {
                item.setCantidad(item.getCantidad() + cantidad);
                item.recalcularSubtotal();
                calcularTotales();
                log.info("Carrito actualizado | Producto: '{}' | Nueva cantidad: {}",
                        producto.getNombreProducto(), item.getCantidad());
                return;
            }
        }

        items.add(new ItemCarrito(producto, cantidad));
        calcularTotales();

        log.info("Producto agregado al carrito | '{}' x{} | Subtotal: S/ {}",
                producto.getNombreProducto(), cantidad,
                producto.getPrecio().multiply(new BigDecimal(cantidad)));
    }

    // ─────────────────────────────────────────────
    // Remover un producto del carrito
    // ─────────────────────────────────────────────
    public void removerProducto(Integer idProducto) {
        // Guava Preconditions: el ID no puede ser nulo
        Preconditions.checkNotNull(idProducto, "El ID de producto no puede ser nulo");

        items.removeIf(item -> item.getProducto().getIdProducto().equals(idProducto));
        calcularTotales();

        log.info("Producto ID {} removido del carrito", idProducto);
    }

    public void limpiar() {
        items.clear();
        total    = BigDecimal.ZERO;
        igv      = BigDecimal.ZERO;
        subtotal = BigDecimal.ZERO;
        log.info("Carrito limpiado tras registro de venta");
    }

    // ─────────────────────────────────────────────
    // RF04: Cálculo exacto con BigDecimal y redondeo HALF_UP
    // ─────────────────────────────────────────────
    private void calcularTotales() {
        BigDecimal sumaTotal = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            sumaTotal = sumaTotal.add(item.getSubtotal());
        }

        this.total    = sumaTotal.setScale(2, RoundingMode.HALF_UP);

        // Desglose del IGV (18%) a partir del total integrado
        BigDecimal divisor = new BigDecimal("1.18");
        this.subtotal = this.total.divide(divisor, 2, RoundingMode.HALF_UP);
        this.igv      = this.total.subtract(this.subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    // ─────────────────────────────────────────────
    // Subclase interna: cada línea del carrito
    // ─────────────────────────────────────────────
    @Data
    public static class ItemCarrito {
        private Producto producto;
        private Integer cantidad;
        private BigDecimal subtotal;

        public ItemCarrito(Producto producto, Integer cantidad) {
            // Guava Preconditions: validación al construir el ítem
            Preconditions.checkNotNull(producto, "Producto nulo al crear ItemCarrito");
            Preconditions.checkArgument(cantidad > 0,
                    "Cantidad debe ser positiva al crear ItemCarrito, se recibió: %s", cantidad);
            this.producto = producto;
            this.cantidad = cantidad;
            recalcularSubtotal();
        }

        public void recalcularSubtotal() {
            this.subtotal = this.producto.getPrecio()
                    .multiply(new BigDecimal(this.cantidad));
        }
    }
}