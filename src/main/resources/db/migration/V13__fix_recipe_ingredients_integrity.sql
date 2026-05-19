
DELETE FROM recipe_ingredients
WHERE stock_item_id NOT IN (SELECT id_stock_item FROM stock_items);

DELETE FROM product_recipes
WHERE id_recipe NOT IN (SELECT id_recipe FROM recipe_ingredients);

ALTER TABLE recipe_ingredients
    ADD CONSTRAINT fk_recipe_ingredients_stock_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items(id_stock_item)
        ON DELETE RESTRICT;
