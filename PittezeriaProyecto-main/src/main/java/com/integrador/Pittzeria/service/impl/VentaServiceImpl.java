package com.integrador.Pittzeria.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.integrador.Pittzeria.model.dto.CarritoTemporal;
import com.integrador.Pittzeria.model.entity.DetalleVenta;
import com.integrador.Pittzeria.model.entity.Empleado;
import com.integrador.Pittzeria.model.entity.Producto;
import com.integrador.Pittzeria.model.entity.Venta;
import com.integrador.Pittzeria.repository.EmpleadoRepository;
import com.integrador.Pittzeria.repository.ProductoRepository;
import com.integrador.Pittzeria.repository.VentaRepository;
import com.integrador.Pittzeria.service.IVentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VentaServiceImpl implements IVentaService {

    // Logback: logger para trazabilidad de todas las operaciones de venta
    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // ─────────────────────────────────────────────
    // RF04 y RF05: Registrar una venta desde el carrito
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public Venta registrarVenta(CarritoTemporal carrito, String metodoPago, String nombreEmpleado) {

        // 1. Buscar el empleado (cajero) que registra la venta
        Empleado empleado = empleadoRepository.findByNombre(nombreEmpleado)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + nombreEmpleado));

        // 2. Crear el objeto Venta principal
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setTotal(carrito.getTotal());
        venta.setTipoEntrega(metodoPago); // Ej: "Efectivo", "Yape", "Plin"
        venta.setEmpleado(empleado);

        // 3. Convertir cada ItemCarrito en un DetalleVenta
        List<DetalleVenta> detalles = new ArrayList<>();
        for (CarritoTemporal.ItemCarrito item : carrito.getItems()) {
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setSubtotal(item.getSubtotal());
            detalle.setVenta(venta);
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);

        // 4. Guardar (el cascade ALL guarda también los detalles automáticamente)
        Venta ventaGuardada = ventaRepository.save(venta);

        // 5. Descontar el stock de cada producto vendido
        for (CarritoTemporal.ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();
            int nuevoStock = producto.getStock() - item.getCantidad();
            if (nuevoStock < 0) nuevoStock = 0;
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
            log.info("Stock descontado | Producto: '{}' | Vendido: {} | Stock restante: {}",
                    producto.getNombreProducto(), item.getCantidad(), nuevoStock);
        }

        // Logback: registrar la venta exitosa con ID y cajero
        log.info("Venta #{} registrada por '{}' | Método: {} | Total: S/ {}",
                ventaGuardada.getIdVenta(), nombreEmpleado, metodoPago, carrito.getTotal());

        // 6. Limpiar el carrito de sesión tras registrar
        carrito.limpiar();

        return ventaGuardada;
    }

    // ─────────────────────────────────────────────
    // RF06: Buscar venta por ID para emitir comprobante
    // ─────────────────────────────────────────────
    @Override
    public Venta buscarPorId(Integer idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + idVenta));
    }

    // ─────────────────────────────────────────────
    // RF07: Anular una venta (eliminación lógica o física)
    // ─────────────────────────────────────────────
    @Override
    @Transactional
    public void anularVenta(Integer idVenta) {
        Venta venta = buscarPorId(idVenta);

        // Logback: advertencia al anular — queda registro en el log del servidor
        log.warn("Venta #{} ANULADA | Empleado registrado: '{}' | Total anulado: S/ {}",
                idVenta, venta.getEmpleado().getNombre(), venta.getTotal());

        ventaRepository.delete(venta);
    }

    // ─────────────────────────────────────────────
    // RF10: Reporte de cierre de caja del día
    // ─────────────────────────────────────────────
    @Override
    public Map<String, Object> generarReporteCierreCaja(LocalDateTime fecha) {
        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia    = fecha.toLocalDate().atTime(23, 59, 59);

        List<Venta> ventasDelDia = ventaRepository.findByFechaBetween(inicioDia, finDia);

        // Guava ImmutableList: detalles de venta en solo lectura para evitar
        // modificaciones accidentales fuera del contexto transaccional
        ImmutableList<Venta> ventasInmutables = ImmutableList.copyOf(ventasDelDia);

        // Guava Multimap: agrupar ventas por método de pago (Efectivo / Yape / Plin)
        // Útil para mostrar subtotales por método en la vista cierre-caja.html
        Multimap<String, Venta> ventasPorMetodo = ArrayListMultimap.create();
        for (Venta v : ventasInmutables) {
            ventasPorMetodo.put(v.getTipoEntrega(), v);
        }

        // Calcular el total acumulado del día
        BigDecimal totalSistema = ventasInmutables.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Logback: registrar el cierre de caja generado
        log.info("Cierre de caja generado | Fecha: {} | Ventas: {} | Total: S/ {}",
                fecha.toLocalDate(), ventasInmutables.size(), totalSistema);

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("ventasCount",      ventasInmutables.size());
        reporte.put("totalSistema",     totalSistema);
        reporte.put("ventas",           ventasInmutables);
        reporte.put("ventasPorMetodo",  ventasPorMetodo.asMap()); // mapa agrupado para la vista

        return reporte;
    }

    // ─────────────────────────────────────────────
    // RF11: Ranking de pizzas más vendidas (para estadísticas)
    // ─────────────────────────────────────────────
    @Override
    public List<Object[]> obtenerRankingPizzas() {
        return ventaRepository.obtenerRankingPizzas();
    }

    // ─────────────────────────────────────────────
    // RF11: Historial de ventas por rango de fechas
    // ─────────────────────────────────────────────
    @Override
    public List<Venta> consultarHistorialFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin);
    }
}