package models;

import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Ingredients> ingredients;
}