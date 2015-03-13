/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 13.02.15
 *
 * Klasinn inniheldur Global breytur sem notaðar voru þegar við vorum að prófa leitina.
 * Tilgangur var að sleppa við að setja gögnin inn manually.
 */

import models.Cocktail;
import models.Ingredients;
import play.*;
import securesocial.core.RuntimeEnvironment;
import service.MyEnvironment;
import play.GlobalSettings;

import java.util.List;

public class Global extends GlobalSettings {
    private RuntimeEnvironment env = new MyEnvironment();

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        A result;

        try {
            result = controllerClass.getDeclaredConstructor(RuntimeEnvironment.class).newInstance(env);
        } catch (NoSuchMethodException e) {
            // the controller does not receive a RuntimeEnvironment, delegate creation to base class.
            result = super.getControllerInstance(controllerClass);
        }
        return result;
    }
    public void onStart(Application app) {

        Logger.info("Application has started");

        //if ( Ingredients.searchByName("Rum").isEmpty() ) {
            String[] ingredients = {"Rum", "Vodka", "Tequila", "Gin", "Passoa", "Coke", "Strawberries", "Tonic", "Sugar"};

            for (String a : ingredients ) {
                Ingredients ingredient = new Ingredients();
                ingredient.setName(a);
                ingredient.save();
            }

            String[] cocktails = {"Mojito", "Cuba libre", "Gin and tonic", "Strawberry Daquiri"};

            for (String c : cocktails) {
                Cocktail cocktail = new Cocktail();
                cocktail.setName(c);

                if (c.equals("Mojito")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Sugar").get(0));
                    Logger.info("eg er her");
                   // cocktail.setAmount(4.0);
                   //cocktail.setAmount(2.0);
                    cocktail.setDescription("Mojito is a traditional Cuban highball. Traditionally, a mojito is a cocktail that consists of five ingredients: white rum, sugar, lime juice, sparkling water, and mint.");

                } else if (c.equals("Cuba libre")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Coke").get(0));
                    cocktail.setDescription("The Cuba Libre also known as rum and coke is a highball made of cola, lime, and dark or light rum.");

                } else if (c.equals("Gin and tonic")) {
                    cocktail.addIngredient(Ingredients.searchByName("Gin").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Tonic").get(0));
                    cocktail.setDescription("A gin and tonic is a highball cocktail made with gin and tonic water poured over ice. It is usually garnished with a slice or wedge of lime.");

                } else if (c.equals("Strawberry Daquiri")) {
                    cocktail.addIngredient(Ingredients.searchByName("Rum").get(0));
                    cocktail.addIngredient(Ingredients.searchByName("Strawberries").get(0));
                    cocktail.setDescription("On hot summer days we have to thank Cuban bartenders for creating such thirst quenching cocktails, like the Mojito and the Daiquiri. Both of these are lightly sweetened, rum based drinks that incorporate lime and sometimes other fruit flavours.");
                }

                cocktail.save();
            }
        //}
    }
}