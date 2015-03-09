var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');
var mysql   = require('mysql');
var http    = require('http');

/**
 * Aðferð: Skilar request-loforði sem skilar niðurstöðu úr asynchronous aðgerð
 * url er vefslóð sem á að senda fyrirspurn á
 **/
var requestPromise = function (url) {
        return new Promise(function (resolve, reject) {
            request(url, function (err, resp, body) {
                if ( err )
                {
                  return reject( err );
                }
                else if ( resp.statusCode != 200 )
                {
                  err = new Error( "Response not OK: " + resp.statusCode );
                  return reject( err );
                }

                resolve( body );
              });
          });
  };
/**
 * Aðferð: Skilar scrape-loforði sem skilar niðurstöðu úr asynchronous aðgerð
 * url er vefslóð sem á að senda fyrirspurn á
 **/
var scrapePromise = function (url) {
        return new Promise(function (resolve, reject) {
            requestPromise(url)
                .then(function (data) {
                    //Þegar fyrirspurnin sem gerð er í requestPromise(url) skilar "data" sem er HTML
                    //sem fannst á slóðinni sem gefin var.

                    $ = cheerio.load(data); //cheerio virkar soldið eins "window" object í browser
                    var cocktail;
                    var ingredientNames = [],
                        ingredientAmount = [];

                    //grf. að allar einingar séu í "cl"
                    $('td.ingredient ul li')
                        .each(function () {
                            var ingredient = $(this).text().split('cl'),
                                name = ingredient[1].trim().toLowerCase(),
                                amount = parseFloat( ingredient[0] );

                            ingredientNames.push( name );
                            ingredientAmount.push( amount );
                          });

                    //Útbúm kokteil hlut út frá efni síðunnar
                    cocktail = {'name': $('#firstHeading').text().trim(),
                                      'description': $('p').not('table p').eq(0).text(), // this is hopefully the description.
                                      'ingredientNames' : ingredientNames,
                                      'ingredientAmount' : ingredientAmount
                                    };

                    //Ef cocktail er ekki undefined getum við uppfyllt (resolve) loforðið.
                    if ( cocktail )
                    {
                      resolve( cocktail );
                    }
                    else
                    {
                      return reject( new Error( "failed to build cocktail" ) );
                    }
                  }, function (err) {
                      console.error("%s; %s", err.message, url);
                    });
          });
  };

//Búum til gagnagrunnstengingu
var connection = mysql.createConnection({
  host     : 'mixacocktail.net',
  user     : 'play',
  password : 'solowsolow',
  database : 'scrape_test'
});


/**
 * Aðferð: Sér um að setja inn hráefni og tengingar millihráefna og kokteila í gagnagrunn.
 * conn er tenging við gagnagrunn, name er nafn hráefnis og cid er auðkenni kokteils í grunninum
 **/
var handleIngredients = function ( conn, name, cid ) {
      var id;

      //Fjarlægjum rusl úr nafni t.d. (1 part) or (2 parts)
      name = name.replace(/^\([0-9]\s\bpart[s]?\)\s(\w+)/, '$1');

      conn
          .query('SELECT id FROM ingredients WHERE name = ?', name.trim(), function ( err, result ) {
              //Ef hráefni með nafninu fannst viljum við geyma það í id breytunni
              if ( result.length > 0 )
              {
                console.log('ingredients query:' + name);
                console.log(result);
                id = result[0].id;
              }
              else
              {
                console.log('ingredients query:' + name);
                console.log(result);
              }
            }).on('end', function () {
                //Sé id ekki skilgreind viljum við setja hráefnið í gagnagrunninn
                if ( !id )
                {
                  conn.query('INSERT INTO ingredients SET ?', {'name': name}, function ( err, result ) {
                      if ( err )
                      {
                        conn.rollback(function() {
                            throw err;
                          });
                      }

                      id = result.insertId;
                    }).on('end', function () {
                          //Eftir að hráefnið hefur verið sett í grunninn tengjum við það viðeigandi kokteil
                          insertCocktailIngredientsRelation( conn, {'ingredients_id': id, 'cocktail_id': cid} );
                      });
                }
                else
                {
                  //Hafi hráefnið verið til í grunninn tengjum við það viðeigandi kokteil
                  insertCocktailIngredientsRelation( conn, {'ingredients_id': id, 'cocktail_id': cid} );
                }
              });
  };

/**
 * Aðferð: Sér um að setja inn hráefni og tengingar millihráefna og kokteila í gagnagrunn.
 * conn er tenging við gagnagrunn, name er nafn kokteils, description er lýsing kokteils
 * og ingredients er fylki af hráefnum kokteils.
 **/
var insertCocktail = function ( conn, name, description, ingredients ) {
        var cocktailName = name.indexOf('(cocktail)') > -1 ? name.substr( 0, name.indexOf('(cocktail)') ).trim() : name.trim();
        var tempCocktail = {'name': cocktailName, 'description': description},
            id;

      conn
          .query('SELECT id FROM cocktail WHERE name LIKE ?', name, function ( err, result ) {
              //Ef kokteill með nafninu fannst viljum við geyma auðkenni hans í id breytunni
              if ( result.length > 0 )
              {
                id = result[0].id;
              }
            }).on('end', function () {
                  //Þetta ætti e.t.v. að vera í if setningunni að neðan
                  conn.query('INSERT INTO cocktail SET ?', tempCocktail, function ( err, result ) {
                      if ( err )
                      {
                        conn.rollback(function() {
                            throw err;
                          });
                      }

                      //sé id ekki skilgreint uppfærum við
                      if ( !id )
                      {
                        tempCocktail.id = result.insertId;
                      }
                    }).on('end', function () {
                        //Ef tempCocktail.id er skilgreint viljum við setja inn og tengja hráefni fyrir kokteilinn
                        if ( tempCocktail.id )
                        {
                            //Ítrum yfir hráefni kokteilsins
                            for (var i = 0; i < ingredients.length; i++)
                            {
                              handleIngredients(conn, ingredients[i], tempCocktail.id);
                            }
                        }
                      });
              });
  };

/**
 * Aðferð: Sér um að búa til tengingar milli hráefnis og kokteils
 * conn er tenging við gagnagrunn, cirelation er hlutur sem heldur á auðkenni kokteils og auðkenni hráefnis
 **/
var insertCocktailIngredientsRelation = function ( conn, cirelation ) {
      delete cirelation.cocktailName;

      conn.query('INSERT INTO cocktail_ingredients SET ?', cirelation, function ( err, result ) {
          if ( err )
          {
            connection.rollback(function() {
                throw err;
              });
          }
        });
};


//Framkvæmum fyrirspurn á http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails
requestPromise('http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails')
    .done(function (res) {
        //Þegar loforðið er uppfyllt finnum við lista af hlekkjum á síðunni
        $ = cheerio.load(res);
        var lis = $('.multicol li');

        //ítrum í gegnum listann
        lis
            .each(function (i, elm) {
              var li = $(this);
                //hafi hlekkurinn ekki klasann '.new' viljum við skrapa efnið á slóð hans
                if ( !li.find('a.new').length )
                {
                  //fyrir hvern hlekk í listanum gerum við nýja fyrirspurn með scrapePromise
                  //li.find('a').eq(0).attr('href') skilar vonandi okkur slóð á síðu sem inniheldur kokteil
                  scrapePromise('http://en.wikipedia.org' + li.find('a').eq(0).attr('href') )
                      .done(function (res) {
                          //þegar srapePromise er uppfyllt setjum við upplýsingarnar í gagnagrunn
                          insertCocktail( connection, res.name, res.description, res.ingredientNames );
                    });
                }
              });
      });