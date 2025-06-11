
CREATE TABLE bank_account (
    is_default BIT NOT NULL,
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_holder VARCHAR(50),
    account_number VARCHAR(50),
    bank_name VARCHAR(50),
    PRIMARY KEY (id)
);

CREATE TABLE category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_name VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE comment (
    created_at DATETIME(6),
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT,
    user_id BIGINT,
    content VARCHAR(1000) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE coupon (
    price INTEGER NOT NULL CHECK (price<=50000),
    stock INTEGER NOT NULL CHECK (stock<=1000000),
    end_at DATETIME(6),
    expires_at DATETIME(6),
    id BIGINT NOT NULL AUTO_INCREMENT,
    start_at DATETIME(6),
    title VARCHAR(25) NOT NULL,
    content VARCHAR(100),
    coupon_status ENUM ('COMPLETED','ENDED','IN_PROGRESS','PREVIOUS'),
    PRIMARY KEY (id)
);

CREATE TABLE delivery_info (
    is_default BIT,
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    detail_address VARCHAR(100),
    address VARCHAR(255),
    zipcode VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE fcm_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_group_id BIGINT NOT NULL,
    image_index BIGINT,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    original_url VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE image_group (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type ENUM ('PROJECT','USER'),
    PRIMARY KEY (id)
);

CREATE TABLE options (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    option_name VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE order_item (
    quantity INTEGER NOT NULL,
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    price BIGINT,
    product_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE orders (
    is_settled BIT,
    quantity INTEGER NOT NULL,
    delivery_info_id BIGINT,
    id BIGINT NOT NULL AUTO_INCREMENT,
    ordered_at DATETIME(6),
    project_id BIGINT,
    total_price BIGINT,
    user_id BIGINT,
    status ENUM ('CANCELED','DELIVERED','ORDERED'),
    PRIMARY KEY (id)
);

CREATE TABLE payment (
    amount_total BIGINT,
    approved_at DATETIME(6),
    created_at DATETIME(6),
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT,
    user_id BIGINT,
    pg_token VARCHAR(255),
    tid VARCHAR(255),
    status ENUM ('APPROVED','CANCELLED','FAILED','PENDING','REFUNDED'),
    PRIMARY KEY (id)
);

CREATE TABLE platform_plan (
    pg_fee_rate FLOAT(53) NOT NULL,
    platform_fee_rate FLOAT(53) NOT NULL,
    vat_rate FLOAT(53) NOT NULL,
    id BIGINT NOT NULL AUTO_INCREMENT,
    description TEXT,
    name ENUM ('BASIC','PREMIUM','PRO') NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    price BIGINT CHECK (price<=1000000000),
    project_id BIGINT NOT NULL,
    stock BIGINT CHECK (stock<=50000),
    name VARCHAR(25),
    description VARCHAR(50),
    PRIMARY KEY (id)
);

CREATE TABLE project (
    bank_account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME(6),
    end_at DATETIME(6),
    goal_amount BIGINT CHECK (goal_amount<=1000000000),
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_group_id BIGINT NOT NULL,
    platform_plan_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    shipped_at DATETIME(6),
    start_at DATETIME(6),
    total_amount BIGINT CHECK (total_amount<=1000000000),
    views BIGINT,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(100) NOT NULL,
    content_markdown TEXT,
    is_approved ENUM ('APPROVED','PENDING','REJECTED'),
    status ENUM ('ACTIVE','CANCELLED','COMPLETED','FAILED','QUEUED'),
    PRIMARY KEY (id)
);

CREATE TABLE search (
    id BIGINT NOT NULL AUTO_INCREMENT,
    search_word VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE settlement (
    total_count INTEGER NOT NULL,
    bankaccount_id BIGINT,
    id BIGINT NOT NULL AUTO_INCREMENT,
    pay_out_amount BIGINT NOT NULL,
    pg_fee_total BIGINT NOT NULL,
    platform_fee_total BIGINT NOT NULL,
    project_id BIGINT,
    requested_at DATETIME(6),
    settled_at DATETIME(6),
    total_sales BIGINT NOT NULL,
    user_id BIGINT,
    vat_total BIGINT NOT NULL,
    status ENUM ('CANCELLED','COMPLETED','FAILED','PENDING'),
    PRIMARY KEY (id)
);

CREATE TABLE settlement_history (
    amount BIGINT NOT NULL,
    created_at DATETIME(6),
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    pg_fee BIGINT NOT NULL,
    platform_fee BIGINT NOT NULL,
    settlement_id BIGINT NOT NULL,
    total_price BIGINT,
    vat BIGINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE "user" (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_group_id BIGINT,
    email VARCHAR(50) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    seller_introduction VARCHAR(200),
    link_url VARCHAR(1000),
    role ENUM ('ROLE_ADMIN','ROLE_USER') NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_coupon (
    coupon_id BIGINT,
    id BIGINT NOT NULL AUTO_INCREMENT,
    issue_at DATETIME(6),
    user_id BIGINT,
    status ENUM ('EXPIRED','UNUSED','USED'),
    PRIMARY KEY (id)
);
