package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by elsamjoll on 2/3/15.
 */
@Entity
public class Cocktail extends Model {
    @Id
    public String id;
    public String name;
    public String description;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private List<Ingredients> ingredients;

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

    public static List<Cocktail> searchByIngredient(Ingredients ingredient){
        return find.where().in("ingredients", ingredient).findList();
        //return find.where().ieq("name", name).findList();
    }

    public static List<Cocktail> searchByIngredients(List<Ingredients> ingredients) {
        //TODO: create proper query.
        return find.where().in("ingredients", ingredients).findList();
    }
}