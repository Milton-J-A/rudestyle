package pe.cibertec.rudestyle.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pe.cibertec.rudestyle.model.SolicitudPostventa;
import pe.cibertec.rudestyle.repository.SolicitudPostventaRepository;

@Controller
@RequestMapping("/admin/postventa")
public class AdminPostventaController {

    @Autowired
    private SolicitudPostventaRepository solicitudRepo;

    // 1. Cargar la vista con solo los tickets de cambios y devoluciones
    @GetMapping
    public String verModuloPostventa(Model model) {
        List<SolicitudPostventa> listaPostventa = solicitudRepo.findAllByOrderByFechaRegistroDesc()
                .stream()
                .filter(s -> "CAMBIO_TALLA".equals(s.getMotivo()) || "DEVOLUCION".equals(s.getMotivo()))
                .toList();

        model.addAttribute("postventas", listaPostventa);
        
        // Retorna la plantilla que tienes en templates/admin-postventa.html
        return "admin-postventa";
    }

    // 2. Procesar el cambio de estado (Pendiente, En Coordinación, Completado, Rechazado)
    @PostMapping("/cambiar-estado")
    public String cambiarEstadoPostventa(
            @RequestParam("idSolicitud") Integer idSolicitud,
            @RequestParam("nuevoEstado") String nuevoEstado) {
        
        solicitudRepo.findById(idSolicitud).ifPresent(solicitud -> {
            solicitud.setEstado(nuevoEstado);
            solicitudRepo.save(solicitud);
        });

        return "redirect:/admin/postventa";
    }
}