package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
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
    public List<Ingredients> ingredients;
}