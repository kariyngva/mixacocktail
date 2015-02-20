package models;

import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kari on 03/02/15.
 */
@Entity
public class Ingredients extends Model {
 //   @ManyToMany(cascade = CascadeType.PERSIST)

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    public String name;

    public void setName(String name) {
        this.name = name;
    }

    public static Finder<Long,Ingredients> find = new Finder<Long,Ingredients>(
            Long.class, Ingredients.class
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
