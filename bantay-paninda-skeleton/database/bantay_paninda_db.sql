-- ==========================================================
-- BANTAY PANINDA DATABASE
-- Run this file in HeidiSQL while MySQL is running in Laragon.
-- This script creates the tables and adds the sample records.
-- ==========================================================

CREATE DATABASE IF NOT EXISTS bantay_paninda_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE bantay_paninda_db;

-- ----------------------------------------------------------
-- TABLE 1: Categories
-- This table stores the product categories.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    category_id   INT          NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(50)  NOT NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uq_category_name (category_name)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- TABLE 2: Products
-- This table stores the products, prices, and available stock.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id          INT           NOT NULL AUTO_INCREMENT,
    product_name        VARCHAR(100)  NOT NULL,
    category_id         INT           NULL,
    price               DECIMAL(10,2) NOT NULL,
    stock               INT           NOT NULL DEFAULT 0,
    low_stock_threshold INT           NOT NULL DEFAULT 5,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    UNIQUE KEY uq_product_name (product_name),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_price CHECK (price >= 0),
    CONSTRAINT chk_stock CHECK (stock >= 0)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- TABLE 3: Sales
-- This table stores each sale made in the system.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id      INT           NOT NULL AUTO_INCREMENT,
    sale_date    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (sale_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- TABLE 4: Sale Items
-- This table stores the products included in each sale.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT           NOT NULL AUTO_INCREMENT,
    sale_id      INT           NOT NULL,
    product_id   INT           NULL,
    product_name VARCHAR(100)  NOT NULL,
    quantity     INT           NOT NULL,
    unit_price   DECIMAL(10,2) NOT NULL,
    subtotal     DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (sale_item_id),
    CONSTRAINT fk_items_sale
        FOREIGN KEY (sale_id) REFERENCES sales (sale_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_items_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_quantity CHECK (quantity > 0)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- TABLE 5: Users
-- This table stores the accounts used when logging in.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    pin_hash      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NULL,
    role          ENUM('MANAGER','INVENTORY','CASHIER') NOT NULL DEFAULT 'CASHIER',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_username (username)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- TABLE 6: Stock Movements
-- This table records stock added to or deducted from a product.
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_movements (
    movement_id  INT          NOT NULL AUTO_INCREMENT,
    product_id   INT          NULL,
    product_name VARCHAR(100) NOT NULL,
    user_id      INT          NULL,
    change_qty   INT          NOT NULL,
    reason       VARCHAR(50)  NOT NULL,
    movement_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (movement_id),
    KEY idx_stock_movements_date (movement_date),
    KEY idx_stock_movements_product_name (product_name),
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id) REFERENCES products (product_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_stock_movements_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_stock_movements_change_qty CHECK (change_qty <> 0)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Add the product categories used in the system.
-- ----------------------------------------------------------
INSERT IGNORE INTO categories (category_name) VALUES
('Canned Goods'),
('Instant Food'),
('Beverages'),
('Snacks'),
('Toiletries'),
('Condiments');

-- ----------------------------------------------------------
-- Default accounts for the Mall login.
-- The PIN is saved as a SHA-256 hash instead of plain text.
-- manager / 1111
-- inventory / 2222
-- cashier / 3333
-- ----------------------------------------------------------
INSERT IGNORE INTO users (username, pin_hash, full_name, role) VALUES
('manager', '64759c78a81e01253c72ba1cd06176eb621f8efeb68630f8b83bb2a4e452ec56', 'Store Manager', 'MANAGER'),
('inventory', '112cd15f3c5245c04ff7150314dfd6c64717f539db5616a4c7a7b4888bb35544', 'Inventory Staff', 'INVENTORY'),
('cashier', '85a05f7c3203b5475b27f4a3af4e24ca9f7bccb54bdbd7a88656892115b00b53', 'Cashier Staff', 'CASHIER');

-- ----------------------------------------------------------
-- Tindahan account used for PIN login.
-- The test PIN is 4444.
-- Update the account if it already exists; otherwise, add it.
-- ----------------------------------------------------------
UPDATE users
SET pin_hash = SHA2('tindahan:4444', 256),
    full_name = 'Tindahan Access',
    role = 'CASHIER'
WHERE username = 'tindahan';

INSERT INTO users (username, pin_hash, full_name, role)
SELECT 'tindahan', SHA2('tindahan:4444', 256), 'Tindahan Access', 'CASHIER'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'tindahan'
);

-- ----------------------------------------------------------
-- Add the sample products for testing.
-- Some products have low stock so the alert can be checked.
-- ----------------------------------------------------------
INSERT IGNORE INTO products (product_name, category_id, price, stock, low_stock_threshold) VALUES
-- Canned Goods
('Corned Beef 150g', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 35.00, 30, 5),
('Luncheon Meat 150g', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 42.00, 22, 5),
('Vienna Sausage 130g', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 28.50, 20, 5),
('Evaporated Milk 370ml', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 45.00, 25, 5),
('Condensed Milk 300ml', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 40.00, 28, 5),
('Canned Tuna Flakes 155g', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 32.00, 15, 5),
('Pineapple Chunks 220g', (SELECT category_id FROM categories WHERE category_name = 'Canned Goods'), 38.00, 2, 5),

-- Instant Food
('3-in-1 Coffee Refill Pack 480g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 85.00, 30, 5),
('Choco Malt Drink 300g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 95.00, 20, 5),
('Powdered Orange Drink 450g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 110.00, 12, 5),
('Instant Oatmeal Sachet 30g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 12.00, 25, 5),
('Cream of Mushroom Soup Mix 40g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 18.00, 1, 5),
('Instant Rice Porridge Sachet', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 10.00, 3, 5),
('Hotcake Mix 200g', (SELECT category_id FROM categories WHERE category_name = 'Instant Food'), 45.00, 18, 5),

-- Beverages
('Coke 1.5L', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 75.00, 24, 5),
('Sprite 1.5L', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 72.00, 18, 5),
('Royal 1.5L', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 70.00, 15, 5),
('Coca-Cola 500ml PET', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 35.00, 30, 5),
('Bottled Water 350ml', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 12.00, 40, 5),
('Iced Tea Bottle 500ml', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 25.00, 20, 5),
('Imported Soda Can 250ml', (SELECT category_id FROM categories WHERE category_name = 'Beverages'), 55.00, 2, 5),

-- Snacks
('Chips Chicharon 85g', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 35.00, 15, 5),
('Potato Chips Barbecue 55g', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 28.00, 12, 5),
('Cheese Curls 35g', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 15.00, 20, 5),
('Chocolate Bar 30g', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 22.00, 30, 5),
('Hard Candy Pack 100pcs', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 65.00, 10, 5),
('Biscuit Assorted Family Pack', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 95.00, 8, 5),
('Marshmallows Mini 100g', (SELECT category_id FROM categories WHERE category_name = 'Snacks'), 45.00, 3, 5),

-- Toiletries
('Bath Soap Safeguard 60g', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 28.00, 20, 5),
('Shampoo Sachet 12ml', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 6.00, 55, 5),
('Toothpaste 50ml', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 35.00, 18, 5),
('Detergent Bar 350g', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 22.00, 25, 5),
('Detergent Powder Sachet 50g', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 9.00, 40, 5),
('Imported Bar Soap 100g', (SELECT category_id FROM categories WHERE category_name = 'Toiletries'), 85.00, 1, 5),

-- Condiments
('Soy Sauce 60ml Pouch', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 12.00, 30, 5),
('Vinegar 60ml Pouch', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 10.00, 28, 5),
('Patis Fish Sauce 60ml', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 14.00, 22, 5),
('Banana Ketchup 100g', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 20.00, 25, 5),
('Worcestershire Sauce 50ml', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 18.00, 10, 5),
('Iodized Salt 250g', (SELECT category_id FROM categories WHERE category_name = 'Condiments'), 14.00, 20, 5);


-- ----------------------------------------------------------
-- Check if the database and Tindahan account were added correctly.
-- These SELECT queries only display information; they do not change records.
-- pin_matches should show 1 if the test PIN is correct.
-- ----------------------------------------------------------
SELECT
    DATABASE() AS selected_database,
    @@hostname AS mysql_host,
    @@port AS mysql_port;

SELECT
    user_id,
    username,
    role,
    CASE
        WHEN LOWER(pin_hash) = LOWER(SHA2('tindahan:4444', 256)) THEN 1
        ELSE 0
    END AS pin_matches
FROM users
WHERE username = 'tindahan';

SELECT user_id, username, role
FROM users
ORDER BY user_id;

SELECT COUNT(*) AS stock_movements_table_ready
FROM stock_movements;
