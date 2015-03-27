var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');
var mysql   = require('mysql');
var http    = require('http');
var async    = require('async');

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
                        ingredientAmount = [],
                        prepElm = $('table th'),
                        preparation = "",
                        imgUrl = $('td .image img').not('.metadata img, .floatright img').length ? 'http:' + $('td .image img').not('.metadata img, .floatright img').attr('src') : '';

                    prepElm
                        .each(function () {
                            var elm = $(this);

                            if ( elm.text().toLowerCase() == 'preparation' )
                            {
                              preparation = elm.next().text();
                            }
                          });

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
                                      'description'       : $('p').not('table p').eq(0).text(), // this is hopefully the description.
                                      'ingredientNames'   : ingredientNames,
                                      'ingredientAmount'  : ingredientAmount,
                                      'image_url'         : imgUrl,
                                      'preparation'       : preparation
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
      var idwhat;
      //Fjarlægjum rusl úr nafni t.d. (1 part) or (2 parts)
      name = name.replace(/^\([0-9]\s\bpart[s]?\)\s(\w+)/, '$1');

      conn
          .query('SELECT id FROM ingredients WHERE name = ?', name.trim(), function ( err, result ) {
              //Ef hráefni með nafninu fannst viljum við geyma það í id breytunni
              if ( result.length > 0 )
              {
                console.log(result);
                id = result[0].id;
              }
              else
              {
                console.log(result);
              }
              console.log('select id is: ' + id);
            }).on('end', function () {
                //Sé id ekki skilgreind viljum við setja hráefnið í gagnagrunninn
                if ( !id )
                {
                  console.log('before insert id is: ' + id);
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
                          console.log('after id is: ' + id);
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
var insertCocktail = function ( conn, name, description, ingredients, image_url, preparation ) {
        var cocktailName = name.indexOf('(cocktail)') > -1 ? name.substr( 0, name.indexOf('(cocktail)') ).trim() : name.trim();
        var tempCocktail = {
                            'name': cocktailName,
                            'description': description,
                            'image_url': image_url,
                            'preparation': preparation
                          },
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
                  if ( !id )
                  {
                    conn.query('INSERT INTO cocktail SET ?', tempCocktail, function ( err, result ) {
                        if ( err )
                        {
                          conn.rollback(function() {
                              throw err;
                            });
                        }

                        tempCocktail.id = result.insertId;

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
                  }
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
        }).on('end', function () {

          });
};




//Fylki af hlekkjum á kokteila
var linkArray = ['http://en.wikipedia.org/wiki/Alexander_(cocktail)',
'http://en.wikipedia.org/wiki/Americano_(cocktail)',
'http://en.wikipedia.org/wiki/Angel_Face_(cocktail)',
'http://en.wikipedia.org/wiki/Aviation_(cocktail)',
'http://en.wikipedia.org/wiki/Bacardi_cocktail',
'http://en.wikipedia.org/wiki/Casino_(cocktail)',
'http://en.wikipedia.org/wiki/Clover_Club_Cocktail',
'http://en.wikipedia.org/wiki/Daiquiri',
'http://en.wikipedia.org/wiki/Martini_(cocktail)',
'http://en.wikipedia.org/wiki/Fizz_(cocktail)#Gin_Fizz',
'http://en.wikipedia.org/wiki/Manhattan_(cocktail)',
'http://en.wikipedia.org/wiki/Mary_Pickford_(cocktail)',
'http://en.wikipedia.org/wiki/The_Monkey_Gland',
'http://en.wikipedia.org/wiki/Negroni',
'http://en.wikipedia.org/wiki/Old_Fashioned',
'http://en.wikipedia.org/wiki/Paradise_(cocktail)',
'http://en.wikipedia.org/wiki/Planter%27s_Punch',
'http://en.wikipedia.org/wiki/Porto_flip',
'http://en.wikipedia.org/wiki/Fizz_(cocktail)#Ramos_Gin_Fizz',
'http://en.wikipedia.org/wiki/Rusty_Nail_(cocktail)',
'http://en.wikipedia.org/wiki/Sazerac',
'http://en.wikipedia.org/wiki/Screwdriver_(cocktail)',
'http://en.wikipedia.org/wiki/Sidecar_(cocktail)',
'http://en.wikipedia.org/wiki/Stinger_(cocktail)',
'http://en.wikipedia.org/wiki/Whiskey_sour',
'http://en.wikipedia.org/wiki/Sour_(cocktail)#White_Lady',
'http://en.wikipedia.org/wiki/Bellini_(cocktail)',
'http://en.wikipedia.org/wiki/Black_Russian',
'http://en.wikipedia.org/wiki/Bloody_Mary_(cocktail)',
'http://en.wikipedia.org/wiki/Caipirinha',
'http://en.wikipedia.org/wiki/Champagne_Cocktail',
'http://en.wikipedia.org/wiki/Cosmopolitan_(cocktail)',
'http://en.wikipedia.org/wiki/Cuba_Libre',
'http://en.wikipedia.org/wiki/French_Connection_(cocktail)',
'http://en.wikipedia.org/wiki/Godfather_(cocktail)',
'http://en.wikipedia.org/wiki/Amaretto#Beverages',
'http://en.wikipedia.org/wiki/Golden_dream_(cocktail)',
'http://en.wikipedia.org/wiki/Grasshopper_(cocktail)',
'http://en.wikipedia.org/wiki/French_75_(cocktail)',
'http://en.wikipedia.org/wiki/Harvey_Wallbanger',
'http://en.wikipedia.org/wiki/Horse%27s_Neck',
'http://en.wikipedia.org/wiki/Irish_Coffee',
'http://en.wikipedia.org/wiki/Kir_(cocktail)',
'http://en.wikipedia.org/wiki/Long_Island_Iced_Tea',
'http://en.wikipedia.org/wiki/Mai_Tai',
'http://en.wikipedia.org/wiki/Margarita',
'http://en.wikipedia.org/wiki/Mimosa_(cocktail)',
'http://en.wikipedia.org/wiki/Mint_Julep',
'http://en.wikipedia.org/wiki/Mojito',
'http://en.wikipedia.org/wiki/Moscow_Mule',
'http://en.wikipedia.org/wiki/Pi%C3%B1a_Colada',
'http://en.wikipedia.org/wiki/Rose_(cocktail)',
'http://en.wikipedia.org/wiki/Sea_Breeze_(cocktail)',
'http://en.wikipedia.org/wiki/Sex_on_the_Beach',
'http://en.wikipedia.org/wiki/Singapore_Sling',
'http://en.wikipedia.org/wiki/Tequila_Sunrise_(cocktail)',
'http://en.wikipedia.org/wiki/Bramble_(cocktail)',
'http://en.wikipedia.org/wiki/B-52_(cocktail)',
'http://en.wikipedia.org/wiki/Dark_%27N%27_Stormy',
'http://en.wikipedia.org/wiki/Martini_(cocktail)',
'http://en.wikipedia.org/wiki/Espresso_Martini',
'http://en.wikipedia.org/wiki/Kamikaze_(cocktail)',
'http://en.wikipedia.org/wiki/Pisco_Sour',
'http://en.wikipedia.org/wiki/Spritz_Veneziano',
'http://en.wikipedia.org/wiki/Vesper_(cocktail)',
'http://en.wikipedia.org/wiki/Yellow_Bird_(cocktail)'];


//Mix til þess að setja gögn í grunninn okkar
//usage: node scraper.js
var index = 0;

//Þetta interval er hakk til þess að leyfa SQL aðgerðum að keyra "one by one" en
//ekki (mögulega) mörgum í einu. Fallegri leið væri að nota einhversskonar biðröð.
var scrapeInterval = setInterval(function () {
                    console.log( index );

                    if ( index >= linkArray.length-1 )
                    {
                      clearInterval( scrapeInterval );
                    }

                    scrapePromise( linkArray[index] )
                        .then(function (res) {
                          console.log( res );
                            //þegar srapePromise er uppfyllt setjum við upplýsingarnar í gagnagrunn
                            insertCocktail( connection, res.name, res.description, res.ingredientNames, res.image_url, res.preparation );
                      });

                    index++;
                  }, 2000);



//Framkvæmum fyrirspurn á http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails
// requestPromise('http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails')
//     .then(function (res) {
//         //Þegar loforðið er uppfyllt finnum við lista af hlekkjum á síðunni
//         $ = cheerio.load(res);
//         var lis = $('.multicol li');

//         //ítrum í gegnum listann
//         lis
//             .each(function (i, elm) {
//               var li = $(this);
//                 //hafi hlekkurinn ekki klasann '.new' viljum við skrapa efnið á slóð hans
//                 console.log()
//                 if ( !li.find('a.new').length )
//                 {
//                   console.log( li.find('a').eq(0).attr('href') );
//                   //fyrir hvern hlekk í listanum gerum við nýja fyrirspurn með scrapePromise
//                   //li.find('a').eq(0).attr('href') skilar vonandi okkur slóð á síðu sem inniheldur kokteil
//                   scrapePromise('http://en.wikipedia.org' + li.find('a').eq(0).attr('href') )
//                       .then(function (res) {
//                           //þegar srapePromise er uppfyllt setjum við upplýsingarnar í gagnagrunn
//                           console.log( 'lul' );

//                           insertCocktail( connection, res.name, res.description, res.ingredientNames );
//                     });
//                 }
//               });
//       });


