package controllers;

import models.Cocktail;
import models.Ingredients;
import play.*;
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
        Cocktail cocktail = Form.form(Cocktail.class).bindFromRequest().get();
        cocktail.save();
        return redirect(routes.Application.getCocktails());
    }

    public static Result getCocktails() {
        List<Cocktail> clist = new Model.Finder(String.class, Cocktail.class).all();
        return ok(cocktails.render(clist));
    }
}