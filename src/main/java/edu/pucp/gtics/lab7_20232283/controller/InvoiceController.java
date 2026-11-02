package edu.pucp.gtics.lab7_20232283.controller;

import edu.pucp.gtics.lab7_20232283.entity.Customer;
import edu.pucp.gtics.lab7_20232283.entity.Invoice;
import edu.pucp.gtics.lab7_20232283.entity.InvoiceDetail;
import edu.pucp.gtics.lab7_20232283.entity.Product;
import edu.pucp.gtics.lab7_20232283.repository.CustomerRepository;
import edu.pucp.gtics.lab7_20232283.repository.InvoiceRepository;
import edu.pucp.gtics.lab7_20232283.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public InvoiceController(InvoiceRepository invoiceRepository,
                             CustomerRepository customerRepository,
                             ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @GetMapping({"", "/", "/list"})
    public String listar(Model model) {
        model.addAttribute("invoices", invoiceRepository.findAllByOrderByDateDescIdDesc());
        return "invoice/list";
    }

    @GetMapping("/detail")
    public String detalle(@RequestParam("id") Integer id, Model model, RedirectAttributes attr) {
        Optional<Invoice> opt = invoiceRepository.findById(id);
        if (opt.isEmpty()) {
            attr.addFlashAttribute("error", "El pedido solicitado no existe");
            return "redirect:/invoices";
        }
        model.addAttribute("invoice", opt.get());
        return "invoice/detail";
    }

    @GetMapping("/new")
    public String nuevo(Model model) {
        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.now());
        invoice.setCustomer(new Customer());
        for (int i = 0; i < 3; i++) {           // 3 líneas vacías para empezar
            invoice.getDetails().add(lineaVacia());
        }
        model.addAttribute("invoice", invoice);
        return mostrarFormulario(model);
    }

    // Botón "+ Agregar producto" (sin JavaScript): agrega una línea vacía y vuelve al formulario
    @PostMapping(value = "/save", params = "addLine")
    public String agregarLinea(@ModelAttribute("invoice") Invoice invoice,
                               BindingResult bindingResult,
                               Model model) {
        invoice.getDetails().add(lineaVacia());
        return mostrarFormulario(model);
    }

    // Botones "Calcular total" y "Registrar pedido"
    @Transactional // el pedido, sus detalles y el descuento de stock se guardan juntos
    @PostMapping(value = "/save", params = "!addLine")
    public String guardar(@ModelAttribute("invoice") @Valid Invoice invoice,
                          BindingResult bindingResult,
                          @RequestParam(value = "calculate", required = false) String calcular,
                          Model model,
                          RedirectAttributes attr) {

        validarPedido(invoice, bindingResult);
        calcularSubtotales(invoice);

        // Si hay errores de validación o de binding, NO se registra nada
        if (bindingResult.hasErrors() || calcular != null) {
            return mostrarFormulario(model);
        }

        // Se quitan las líneas vacías y se enlazan las demás con el pedido
        invoice.getDetails().removeIf(d -> d.getQuantity() == null);
        for (InvoiceDetail d : invoice.getDetails()) {
            d.setInvoice(invoice);
            Product p = d.getProduct();
            p.setStock(p.getStock() - d.getQuantity());   // control de inventario
        }
        invoiceRepository.save(invoice);   // cascade = ALL guarda también los detalles

        attr.addFlashAttribute("msg", invoice.getType() + " N° " + invoice.getId()
                + " registrada. Total: S/ " + String.format("%.2f", invoice.getTotal()));
        return "redirect:/invoices";
    }

    // Reglas que dependen de la BD o de varios campos
    private void validarPedido(Invoice invoice, BindingResult br) {

        // 1) Cliente seleccionado y existente
        Customer customer = null;
        if (invoice.getCustomer() == null || invoice.getCustomer().getId() == null) {
            if (!br.hasFieldErrors("customer.id")) {
                br.rejectValue("customer.id", "customer.vacio", "Debe seleccionar un cliente");
            }
        } else {
            customer = customerRepository.findById(invoice.getCustomer().getId()).orElse(null);
            if (customer == null) {
                br.rejectValue("customer.id", "customer.noExiste", "El cliente seleccionado no existe");
            } else {
                invoice.setCustomer(customer);   // se reemplaza por el cliente real de la BD
            }
        }

        // 2) Líneas: al menos un producto, sin repetir, cantidad <= stock
        Set<Integer> productosUsados = new HashSet<>();
        int lineasConDatos = 0;
        for (int i = 0; i < invoice.getDetails().size(); i++) {
            InvoiceDetail d = invoice.getDetails().get(i);
            String campoProducto = "details[" + i + "].product.id";
            String campoCantidad = "details[" + i + "].quantity";
            Integer productId = (d.getProduct() == null) ? null : d.getProduct().getId();
            boolean errorCantidad = br.hasFieldErrors(campoCantidad);   // letras o cantidad <= 0

            if (d.getProduct() == null) {
                d.setProduct(new Product());
            }
            if (productId == null && d.getQuantity() == null && !errorCantidad) {
                continue;   // línea vacía: se ignora
            }
            lineasConDatos++;

            if (productId == null) {
                br.rejectValue(campoProducto, "linea.producto", "Seleccione un producto");
                continue;
            }
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                br.rejectValue(campoProducto, "linea.producto", "El producto no existe");
                continue;
            }
            d.setProduct(product);   // producto real (con precio y stock)

            if (!productosUsados.add(productId)) {
                br.rejectValue(campoProducto, "linea.repetido",
                        "El producto ya fue agregado en otra línea");
            }
            if (!errorCantidad) {
                if (d.getQuantity() == null) {
                    br.rejectValue(campoCantidad, "linea.cantidad", "Ingrese la cantidad");
                } else if (d.getQuantity() > product.getStock()) {
                    br.rejectValue(campoCantidad, "linea.stock",
                            "Stock insuficiente: solo hay " + product.getStock() + " disponibles");
                }
            }
        }
        if (lineasConDatos == 0) {
            br.reject("pedido.vacio", "El pedido debe incluir al menos un producto");
        }

        // 3) Error GLOBAL (depende de dos campos): tipo de comprobante vs documento del cliente
        if (customer != null && !br.hasFieldErrors("type")) {
            if ("Factura".equals(invoice.getType()) && !customer.isRuc()) {
                br.reject("pedido.factura", "Una Factura solo puede emitirse a un cliente con RUC");
            } else if ("Boleta".equals(invoice.getType()) && !customer.isDni()) {
                br.reject("pedido.boleta", "Una Boleta solo puede emitirse a un cliente con DNI");
            }
        }
    }

    // subtotal = cantidad x precio (en Java, no en JavaScript)
    private void calcularSubtotales(Invoice invoice) {
        for (InvoiceDetail d : invoice.getDetails()) {
            Product p = d.getProduct();
            d.setPrice(p != null ? p.getPrice() : null);
            if (p != null && p.getPrice() != null && d.getQuantity() != null && d.getQuantity() > 0) {
                d.setSubtotal(Math.round(d.getQuantity() * p.getPrice() * 100.0) / 100.0);
            } else {
                d.setSubtotal(null);
            }
        }
    }

    private InvoiceDetail lineaVacia() {
        InvoiceDetail d = new InvoiceDetail();
        d.setProduct(new Product());
        return d;
    }

    private String mostrarFormulario(Model model) {
        model.addAttribute("tipos", List.of("Factura", "Boleta"));
        model.addAttribute("customers", customerRepository.findAllByOrderByNameAsc());
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        return "invoice/form";
    }
}
