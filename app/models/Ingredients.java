/**
 * @author: Hópur 7; Kári Yngva, Elsa Mjöll og Rakel Björt
 * @since: 03.02.15
 *
 * Klasinn geymir Ingredients hlut sem erfir frá Model.
 */
package models;

import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class Ingredients extends Model {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    public String name;

    /**
     * Aðferð: Bæta nafni við Ingredients hlut
     *
     * @param name  er nafn hráefnis.
     **/
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Aðferð: Finder er leitar hlutur fyrir Ingredients með ID af taginu Long.
     *
     * @param Ingredients hluturinn sem leitarinn er fyrir og Long er
     *                 tagið sem auðkenni hlutarins er af.
     **/
    public static Finder<Long,Ingredients> find = new Finder<Long,Ingredients>(
            Long.class, Ingredients.class
    );

    /**
     * Aðferð: Leitar í Ingredients lista eftir innslegnu nafni hráefnis.
     *
     * @return: Skilar lista af hráefni sem inniheldur það nafn sem leitað var að.
     **/
    public static List<Ingredients> searchByName(String name){
        return find.where().ieq("name", name).findList();
    }

    /**
     * Aðferð: Leitar í Ingredients lista að mörgun innslegnum hráefnum, eftir nafni.
     *
     * @return: Skilar fylki af hráefnum sem innihalda þau nöfn sem leitað var að.
     **/
    public static List<Ingredients> searchByNames(String[] names) {
        ArrayList<Ingredients> results = new ArrayList<Ingredients>();
        for ( String name : names ) {
            results.add( find.where().ieq("name", name).findUnique() );
        }
        return results;
    }
}
