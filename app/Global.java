/**
 * Created by kari on 13/02/15.
 */
import models.Cocktail;
import models.Ingredients;
import play.*;

import java.util.List;

public class Global extends GlobalSettings {
    public void onStart(Application app) {
        Logger.info("Application has started");

        if ( Ingredients.searchByName("Rum").isEmpty() ) {
            String[] ingredients = {"Rum", "Vodka", "Tequila", "Gin", "Passoa", "Coke", "Strawberries", "Tonic", "Sugar"};

            for (String a : ingredients ) {
                Ingredients ingredient = new Ingredients();
                ingredient.setName(a);
                ingredient.save();
            }

            String[] cocktails = {"Mojito", "Rum and coke", "Gin and tonic", "Strawberry Daquiri"};

            for (String c : cocktails) {
                Cocktail cocktail = new Cocktail();
                cocktail.setName(c);
                cocktail.setDescription("Desc here");

                if (c.equals("Mojito")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Sugar").get(0));

                } else if (c.equals("Run and coke")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Coke").get(0));

                } else if (c.equals("Gin and tonic")) {
                    cocktail.addIngredient(Ingredients.searchByName("Gin").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Tonic").get(0));

                } else if (c.equals("Strawberry Daquiri")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Strawberries").get(0));
                }

                cocktail.save();
            }
        }
    }
}