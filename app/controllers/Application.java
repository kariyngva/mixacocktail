package controllers;

import play.*;
import models.Ingredients;
import models.Cocktail;
import play.db.ebean.Model;
import play.mvc.*;
import securesocial.core.java.SecuredAction;
import views.html.*;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;
import views.html.index;
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
    public static Result getAllCocktails(int page){
        return ok( toJson(Cocktail.getAllCocktails(page)) );
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

    @SecuredAction(authorization = WithProvider.class, params = {"facebook"})
    public Result onlyFacebook() {
        return ok("You are seeing this because you logged in using Facebook");
    }
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment<DemoUser> env;

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    public Application(RuntimeEnvironment<DemoUser> env) {
        this.env = env;
    }
    /**
     * This action only gets called if the user is logged in.
     *
     * @return
     */
    @SecuredAction
    public Result profile() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(profile.render(user, SecureSocial.env()));
    }

    @UserAwareAction
    public Result userAware() {
        DemoUser demoUser = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        String userName ;
        if ( demoUser != null ) {
            BasicProfile user = demoUser.main;
            if ( user.firstName().isDefined() ) {
                userName = user.firstName().get();
            } else if ( user.fullName().isDefined()) {
                userName = user.fullName().get();
            } else {
                userName = "authenticated user";
            }
        } else {
            userName = "guest";
        }
        return ok("Hello " + userName + ", you are seeing a public page");
    }


    /**
     * Sample use of SecureSocial.currentUser. Access the /current-user to test it
     */
    public F.Promise<Result> currentUser() {
        return SecureSocial.currentUser(env).map( new F.Function<Object, Result>() {
            @Override
            public Result apply(Object maybeUser) throws Throwable {
                String id;

                if ( maybeUser != null ) {
                    DemoUser user = (DemoUser) maybeUser;
                    id = user.main.userId();
                } else {
                    id = "not available. Please log in.";
                }
                return ok("your id is " + id);
            }
        });
    }
}