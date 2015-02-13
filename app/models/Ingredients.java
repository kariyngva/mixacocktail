package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kari on 03/02/15.
 */
@Entity
public class Ingredients extends Model {
 //   @ManyToMany(cascade = CascadeType.PERSIST)

    @Id
    public String id;

    @Constraints.Required
    public String name;

    public void setName(String name) {
        this.name = name;
    }

    public static Finder<String,Ingredients> find = new Finder<String,Ingredients>(
            String.class, Ingredients.class
    );

    public static List<Ingredients> searchByName(String name){
        return find.where().ieq("name", name).findList();
    }

    public static List<Ingredients> searchByNames(String[] names) {
        ArrayList<Ingredients> results = new ArrayList<Ingredients>();
        for ( String name : names ) {
            results.add( find.where().ieq("name", name).findUnique() );
        }
        return results;
    }
}
