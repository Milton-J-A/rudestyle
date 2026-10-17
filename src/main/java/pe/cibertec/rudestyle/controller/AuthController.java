package pe.cibertec.rudestyle.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.cibertec.rudestyle.model.Usuario;
import pe.cibertec.rudestyle.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping("/login")
    public String loginView() {
        return "login";
    }

    @PostMapping("/login")
    public String loginProcesar(@RequestParam(value = "correo", required = false) String correo,
                                @RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam(value = "clave", required = false) String clave,
                                @RequestParam(value = "contrasena", required = false) String contrasena,
                                HttpSession session,
                                Model model) {

        String credencialCorreo = (correo != null && !correo.isBlank()) ? correo : email;
        String credencialPassword = (password != null && !password.isBlank()) ? password : 
                                    (clave != null && !clave.isBlank()) ? clave : contrasena;

        if (credencialCorreo == null || credencialPassword == null) {
            model.addAttribute("error", "Por favor ingrese correo y contraseña");
            return "login";
        }

        // Buscar por 'correo' o 'email'
        Optional<Usuario> userOpt = usuarioRepo.findByCorreo(credencialCorreo);
        if (userOpt.isEmpty()) {
            userOpt = usuarioRepo.findByEmail(credencialCorreo);
        }

        if (userOpt.isPresent()) {
            Usuario u = userOpt.get();
            boolean claveCoincide = (u.getClave() != null && u.getClave().equals(credencialPassword)) ||
                                   (u.getContrasena() != null && u.getContrasena().equals(credencialPassword));

            if (claveCoincide) {
                session.setAttribute("usuarioLogueado", u);
                if (u.getRol() != null && u.getRol().toUpperCase().contains("ADMIN")) {
                    return "redirect:/admin/productos";
                }
                return "redirect:/catalogo";
            }
        }

        model.addAttribute("error", "Correo o contraseña incorrectos");
        return "login";
    }

    @GetMapping("/registro")
    public String registroView() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registroProcesar(@RequestParam("nombre") String nombre,
                                   @RequestParam("correo") String correo,
                                   @RequestParam("password") String password,
                                   @RequestParam(value = "telefono", required = false) String telefono,
                                   @RequestParam(value = "direccion", required = false) String direccion,
                                   HttpSession session,
                                   Model model) {

        if (usuarioRepo.findByCorreo(correo).isPresent()) {
            model.addAttribute("error", "El correo ya se encuentra registrado.");
            return "registro";
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setCorreo(correo);
        nuevo.setClave(password);
        nuevo.setContrasena(password);
        nuevo.setTelefono(telefono);
        nuevo.setDireccion(direccion);
        nuevo.setRol("CLIENTE");
        nuevo.setFechaRegistro(LocalDateTime.now());

        Usuario usuarioGuardado = usuarioRepo.save(nuevo);
        session.setAttribute("usuarioLogueado", usuarioGuardado);

        return "redirect:/catalogo";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}