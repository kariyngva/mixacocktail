package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import play.Logger;
import play.db.ebean.Model;
import scala.collection.immutable.Map;

import javax.persistence.*;
import java.util.List;


/**
 * Created by elsamjoll on 2/3/15.
 */
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

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addIngredient(Ingredients ingredient) {
        ingredients.add(ingredient);
    }

    public List<Ingredients> getIngredients() {
        return this.ingredients;
    }

    public static Finder<String,Cocktail> find = new Finder<String,Cocktail>(
            String.class, Cocktail.class
    );

    public static List<Cocktail> getAllCocktails(int p){
        PagingList<Cocktail> pagingList =
                Ebean.find(Cocktail.class)
                        //skilar bara einni nidurstodu per page
                        .findPagingList(1);
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