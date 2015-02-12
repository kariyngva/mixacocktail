package controllers;

import models.Cocktail;
import models.Ingredients;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;
import views.html.helper.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready.", 10));
    }

    public static Result addCocktail() {
        Cocktail cocktail = new Cocktail();
        Map<String, String[]> map = request().body().asFormUrlEncoded();
        String[] ingredients = map.get("ingredient");
        String[] cname = map.get("name");

        Ingredients ingredients1;
        // Loop for each checked question
        for (String t : ingredients) {
            Logger.info("ingredients data is " + t);

            //create and save a new ingredient if not found?
            //if()
            Ingredients ingToAdd = new Ingredients();
            ingToAdd.setName(t);
            cocktail.addIngredient(ingToAdd);
        }

        cocktail.setName(cname[0]);
        //Cocktail cocktail = Form.form(Cocktail.class).bindFromRequest().get();

        cocktail.save();
        return redirect(routes.Application.getCocktails());

    }

    public static Result getCocktails() {
        List<Cocktail> clist = new Model.Finder(String.class, Cocktail.class).all();
        return ok(cocktails.render(clist));
    }
}