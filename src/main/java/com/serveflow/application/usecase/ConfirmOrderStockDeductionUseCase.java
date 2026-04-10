package com.serveflow.application.usecase;

import com.serveflow.domain.event.OrderConfirmedEvent;
import com.serveflow.domain.exception.RecipeNotFoundException;
import com.serveflow.domain.model.stock.ProductRecipe;
import com.serveflow.domain.model.stock.StockItem;
import com.serveflow.domain.model.stock.StockMovement;
import com.serveflow.domain.repository.ProductRecipeRepository;
import com.serveflow.domain.repository.StockItemRepository;
import com.serveflow.domain.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfirmOrderStockDeductionUseCase {

    private final ProductRecipeRepository recipeRepository;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository movementRepository;

    public ConfirmOrderStockDeductionUseCase(ProductRecipeRepository recipeRepository,
                                             StockItemRepository stockItemRepository,
                                             StockMovementRepository movementRepository) {
        this.recipeRepository = recipeRepository;
        this.stockItemRepository = stockItemRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public void execute(OrderConfirmedEvent event) {
        for (var item : event.items()) {
            ProductRecipe recipe = recipeRepository.findByProductId(item.productId())
                    .orElseThrow(() -> new RecipeNotFoundException(item.productId()));

            for (var ingredient : recipe.getIngredients()) {
                BigDecimal required = ingredient.getRequiredQuantity(item.quantity());

                // Pessimistic lock para evitar furos de estoque em concorrência
                StockItem stockItem = stockItemRepository.findByIdForUpdate(ingredient.getStockItemId());
                stockItem.deduct(required);
                stockItemRepository.save(stockItem);

                StockMovement movement = StockMovement.createExit(
                        stockItem.getId(),
                        required,
                        "Baixa automatica - Pedido " + event.orderId()
                                + " - Produto: " + item.productName(),
                        event.orderId()
                );
                movementRepository.save(movement);
            }
        }
    }
}
