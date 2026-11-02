package edu.pucp.gtics.lab7_20232283.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Pedido = comprobante simplificado de venta (Factura o Boleta)
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Seleccione el tipo de comprobante")
    @Pattern(regexp = "(Factura|Boleta)?", message = "El tipo debe ser Factura o Boleta")
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // @Valid: también se validan las líneas (cantidad > 0)
    @Valid
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceDetail> details = new ArrayList<>();

    // Total = suma de subtotales (calculado en Java)
    public double getTotal() {
        double total = 0;
        for (InvoiceDetail d : details) {
            if (d.getSubtotal() != null) {
                total += d.getSubtotal();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<InvoiceDetail> getDetails() { return details; }
    public void setDetails(List<InvoiceDetail> details) { this.details = details; }
}
