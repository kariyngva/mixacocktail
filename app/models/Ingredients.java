package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

/**
 * Created by kari on 03/02/15.
 */
@Entity
public class Ingredients extends Model {
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
}
