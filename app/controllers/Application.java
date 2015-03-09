/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 20.01.15
 *
 * Klasinn
 */
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
    /**
     * Aðferð: Býr til nýjan kokteil hlut, fyrir hvert hráefni innslegið þá ítrum við í gegnum lista
     *         af ingredients og athugum hvort inslegna hráefnið sé til staðar í ilist.
     *         Ef ekkert hráefni finnst þá búum við til nýjan Ingredients hlut ingToAdd.
     *
     * @return: Skilar /Cocktails síðu sem nær í alla þá kokteila og hráefni sem eru inni í gagnagrunni.
     **/
    public static Result addCocktail() {
        Cocktail cocktail = new Cocktail();
        Map<String, String[]> map = request().body().asFormUrlEncoded();
        String[] ingredients = map.get("ingredient");
        String[] cname = map.get("name");

        for (String t : ingredients) {
            List<Ingredients> ilist = Ingredients.searchByName(t);

            if (ilist.size() > 0) {
                cocktail.addIngredient(ilist.get(0));
            }
            else if (t.equals("")){
                //Ef ekkert hraefni er slegid inn, viljum ekki tomann streng i nidurstodur
            }
            else {
                Ingredients ingToAdd = new Ingredients();
                ingToAdd.setName(t);
                cocktail.addIngredient(ingToAdd);
            }
        }

        cocktail.setName(cname[0]);
        cocktail.save();
        return redirect(routes.Application.getCocktails());

    }

    /**
     * Aðferð: Nær í render af kokteilum í lista.
     *
     * @return: Skilar render á clist sem er listi af kokteilum af taginu String
     **/
    public static Result getCocktails() {
        List<Cocktail> clist = new Model.Finder(String.class, Cocktail.class).all();
        return ok(cocktails.render(clist));
    }
    /**
     * Aðferð: Nær í niðurstöður af kokteilum á /cocktails
     *
     * @return: skilar Json sem birtir áveðnar margar niðurstöður af kokteilum:fer eftir hve int er á breytunni page.
     **/
    public static Result getAllCocktails(int page){
        return ok( toJson(Cocktail.getAllCocktails(page)) );
    }

    /**
     * Aðferð: Finnur kokteil út frá innslegnu hráefni og splittar hráefninu með bandstriki til að auðvelda aðskilnað
     *         á mismunandi hráefnum í js.
     *
     * @return: skilar Json með öllum þeim kokteilum sem innihalda innslegin hráefni.
     **/
    public static Result findCocktailByIngredient() {
        //TODO get parameter cut to string
        String[] parameters = request().uri().split("\\?")[1].split("-");
        List<Cocktail> results = Cocktail.searchByIngredients( Ingredients.searchByNames(parameters) );
        return ok( toJson(results) );
    }
}