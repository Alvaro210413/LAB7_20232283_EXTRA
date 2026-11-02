package edu.pucp.gtics.lab7_20232283.controller;

import edu.pucp.gtics.lab7_20232283.entity.Customer;
import edu.pucp.gtics.lab7_20232283.repository.CustomerRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @ModelAttribute("tiposDocumento")
    public List<String> tiposDocumento() {
        return List.of("DNI", "RUC");
    }

    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        model.addAttribute("customers", customerRepository.findAllByOrderByNameAsc());
        return "customer/list";
    }

    // Crear y editar usan la MISMA vista: customer/form
    @GetMapping("/new")
    public String nuevo(@ModelAttribute("customer") Customer customer) {
        return "customer/form";
    }

    @GetMapping("/edit")
    public String editar(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Optional<Customer> opt = customerRepository.findById(id);
        if (opt.isEmpty()) {
            attr.addFlashAttribute("error", "El cliente solicitado no existe");
            return "redirect:/customers";
        }
        model.addAttribute("customer", opt.get());
        return "customer/form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("customer") @Valid Customer customer,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        // Regla que depende de dos campos: longitud según el tipo de documento
        if (!bindingResult.hasFieldErrors("documentType") && !bindingResult.hasFieldErrors("document")) {
            String doc = customer.getDocument();
            if (customer.isDni() && doc.length() != 8) {
                bindingResult.rejectValue("document", "document.dni",
                        "El DNI debe contener exactamente 8 dígitos");
            } else if (customer.isRuc() && doc.length() != 11) {
                bindingResult.rejectValue("document", "document.ruc",
                        "El RUC debe contener exactamente 11 dígitos");
            }
        }

        // Documento único: no debe existir otro cliente (con distinto id) con el mismo número
        if (!bindingResult.hasFieldErrors("document")) {
            Optional<Customer> existente = customerRepository.findByDocument(customer.getDocument());
            if (existente.isPresent() && !existente.get().getId().equals(customer.getId())) {
                bindingResult.rejectValue("document", "document.duplicado",
                        "Ya existe un cliente registrado con el documento " + customer.getDocument()
                                + " (" + existente.get().getName() + ")");
            }
        }

        if (bindingResult.hasErrors()) {
            return "customer/form"; // se conservan los valores ingresados
        }

        boolean nuevo = customer.getId() == null;
        try {
            customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            // Respaldo por si la BD rechaza el duplicado (restricción UNIQUE)
            bindingResult.rejectValue("document", "document.duplicado",
                    "Ya existe un cliente registrado con ese número de documento");
            return "customer/form";
        }
        attr.addFlashAttribute("msg", nuevo ? "Cliente registrado correctamente"
                                            : "Cliente actualizado correctamente");
        return "redirect:/customers";
    }

    @PostMapping("/delete")
    public String borrar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        if (!customerRepository.existsById(id)) {
            attr.addFlashAttribute("error", "El cliente no existe");
            return "redirect:/customers";
        }
        try {
            customerRepository.deleteById(id);
            customerRepository.flush();
            attr.addFlashAttribute("msg", "Cliente eliminado correctamente");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error", "No se puede eliminar el cliente porque tiene pedidos registrados");
        }
        return "redirect:/customers";
    }
}
