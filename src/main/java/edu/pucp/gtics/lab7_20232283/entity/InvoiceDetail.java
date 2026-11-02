package edu.pucp.gtics.lab7_20232283.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "invoice_detail")
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive(message = "La cantidad debe ser un entero mayor que cero")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Precio unitario del producto al momento de la venta
    @Column(name = "price", nullable = false)
    private Double price;

    // subtotal = cantidad x precio
    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
