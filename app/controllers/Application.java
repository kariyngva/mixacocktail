/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 20.01.15
 *
 * Klasinn
 */
package controllers;

import models.UserIngredient;
import models.Rating;
import play.*;
import models.Ingredients;
import models.Cocktail;
import play.db.ebean.Model;
import play.mvc.*;
import securesocial.core.java.SecuredAction;
import views.html.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.lang.reflect.Array;
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
        return ok( index.render() );
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
    public static Result getAllCocktails(int page) {
        List<Cocktail> allCocktails = Cocktail.getAllCocktails(page);
        return ok( toJson( allCocktails ) );
    }

    /**
     * Aðferð: Sækir kokteil eftir auðkenni (id).
     *
     * @return: Vefsíðu sem birtir upplýsingar um kokteil.
     **/
    public static Result getCocktail(long id){
        Cocktail singleCocktail = Cocktail.findById(id);
        return ok( cocktail.render( singleCocktail ) );
    }

    /**
     * Aðferð: Finnur kokteil út frá innslegnu hráefni og splittar hráefninu með bandstriki til að auðvelda aðskilnað
     *         á mismunandi hráefnum í js.
     *
     * @return: skilar Json með öllum þeim kokteilum sem innihalda innslegin hráefni.
     **/
    public static Result findCocktailByIngredient() throws UnsupportedEncodingException {
        String[] parameters = request().uri().split("\\?")[1].split("-");

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = URLDecoder.decode(parameters[i], "UTF-8");
            Logger.info( parameters[i] );
        }

        List<Cocktail> results = Cocktail.searchByIngredients( Ingredients.searchByNames(parameters) );
        return ok( toJson(results) );
    }

    /**
     * Aðferð: Tekur inn streng sem er hluti úr nafni hráefnis eða heilt nafn hráefnis og
     *         leitar að hráefnum sem innihalda hluta eða allan strenginn.
     *
     * @return: Skilar fylki JSON-Ingredients hluta
     **/
    public static Result getIngredients(String ing) {
        String s = new String (ing);
        if (s.length() > 1 ) {
            return ok( toJson(Ingredients.getIngredients(s)) );
        } else {
            return ok(toJson(ok()) );
        }
    }

    /**
     * Aðferð: Tekur inn userID, einkunn og auðkenni kokteils og leyfir notenda að gefa kokteil einkunn.
     *
     * @return: Uppfærður JSON hlutur fyrir kokteilinn gefið var einkunn
     **/
    public Result updateRating(Long cid, int rating, String userid) {
        Cocktail cocktail = Cocktail.findById(cid);

        if (userid.length() > 0 && rating > 0 && rating < 6) {
            cocktail.addRating(userid, rating);
            int sum = 0;
            for(Rating cr : cocktail.getRating()){
                sum += cr.getRating();
            }

            if(cocktail.getRating().size() > 0){
                sum = sum/cocktail.getRating().size();
            }
            cocktail.updateRatingValue(sum);
        }
        return ok( toJson( cocktail ) );
    }

    /**
     * Aðferð: Reynir að auðkenna facebook notanda
     *
     * @return: Skilar streng sem segir að notandi sé auðkenndur
     **/
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
     * Aðferð: Býr til tilvik af DemoUser og birtir prófíl
     *
     * @return: Skilar prófíl síðu fyrir innskráðan notanda
     **/
    @SecuredAction
    public Result profile() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(profile.render(user, SecureSocial.env()));
    }

    /**
     * Aðferð: Býr til tilvik af DemoUser og birtir prufusíðu
     *
     * @return: Skilar mismunandi prufusíðu fyrir innskráðan og óinnskráða notendur
     **/
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
     * Aðferð: Tekur inn Facebook auðkenni notanda ásamt streng sem inniheldur annaðhvort "destroy"
     *          eða 1 eða fleiri hráefni. Hráefnin notanda eru vistuð í gagnagrunn sé ingredients
     *          ekki strengurinn "destroy".
     *
     * @return: skilar tómri HTTP niðurstöðu.
     **/
    public static Result saveIngredients(String userId, String ingredients ) throws UnsupportedEncodingException {
        UserIngredient ui = UserIngredient.findById( userId );

        if ( !ingredients.equals("destroy") )
        {
            String[] parameters = ingredients.split("-");

            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = URLDecoder.decode(parameters[i], "UTF-8");
                Logger.info( parameters[i] );
            }

            if ( ui == null ) {
                ui = new UserIngredient( userId );
            }

            ui.addUserIngredients( Ingredients.searchByNames( parameters ) );
        }
        else
        {
            ui.clearUserIngredients();
        }

        return ok();
    }

    /**
     * Aðferð: Tekur inn strenginn userId sem er facebook auðkenni notanda.
     *         Skilar JSON fylki af ingredients hlutum séu til gögn fyrir notanda með
     *         auðkennið userId.
     *
     * @return: Skilar JSON hlut af hráefnum ef gögn eru til annars tómri niðurstöðu.
     **/
    public static Result getSavedIngredients(String userId) {
        UserIngredient ui = UserIngredient.findById( userId );
        if ( ui != null ) {
            return ok( toJson( ui.getUserIngredients() ) );
        }
        return ok();
    }
}
