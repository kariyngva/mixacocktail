package models;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by elsamjoll on 3/27/15.
 */
@Entity
public class Rating extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userid;
    private int rating;

    public Rating(String userid, int rating) {
        this.userid = userid;
        this.rating = rating;
    }

    public static Finder<Long,Cocktail> find = new Finder<Long,Cocktail>(
            Long.class, Cocktail.class
    );

    public int getRating() {
        return this.rating;
    }

}
