package models;

import play.db.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by kari on 03/02/15.
 */
@Entity
public class Ingredients extends Model {
    @Id
    public String id;
    public String name;
}
