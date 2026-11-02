package edu.pucp.gtics.lab7_20232283.controller;

import edu.pucp.gtics.lab7_20232283.entity.Product;
import edu.pucp.gtics.lab7_20232283.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/*
 * ERRORES DE CONVERSIÓN (letras en precio o stock):
 * Se implementó la alternativa de messages.properties (NO @InitBinder).
 * Si el usuario escribe "abc" en precio (Double) o stock (Integer), el WebDataBinder
 * no puede convertir el valor, deja la propiedad en null y registra en el BindingResult
 * un error con código "typeMismatch". Los mensajes personalizados están en
 * src/main/resources/messages.properties:
 *   typeMismatch.product.price  y  typeMismatch.product.stock
 * Como bindingResult.hasErrors() es true, el producto NO se guarda y se vuelve a
 * mostrar el formulario con el valor rechazado y el mensaje del campo.
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        return "product/list";
    }

    // Crear y editar usan la MISMA vista: product/form
    @GetMapping("/new")
    public String nuevo(@ModelAttribute("product") Product product) {
        return "product/form";
    }

    @GetMapping("/edit")
    public String editar(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) {
            attr.addFlashAttribute("error", "El producto solicitado no existe");
            return "redirect:/products";
        }
        model.addAttribute("product", opt.get());
        return "product/form";
    }

    @PostMapping("/save")
    public String guardar(@ModelAttribute("product") @Valid Product product,
                          BindingResult bindingResult,
                          RedirectAttributes attr) {

        // Nombre único (sin distinguir mayúsculas/minúsculas)
        if (!bindingResult.hasFieldErrors("name")) {
            Optional<Product> existente = productRepository.findByNameIgnoreCase(product.getName());
            if (existente.isPresent() && !existente.get().getId().equals(product.getId())) {
                bindingResult.rejectValue("name", "name.duplicado",
                        "Ya existe un producto con el nombre \"" + product.getName() + "\"");
            }
        }

        // Errores de binding (typeMismatch) y de validación llegan aquí juntos
        if (bindingResult.hasErrors()) {
            return "product/form";
        }

        boolean nuevo = product.getId() == null;
        try {
            productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            bindingResult.rejectValue("name", "name.duplicado", "Ya existe un producto con ese nombre");
            return "product/form";
        }
        attr.addFlashAttribute("msg", nuevo ? "Producto registrado correctamente"
                                            : "Producto actualizado correctamente");
        return "redirect:/products";
    }

    @PostMapping("/delete")
    public String borrar(@RequestParam("id") Integer id, RedirectAttributes attr) {
        if (!productRepository.existsById(id)) {
            attr.addFlashAttribute("error", "El producto no existe");
            return "redirect:/products";
        }
        try {
            productRepository.deleteById(id);
            productRepository.flush();
            attr.addFlashAttribute("msg", "Producto eliminado correctamente");
        } catch (DataIntegrityViolationException e) {
            attr.addFlashAttribute("error", "No se puede eliminar el producto porque figura en pedidos registrados");
        }
        return "redirect:/products";
    }
}
