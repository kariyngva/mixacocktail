var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');
var mysql   = require('mysql');
var http    = require('http');
var allIngredients = [];

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

var scrapePromise = function (url) {
        return new Promise(function (resolve, reject) {
            requestPromise(url)
                .then(function (data) {
                    $ = cheerio.load(data);
                    var cocktail;
                    var ingredientNames = [],
                        ingredientAmount = [];

                    //We assume the measuring unit is "cl"
                    $('td.ingredient ul li')
                        .each(function () {
                            var ingredient = $(this).text().split('cl'),
                                name = ingredient[1].trim().toLowerCase(),
                                amount = parseFloat( ingredient[0] );

                            ingredientNames.push( name );
                            ingredientAmount.push( amount );

                            if ( allIngredients.indexOf( name ) < 0 )
                            {
                              allIngredients.push({ 'name': name, 'amount': amount });
                            }
                          });

                    cocktail = {'name': $('#firstHeading').text().trim(),
                                      'description': $('p').not('table p').eq(0).text(), // this is hopefully the description.
                                      'ingredientNames' : ingredientNames,
                                      'ingredientAmount' : ingredientAmount
                                    };

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


  var connection = mysql.createConnection({
    host     : 'mixacocktail.net',
    user     : 'play',
    password : 'solowsolow',
    database : 'scrape_test'
  });

  // connection.connect(function(err) {
  //   if ( err )
  //   {
  //     console.log('mysql failure ' + err);
  //   }
  //   else
  //   {
  //     console.log('mysql connection made');
  //   }
  // });
  var allCocktails = [];

  var handleIngredients = function ( conn, name, cid ) {
        var id;

        // var nameCorrect = name.indexOf
        //remove parts from name e.g. (1 part) or (2 parts)
        name = name.replace(/^\([0-9]\s\bpart[s]?\)\s(\w+)/, '$1');

        conn
            .query('SELECT id FROM ingredients WHERE name = ?', name.trim(), function ( err, result ) {
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
                  if ( !id )
                  {
                    //insert the ingredient and update id
                    conn.query('INSERT INTO ingredients SET ?', {'name': name}, function ( err, result ) {
                        if ( err )
                        {
                          conn.rollback(function() {
                              throw err;
                            });
                        }

                        id = result.insertId;
                      }).on('end', function () {
                            console.log( 'insert cocktail to ingredients relation' );
                            console.log( {'ingredients_id': id, 'cocktail_id': cid} );
                            insertCocktailIngredientsRelation( conn, {'ingredients_id': id, 'cocktail_id': cid} );
                        });
                  }
                  else
                  {
                    console.log( 'insert cocktail to ingredients relation' );
                    console.log( {'ingredients_id': id, 'cocktail_id': cid} );
                    insertCocktailIngredientsRelation( conn, {'ingredients_id': id, 'cocktail_id': cid} );
                  }
                });
    };

  //Inserts a cocktail, it's ingredients and the nessecary relations between ingredients and the cocktail
  var insertCocktail = function ( conn, name, description, ingredients ) {
          var cocktailName = name.indexOf('(cocktail)') > -1 ? name.substr( 0, name.indexOf('(cocktail)') ).trim() : name.trim();
          var tempCocktail = {'name': cocktailName, 'description': description},
              id;

        conn
            .query('SELECT id FROM cocktail WHERE name LIKE ?', name, function ( err, result ) {
                //if result.length is larger than
                if ( result.length > 0 )
                {
                  id = result[0].id;
                }
              }).on('end', function () {
                    conn.query('INSERT INTO cocktail SET ?', tempCocktail, function (err, result) {
                        if ( err )
                        {
                          conn.rollback(function() {
                              throw err;
                            });
                        }

                        //only set tempCocktail.id if globa id var is not set
                        //i.e. only set it if the cocktail does not exist
                        if ( !id )
                        {
                          tempCocktail.id = result.insertId;
                        }

                      }).on('end', function () {

                          //if tempCocktail.id is not set we can update cocktail_ingredients
                          if ( tempCocktail.id )
                          {
                              for (var i = 0; i < ingredients.length; i++)
                              {
                                handleIngredients(conn, ingredients[i], tempCocktail.id);
                              }
                          }
                        });
                });
    };

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


requestPromise('http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails')
      .done(function (res) {
          $ = cheerio.load(res);
          var lis = $('.multicol li');

          lis
              .each(function (i, elm) {
                var li = $(this);
                  {
                    scrapePromise('http://en.wikipedia.org' + li.find('a').eq(0).attr('href') )
                        .done(function (res) {
                            // var cocktail_ingredients = [];
                            insertCocktail( connection, res.name, res.description, res.ingredientNames );
                      });
                  }
                });

        });



