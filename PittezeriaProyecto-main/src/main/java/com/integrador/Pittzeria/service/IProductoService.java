package com.integrador.Pittzeria.service;

import com.integrador.Pittzeria.model.entity.Producto;
import java.math.BigDecimal;
import java.util.List;

public interface IProductoService {
    List<Producto> listarTodos(); // RF02: Cargar catálogo
    Producto buscarPorId(Integer id);
    boolean validarStock(Integer idProducto, Integer cantidad); // RF03: Validación en tiempo real
    Producto actualizarStock(Integer idProducto, Integer nuevoStock); // RF08: Control de stock (Adm)
    Producto controlStock(Integer idProducto, String tipoMovimiento, Integer cantidad); // RF08: Entrada / Salida / Ajuste
    List<Producto> verificarAlertasStock(); // RF08: Alertas de nivel mínimo
    Producto modificarPrecio(Integer idProducto, BigDecimal nuevoPrecio); // RF09: Gestión de precios
    void cambiarEstadoArticulo(Integer idProducto, boolean activo); // RF09: Desactivar artículos
    Producto registrarProducto(String nombre, BigDecimal precio, Integer stock, Integer idCategoria);
}