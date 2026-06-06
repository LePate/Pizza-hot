package com.integrador.Pittzeria.service;

import com.integrador.Pittzeria.model.entity.Empleado;
import java.util.List;

public interface IEmpleadoService {
    Empleado buscarPorNombre(String nombre); // RF01: Para la autenticación
    List<Empleado> listarTodos();
    Empleado guardar(Empleado empleado);
}