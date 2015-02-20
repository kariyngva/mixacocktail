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
                        .findPagingList(10);
        Page<Cocktail> page = pagingList.getPage(p);
        List<Cocktail> list = page.getList();
        return list;
    };


    public static List<Cocktail> searchByIngredient(Ingredients ingredient){
        return find.where().in("ingredients", ingredient).findList();
        //return find.where().ieq("name", name).findList();
    }

    public static List<Cocktail> searchByIngredients(List<Ingredients> ingredients) {
        String sql = "SELECT id, name, description FROM cocktail WHERE id IN " +
                      "(SELECT cocktail_id FROM cocktail_ingredients WHERE ingredients_id IN" +
                      " (SELECT id FROM ingredients";

        int listSize = ingredients.size() == 1 ? 0 : 1;

        for( Ingredients ingr : ingredients ){
            if ( ingredients.indexOf( ingr ) == 0 )
            {
                sql += " WHERE name = '" + ingr.name + "'";
            }
            else
            {
                sql += " OR name = '" + ingr.name + "'";
            }
        }

        sql += ") group by cocktail_id HAVING COUNT(*)>" + listSize + ");";


        Logger.info("--------------");
        Logger.info( "" + ingredients.size() );
        Logger.info(sql);
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