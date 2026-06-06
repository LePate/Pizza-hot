package com.integrador.Pittzeria.model.dto;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.List;

public class VentaDTO {

    private BigDecimal total;
    private String tipoEntrega;
    private List<String> detalles;

    // ✅ GUAVA: Devuelve los detalles como lista inmutable
    public ImmutableList<String> getDetallesInmutables() {
        if (detalles == null) return ImmutableList.of();
        return ImmutableList.copyOf(detalles);
    }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public List<String> getDetalles() { return detalles; }
    public void setDetalles(List<String> detalles) { this.detalles = detalles; }
}
