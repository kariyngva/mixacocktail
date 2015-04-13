/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 03.27.15
 *
 * Klasinn geymir Rating hlut sem erfir frá Model.
 */

package models;

import com.avaje.ebean.Ebean;
import play.db.ebean.Model;

import javax.persistence.*;

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
     * Aðferð: Nær í rating/stjörnugjafar gildi frá Rating hlut
     *
     * @return skilar rating/stjörnugjafar gildi.
     **/
    public int getRating() {
        return this.rating;
    }


    /**
     * Aðferð: Fyrirspurn á gagnagrunninn að finna auðkenni notandans og stjörnugjöf hans.
     *
     * @return Skilar Rating hlut með auðkenni notandans og stjörnugjöf hans.
     **/
    public static Rating findUserandRating(Long cid, String userid){
        return Ebean.find(Rating.class).where().eq("userid",userid).eq("cocktail_id",cid).findUnique();

    }
    /**
     * Aðferð: Vistar stjörnugjöf í Rating hlut.
     * @param rating  er stjörnugjöf kokteils.
     **/
    public void setRating(int rating){
        this.rating = rating;
        this.save();
    }
}
