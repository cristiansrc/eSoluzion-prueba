CREATE TABLE prices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id    BIGINT       NOT NULL,
    start_date  TIMESTAMP    NOT NULL,
    end_date    TIMESTAMP    NOT NULL,
    price_list  INT          NOT NULL,
    product_id  BIGINT       NOT NULL,
    priority    INT          NOT NULL,
    price       DECIMAL(10,2) NOT NULL,
    curr        VARCHAR(3)   NOT NULL
);

CREATE INDEX idx_prices_brand_product_dates 
    ON prices(brand_id, product_id, start_date, end_date);
