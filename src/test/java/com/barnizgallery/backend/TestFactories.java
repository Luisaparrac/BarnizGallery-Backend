package com.barnizgallery.backend;

import java.util.List;

import com.barnizgallery.backend.patterns.factorymethod.ViewInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.ZoomInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.BidInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.FavoriteInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.AddToCartInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.ShareInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.PurchaseInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.StoreVisitInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.CustomOrderInteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;

/**
 * The interaction factories for unit tests: one creator per action, as Spring registers them.
 */
public final class TestFactories {

    private TestFactories() {
    }

    public static InteractionFactoryProvider provider() {
        return new InteractionFactoryProvider(List.of(new ViewInteractionFactory(),
                new ZoomInteractionFactory(),
                new BidInteractionFactory(),
                new FavoriteInteractionFactory(),
                new AddToCartInteractionFactory(),
                new ShareInteractionFactory(),
                new PurchaseInteractionFactory(),
                new StoreVisitInteractionFactory(),
                new CustomOrderInteractionFactory()));
    }
}
