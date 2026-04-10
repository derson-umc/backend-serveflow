package com.serveflow.domain.model.stock;

import java.util.*;

public class ProductRecipe {

    private final UUID id;
    private final UUID productId;
    private final String productName;
    private final List<RecipeIngredient> ingredients;
    private Long version;

    public ProductRecipe(UUID id, UUID productId, String productName,
                         List<RecipeIngredient> ingredients, Long version) {
        this.id = Objects.requireNonNull(id, "ID da ficha tecnica e obrigatorio.");
        this.productId = Objects.requireNonNull(productId, "ID do produto e obrigatorio.");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("Nome do produto e obrigatorio.");
        this.productName = productName.strip();
        this.ingredients = new ArrayList<>(Optional.ofNullable(ingredients).orElse(List.of()));
        this.version = version;
    }

    public static ProductRecipe create(UUID productId, String productName,
                                       List<RecipeIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty())
            throw new IllegalArgumentException("Ficha tecnica deve conter ao menos um ingrediente.");
        return new ProductRecipe(UUID.randomUUID(), productId, productName, ingredients, null);
    }

    public void addIngredient(RecipeIngredient ingredient) {
        Objects.requireNonNull(ingredient, "Ingrediente nao pode ser nulo.");
        boolean alreadyExists = ingredients.stream()
                .anyMatch(i -> i.getStockItemId().equals(ingredient.getStockItemId()));
        if (alreadyExists)
            throw new IllegalStateException("Insumo ja existe nesta ficha tecnica.");
        ingredients.add(ingredient);
    }

    public void removeIngredient(UUID ingredientId) {
        if (ingredients.size() <= 1)
            throw new IllegalStateException("Ficha tecnica deve conter ao menos um ingrediente.");
        boolean removed = ingredients.removeIf(i -> i.getId().equals(ingredientId));
        if (!removed)
            throw new IllegalArgumentException("Ingrediente nao encontrado na ficha tecnica.");
    }

    /**
     * Verifica se todos os insumos possuem estoque suficiente para N porções.
     * Recebe um mapa de stockItemId -> quantidade disponivel.
     */
    public boolean canPrepare(int quantity, Map<UUID, java.math.BigDecimal> stockLevels) {
        return ingredients.stream().allMatch(ingredient -> {
            var available = stockLevels.getOrDefault(ingredient.getStockItemId(), java.math.BigDecimal.ZERO);
            var required = ingredient.getRequiredQuantity(quantity);
            return available.compareTo(required) >= 0;
        });
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public List<RecipeIngredient> getIngredients() { return List.copyOf(ingredients); }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductRecipe other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
