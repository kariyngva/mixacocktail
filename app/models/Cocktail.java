/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 03.02.15
 *
 * Klasinn geymir Cokctail hlut sem erfir frá Model.
 */
package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import play.Logger;
import play.db.ebean.Model;
import scala.collection.immutable.Map;
import javax.persistence.Transient;
import javax.persistence.*;
import java.util.List;


@Entity
public class Cocktail extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(columnDefinition = "TEXT")
    public String preparation;

    @Column(length = 400)
    public String imageUrl;

    @Transient
    public String message = "";
    @Transient
    public int ratingValue = 0;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Ingredients> ingredients;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Rating> ratingList;


    /**
     * Aðferð: Bæta nafni við Cocktail hlut
     * @param name  er nafn kokteils.
     **/
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Aðferð: Bæta lýsingu við Cocktail hlut.
     *
     * @param description er lýsing sem lýsir kokteilnum.
     **/
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Aðferð: Bætir hráefni við Ingredients lista
     *
     * @param ingredient er hráefni í Ingredients lista.
     **/
    public void addIngredient(Ingredients ingredient) {
        ingredients.add(ingredient);
    }

    /**
     * Aðferð: Nær í hráefni úr Ingredient lista.
     *
     * @return: Skilar lista af hráefnum úr Ingredients lista.
     **/
    public List<Ingredients> getIngredients() {
        return this.ingredients;
    }

    /**
     * Aðferð: Nær í stjörnugjöf úr Rating lista.
     *
     * @return: Skilar lista af stjörnugjöfum frá Rating lista.
     **/
    public List<Rating> getRating() {
        return this.ratingList;
    }


    /**
     * Aðferð: Finder er leitar hlutur fyrir Cocktail með ID af taginu Long.
     *
     * @param Cocktail  er hluturinn sem leitarinn er fyrir og Long er
     *                 tagið sem auðkenni hlutarins er af.
     **/
    public static Finder<Long,Cocktail> find = new Finder<Long,Cocktail>(
            Long.class, Cocktail.class
    );

    /**
     * Aðferð: Nær í lista af öllum kokteilum sem eru inni í gagnagrunni og skilar sem paging list með
     *         tvo kokteila per síðu.
     *
     * @return: Skilar lista af kokteilum sem paging list.
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

    /**
     * Aðferð: Leitar í öllum kokteilum eftir ákveðnu hráefni.
     *
     * @return: Skilar lista af kokteilum sem inniheldur það hráefni sem leitað var að.
     **/
    public static List<Cocktail> searchByIngredient(Ingredients ingredient){
        return find.where().in("ingredients", ingredient).findList();
    }

    /**
     * Aðferð: Fyrirspurning okkar á gagnagrunninn, tekur alla kokteila og leitar eftir innslegnu hráefni.
     *
     * @return: Skilar lista af kokteilum sem innihalda eitthvert þeirra hráefna sem leitað var að.
     **/
    public static List<Cocktail> searchByIngredients(List<Ingredients> ingredients) {
        String ingredientIds = "";

        //Byggum upp SQL með því að bæta id hvers hráefnis í strenginn ingredientIds.
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

        //Búum til SQL fyrirspurn sem skilar okkur kokteilum, ásamt hversu mörg hráefni passa og hversu mörg vantar.
        String sql = "select id, name, description, preparation, missing_ingredients from " +
                "(select " +
                "count(ingredients_id) as matching_ingredients, " +
                "(select count(*) from cocktail_ingredients as ci0 where ci0.cocktail_id = c.id) as total_ingredients, " +
                "((select count(*) from cocktail_ingredients as ci0 where ci0.cocktail_id = c.id) - count(ingredients_id)) as missing_ingredients, " +
                "c.name, " +
                "c.id, " +
                "c.description, " +
                "c.preparation " +
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
                    .columnMapping("preparation", "preparation")
                    .columnMapping("missing_ingredients", "message")
                        .create();

        Query<Cocktail> query = Ebean.find(Cocktail.class);
        query.setRawSql(rawSql);

        return query.findList();
    }

    /**
     * Aðferð: Gefur kokteil einkunn, ef einkunn er fyrir til frá notanda er hún yfirskrifuð með nýju gildi
     *
     * @param  userid er auðkenni notanda og rating er gildi stjörnugjafar
     **/
    public void addRating(String userid, int rating){
        Rating r = Rating.findUserandRating(this.id,userid);
        if(r != null){
            Logger.info("get rating"+ r.getRating());
            r.setRating(rating);
        }
        else{
            r = new Rating(userid, rating);
            this.ratingList.add(r);
        }
        this.save();
    }

    /**
     * Aðferð: Fyrirspurning á gagnagrunninn, finnur öll auðkenni kokteils út grá Cocktail klasanum.
     *
     * @return: Skilar öllum þeim kokteilum sem eru með auðkenni.
     **/
    public static Cocktail findById(Long cid){
        return Ebean.find(Cocktail.class)
                .where()
                .eq("id", cid)
                .findUnique();
    }
}