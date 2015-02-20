package controllers;

import play.*;
import models.Ingredients;
import models.Cocktail;
import play.db.ebean.Model;
import play.mvc.*;
import views.html.*;
import java.util.List;
import java.util.Map;

import static play.libs.Json.toJson;

public class Application extends Controller {

    public static Result index() {

        return ok(index.render("Your new application is ready.", 10));

    }

    public static Result addCocktail() {
        Cocktail cocktail = new Cocktail();
        Map<String, String[]> map = request().body().asFormUrlEncoded();
        String[] ingredients = map.get("ingredient");
        String[] cname = map.get("name");

        // Loop for each checked question
        for (String t : ingredients) {
            List<Ingredients> ilist = Ingredients.searchByName(t);

            if (ilist.size() > 0) {
                cocktail.addIngredient(ilist.get(0));
                Logger.info("Hráefni til");
            }
            else if (t.equals("")){
                //Ef ekkert hraefni er slegid inn, viljum ekki tomann streng i nidurstodur
            }
            else {
                Ingredients ingToAdd = new Ingredients();
                ingToAdd.setName(t);
                cocktail.addIngredient(ingToAdd);
                Logger.info("Hráefni ekki til");
            }
        }

        cocktail.setName(cname[0]);
        cocktail.save();
        return redirect(routes.Application.getCocktails());

    }

    public static Result getCocktails() {
        List<Cocktail> clist = new Model.Finder(String.class, Cocktail.class).all();
        return ok(cocktails.render(clist));
    }

    public static Result findCocktailByIngredient() {
        //get parameter cut to string
        String[] parameters = request().uri().split("\\?")[1].split("-");
        List<Cocktail> results = Cocktail.searchByIngredients( Ingredients.searchByNames(parameters) );

        //Búa til list af ingredient út frá string array
        //fá lista af cocktials sem innihalda hráefni úr þeim lista
        //ef sá listi er ekki tómur, returna lista
        //gera aðra leit sem inniheldur cocktails sem eru með einhver hráefnin úr strengja array

        //List<Cocktail> results = Cocktail.searchByIngredient( Ingredients.searchByName("Rum").get(0) );
        return ok( toJson(results) );
    }
}