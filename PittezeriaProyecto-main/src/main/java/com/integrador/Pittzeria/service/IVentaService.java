package com.integrador.Pittzeria.service;

import com.integrador.Pittzeria.model.entity.Venta;
import com.integrador.Pittzeria.model.dto.CarritoTemporal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IVentaService {
    Venta registrarVenta(CarritoTemporal carrito, String metodoPago, String nombreEmpleado); // RF04 y RF05
    Venta buscarPorId(Integer idVenta); // RF06: Emisión de comprobantes
    void anularVenta(Integer idVenta); // RF07: Anulación de pedidos
    Map<String, Object> generarReporteCierreCaja(LocalDateTime fecha); // RF10: Reporte diario
    List<Object[]> obtenerRankingPizzas(); // RF11: Estadísticas
    List<Venta> consultarHistorialFechas(LocalDateTime inicio, LocalDateTime fin); // RF11: Historial
}