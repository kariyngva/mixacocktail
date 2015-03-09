/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 03.02.15
 *
 * Klasinn geymir Cokctail hlut og vistar cocktailhlut í gagnagrunni .
 */
package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import play.Logger;
import play.db.ebean.Model;
import scala.collection.immutable.Map;

import javax.persistence.*;
import java.util.List;


@Entity
public class Cocktail extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String name;
    public String description;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Ingredients> ingredients;
    private List<Cocktail> cocktail;

    /**
     * Aðferð:  Bæta nafni við Cocktail hlut
     * @param name  er það nafn sem passar við kokteil.
     **/
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Aðferð: Bæta lýsingu við Cocktail hlut.
     * @param description er lýsing sem lýsir kokteilnum.
     **/
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Aðferð: Bætir hráefni við  Ingredients hlut
     * @param ingredient er hráefni í Ingredients hlutnum.
     **/
    public void addIngredient(Ingredients ingredient) {
        ingredients.add(ingredient);
    }
    /**
     * Aðferð: Nær í hráefni úr Ingredient lista .
     * @return: Skilar lista af hráefnum úr Ingredients hlut.
     **/
    public List<Ingredients> getIngredients() {
        return this.ingredients;
    }
    /**
     * Aðferð: Leitar af Cocktail í cokctail klasa.
     * @param Cocktail af taginu String
     **/
    public static Finder<String,Cocktail> find = new Finder<String,Cocktail>(
            String.class, Cocktail.class
    );
    /**
     * Aðferð: Nær í lista af öllum kokteilum sem eru inni í gagnagrunni.
     * @return: Skilar lista af kokteilum sem paging list 2 í einu.
     **/
    public static List<Cocktail> getAllCocktails(int p){
        PagingList<Cocktail> pagingList =
                Ebean.find(Cocktail.class)
                        //skilar bara tveimur nidurstodu per page
                        .findPagingList(2);
        Page<Cocktail> page = pagingList.getPage(p);
        List<Cocktail> list = page.getList();
        return list;
    };


    public static List<Cocktail> searchByIngredient(Ingredients ingredient){
        return find.where().in("ingredients", ingredient).findList();
        //return find.where().ieq("name", name).findList();
    }

    public static List<Cocktail> searchByIngredients(List<Ingredients> ingredients) {
        String ingredientIds = "";

        for( Ingredients ingr : ingredients ){
            if ( ingredients.indexOf( ingr ) == 0 )
            {
                ingredientIds += ingr.id;
            }
            else
            {
                ingredientIds += ", "  + ingr.id;
            }
        }

        String sql = "select id, name, description from " +
                "(select " +
                "count(ingredients_id) as matching_ingredients, " +
                "(select count(*) from cocktail_ingredients as ci0 where ci0.cocktail_id = c.id) as total_ingredients, " +
                "((select count(*) from cocktail_ingredients as ci0 where ci0.cocktail_id = c.id) - count(ingredients_id)) as missing_ingredients, " +
                "c.name, " +
                "c.id, " +
                "c.description " +
                "from " +
                "cocktail c " +
                "left join " +
                "cocktail_ingredients ci " +
                "on c.id = ci.cocktail_id " +
                "where " +
                "ingredients_id in ("+ ingredientIds +") " +
                "group by c.id,c.name " +
                "order by missing_ingredients asc) as subq;";

        RawSql rawSql =
                RawSqlBuilder.parse(sql)
                    .columnMapping("id", "id")
                    .columnMapping("name", "name")
                    .columnMapping("description", "description")
                        .create();

        Query<Cocktail> query = Ebean.find(Cocktail.class);
        query.setRawSql(rawSql);

        return query.findList();
    }
}