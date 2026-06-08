package com.empresa.sistema_ventas.repository;

import com.empresa.sistema_ventas.entity.SolicitudTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudTransferenciaRepository extends JpaRepository<SolicitudTransferencia, Long> {

    // Solicitudes pendientes dirigidas a una sucursal (el bodeguero las acepta)
    List<SolicitudTransferencia> findBySucursalOrigen_IdAndEstadoOrderByFechaSolicitudDesc(Long sucursalId, String estado);

    // Todas las solicitudes de una sucursal origen (para la tabla completa)
    List<SolicitudTransferencia> findBySucursalOrigen_IdOrderByFechaSolicitudDesc(Long sucursalId);

    // Contar pendientes para el badge del sidebar
    long countBySucursalOrigen_IdAndEstado(Long sucursalId, String estado);
}
