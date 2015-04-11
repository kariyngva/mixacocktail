package models;

import com.avaje.ebean.Ebean;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by kari on 10/04/15.
 */
@Entity
public class UserIngredient extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userid;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Ingredients> ingredients;

    public UserIngredient(String userid) {
        this.userid = userid;
    }


    /**
     * Aðferð: Bætir hráefni við Ingredients lista notanda.
     *
     * @param ingredients er listi hráefni sem á að vista fyrir notanda.
     **/
    public void addUserIngredients(List<Ingredients> ingredients) {
        this.ingredients = ingredients;
        this.save();
    }

    public void clearUserIngredients() {
        this.ingredients.clear();
        this.save();
    }


    /**
     * Aðferð: Nær í hráefni úr Ingredient lista notanda.
     *
     * @return: Skilar lista af hráefnum úr Ingredients lista notanda.
     **/
    public List<Ingredients> getUserIngredients() {
        return this.ingredients;
    }

    public static UserIngredient findById(String uid){
        return Ebean.find(UserIngredient.class)
                .where()
                .eq("userid", uid)
                .findUnique();
    }
}
