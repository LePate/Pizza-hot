package com.integrador.Pittzeria.controller;

import com.integrador.Pittzeria.model.entity.Venta;
import com.integrador.Pittzeria.service.IVentaService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reportes")
public class ReporteController {

    // Logback: logger para trazabilidad de reportes generados
    private static final Logger log = LoggerFactory.getLogger(ReporteController.class);

    @Autowired
    private IVentaService ventaService;

    // ─────────────────────────────────────────────
    // RF10: Reporte de Cierre de Caja diario para cuadre físico
    // ─────────────────────────────────────────────
    @GetMapping("/cierre-caja")
    public String verCierreCaja(Model model,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBusqueda) {

        LocalDate fechaFiltro = (fechaBusqueda != null) ? fechaBusqueda : LocalDate.now();
        LocalDateTime fechaCompleta = fechaFiltro.atStartOfDay();

        Map<String, Object> datosCaja = ventaService.generarReporteCierreCaja(fechaCompleta);

        model.addAttribute("fecha",           fechaFiltro);
        model.addAttribute("ventasCount",     datosCaja.get("ventasCount"));
        model.addAttribute("totalSistema",    datosCaja.get("totalSistema"));
        // Mapa agrupado por método de pago (generado con Guava Multimap en el service)
        model.addAttribute("ventasPorMetodo", datosCaja.get("ventasPorMetodo"));

        return "admin/cierre-caja";
    }

    // ─────────────────────────────────────────────
    // RF11: Estadísticas de venta (Ranking de Pizzas) e historial por fechas
    // ─────────────────────────────────────────────
    @GetMapping("/estadisticas")
    public String verEstadisticas(Model model,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        model.addAttribute("ranking", ventaService.obtenerRankingPizzas());

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin    = fechaFin.atTime(23, 59, 59);
            model.addAttribute("historial", ventaService.consultarHistorialFechas(inicio, fin));
        }

        return "admin/estadisticas";
    }

    // ─────────────────────────────────────────────
    // Apache POI: Exportar reporte de ventas del día a Excel (.xlsx)
    // Endpoint: GET /admin/reportes/exportar-excel?fecha=2025-06-01
    // ─────────────────────────────────────────────
    @GetMapping("/exportar-excel")
    public void exportarExcel(HttpServletResponse response,
                              @RequestParam(required = false)
                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha)
            throws IOException {

        LocalDate fechaExportar = (fecha != null) ? fecha : LocalDate.now();
        LocalDateTime inicio = fechaExportar.atStartOfDay();

        Map<String, Object> datos = ventaService.generarReporteCierreCaja(inicio);

        @SuppressWarnings("unchecked")
        List<Venta> ventas = (List<Venta>) datos.get("ventas");

        // Configurar cabeceras HTTP para descarga de archivo Excel
        String nombreArchivo = "reporte-ventas-" + fechaExportar + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);

        // Apache POI: crear el libro Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Ventas " + fechaExportar);

            // ── Estilos ──────────────────────────────────────────
            CellStyle estiloEncabezado = workbook.createCellStyle();
            Font fuenteNegrita = workbook.createFont();
            fuenteNegrita.setBold(true);
            estiloEncabezado.setFont(fuenteNegrita);
            estiloEncabezado.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Fila de encabezados ───────────────────────────────
            Row encabezado = hoja.createRow(0);
            String[] columnas = {"ID Venta", "Fecha", "Cajero", "Método de pago", "Total (S/)"};
            for (int i = 0; i < columnas.length; i++) {
                Cell celda = encabezado.createCell(i);
                celda.setCellValue(columnas[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // ── Filas de datos ────────────────────────────────────
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int numFila = 1;
            for (Venta v : ventas) {
                Row fila = hoja.createRow(numFila++);
                fila.createCell(0).setCellValue(v.getIdVenta());
                fila.createCell(1).setCellValue(v.getFecha().format(formatter));
                fila.createCell(2).setCellValue(v.getEmpleado().getNombre());
                fila.createCell(3).setCellValue(v.getTipoEntrega());
                fila.createCell(4).setCellValue(v.getTotal().doubleValue());
            }

            // ── Ajustar ancho de columnas automáticamente ─────────
            for (int i = 0; i < columnas.length; i++) {
                hoja.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());

            // Logback: confirmar generación del Excel
            log.info("Excel exportado | Fecha: {} | Ventas incluidas: {} | Archivo: {}",
                    fechaExportar, ventas.size(), nombreArchivo);
        }
    }
}