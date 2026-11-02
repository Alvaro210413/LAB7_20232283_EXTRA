CREATE DATABASE IF NOT EXISTS lab6;
USE lab6;

DROP TABLE IF EXISTS invoice_detail;
DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS customer;

CREATE TABLE customer (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    document VARCHAR(11) NOT NULL UNIQUE,
    document_type VARCHAR(10) NOT NULL
);

CREATE TABLE product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    price DOUBLE NOT NULL,
    stock INT NOT NULL
);

CREATE TABLE invoice (
    id INT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT
);

CREATE TABLE invoice_detail (
    id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT,
    UNIQUE (invoice_id, product_id)
);

-- Datos de prueba
INSERT INTO customer (name, document, document_type) VALUES
('Comercial Los Andes S.A.C.', '20512345678', 'RUC'),
('María Quispe Huamán', '45678912', 'DNI'),
('Distribuidora Norte E.I.R.L.', '20600123459', 'RUC');

INSERT INTO product (name, price, stock) VALUES
('Arroz extra 5 kg', 24.90, 50),
('Aceite vegetal 1 L', 9.50, 30),
('Azúcar rubia 1 kg', 4.20, 80),
('Leche evaporada 400 g', 3.80, 0);
