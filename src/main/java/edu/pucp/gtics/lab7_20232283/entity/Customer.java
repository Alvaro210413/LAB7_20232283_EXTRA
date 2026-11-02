package edu.pucp.gtics.lab7_20232283.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "(DNI|RUC)?", message = "El tipo de documento solo puede ser DNI o RUC")
    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;

    // La longitud exacta (8 para DNI, 11 para RUC) depende del tipo:
    // se valida en CustomerController porque involucra dos campos.
    @NotBlank(message = "El número de documento es obligatorio")
    @Pattern(regexp = "\\d*", message = "El número de documento solo debe contener dígitos")
    @Column(name = "document", nullable = false, unique = true, length = 11)
    private String document;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public boolean isDni() { return "DNI".equals(documentType); }
    public boolean isRuc() { return "RUC".equals(documentType); }
}
