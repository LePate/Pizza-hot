package com.integrador.Pittzeria.service.impl;

import com.integrador.Pittzeria.model.entity.Categoria;
import com.integrador.Pittzeria.model.entity.Producto;
import com.integrador.Pittzeria.repository.CategoriaRepository;
import com.integrador.Pittzeria.repository.ProductoRepository;
import com.integrador.Pittzeria.service.IProductoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoServiceImpl implements IProductoService {

    // Logback: logger para trazabilidad de operaciones sobre productos
    private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto buscarPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    @Override
    public boolean validarStock(Integer idProducto, Integer cantidad) {
        Producto producto = buscarPorId(idProducto);
        // RF03: Verifica si hay stock disponible antes de vender
        return producto.getStock() >= cantidad;
    }

    @Override
    public Producto actualizarStock(Integer idProducto, Integer nuevoStock) {
        Producto producto = buscarPorId(idProducto);
        producto.setStock(nuevoStock);

        // Logback: registrar cambio de stock para auditoría
        log.info("Stock actualizado | Producto: '{}' (ID: {}) | Nuevo stock: {}",
                producto.getNombreProducto(), idProducto, nuevoStock);

        return productoRepository.save(producto);
    }

    @Override
    public List<Producto> verificarAlertasStock() {
        // RF08: Alerta si el stock es menor o igual a 5 unidades
        List<Producto> alertas = productoRepository.findByStockLessThan(6);

        // Logback: advertencia si hay productos con stock crítico
        if (!alertas.isEmpty()) {
            log.warn("ALERTA DE STOCK: {} producto(s) con stock crítico (≤5 unidades)", alertas.size());
        }

        return alertas;
    }

    @Override
    public Producto modificarPrecio(Integer idProducto, BigDecimal nuevoPrecio) {
        Producto producto = buscarPorId(idProducto);
        producto.setPrecio(nuevoPrecio);

        // Logback: registrar cambio de precio
        log.info("Precio actualizado | Producto: '{}' (ID: {}) | Nuevo precio: S/ {}",
                producto.getNombreProducto(), idProducto, nuevoPrecio);

        return productoRepository.save(producto);
    }

    @Override
    public void cambiarEstadoArticulo(Integer idProducto, boolean activo) {
        // RF09: setear stock a 0 si el producto se desactiva
        Producto producto = buscarPorId(idProducto);

        if (!activo) {
            producto.setStock(0);

            // Logback: advertencia al desactivar un producto
            log.warn("Producto DESACTIVADO | '{}' (ID: {}) | Stock seteado a 0",
                    producto.getNombreProducto(), idProducto);
        }

        productoRepository.save(producto);
    }

    // ─────────────────────────────────────────────
    // RF08: Control de stock con tipo de movimiento
    // Tipos: "ENTRADA" (+cantidad), "SALIDA" (-cantidad), "AJUSTE" (valor exacto)
    // ─────────────────────────────────────────────
    @Override
    public Producto controlStock(Integer idProducto, String tipoMovimiento, Integer cantidad) {
        Producto producto = buscarPorId(idProducto);
        int stockActual = producto.getStock();
        int stockNuevo;

        switch (tipoMovimiento.toUpperCase()) {
            case "ENTRADA":
                stockNuevo = stockActual + cantidad;
                log.info("ENTRADA de stock | Producto: '{}' | Cantidad: +{} | {} → {}",
                        producto.getNombreProducto(), cantidad, stockActual, stockNuevo);
                break;
            case "SALIDA":
                if (cantidad > stockActual) {
                    throw new RuntimeException(
                            "Stock insuficiente para registrar salida. Disponible: " + stockActual + ", Solicitado: " + cantidad);
                }
                stockNuevo = stockActual - cantidad;
                log.warn("SALIDA de stock | Producto: '{}' | Cantidad: -{} | {} → {}",
                        producto.getNombreProducto(), cantidad, stockActual, stockNuevo);
                break;
            case "AJUSTE":
                stockNuevo = cantidad;
                log.info("AJUSTE de stock | Producto: '{}' | Anterior: {} → Nuevo: {}",
                        producto.getNombreProducto(), stockActual, stockNuevo);
                break;
            default:
                throw new RuntimeException("Tipo de movimiento no válido: " + tipoMovimiento);
        }

        producto.setStock(stockNuevo);
        return productoRepository.save(producto);
    }

    // ─────────────────────────────────────────────
    // Commons Lang StringUtils: validación y normalización del nombre
    // ─────────────────────────────────────────────

    /**
     * Valida que el nombre del producto no esté vacío o en blanco
     * antes de intentar guardarlo.
     * StringUtils.isBlank() cubre: null, "", "   "
     */
    public boolean validarNombreProducto(String nombre) {
        if (StringUtils.isBlank(nombre)) {
            log.warn("Intento de guardar producto con nombre vacío o nulo");
            return false;
        }
        return true;
    }

    /**
     * Normaliza el nombre del producto: elimina espacios extra en los extremos
     * y capitaliza la primera letra.
     * Ejemplo: "  PIZZA HAWAIANA  " → "Pizza hawaiana"
     */
    public String normalizarNombre(String nombre) {
        if (StringUtils.isBlank(nombre)) return "";
        // trim() + capitalize(): primera letra mayúscula, resto minúsculas
        return StringUtils.capitalize(nombre.trim().toLowerCase());
    }

    @Override
    public Producto registrarProducto(String nombre, BigDecimal precio, Integer stock, Integer idCategoria) {
        if (!validarNombreProducto(nombre)) {
            throw new RuntimeException("Nombre de producto no válido o vacío");
        }
        String nombreNormalizado = normalizarNombre(nombre);

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + idCategoria));

        Producto producto = new Producto();
        producto.setNombreProducto(nombreNormalizado);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoria(categoria);

        log.info("Nuevo producto registrado | Nombre: '{}' | Precio: S/ {} | Stock: {} | Categoría: {}",
                nombreNormalizado, precio, stock, categoria.getNombreCategoria());

        return productoRepository.save(producto);
    }
}