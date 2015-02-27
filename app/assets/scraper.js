var request = require('request');
var cheerio = require('cheerio');
var Promise = require('promise');

var scrape = function (url) {
        var cocktail;
        requestPromise(url)
            .then(function (data) {
                $ = cheerio.load(data);
                var cocktail;
                var ingredientNames = [],
                    ingredientAmount = [];

                //We assume the measuring unit is "cl"
                $('td.ingredient ul li')
                    .each(function () {

                        var ingredient = $(this).text().split('cl');
                        ingredientNames.push( ingredient[1] );
                        ingredientAmount.push( parseFloat( ingredient[0] )  );
                      });

                cocktail = {'name': $('#firstHeading').text(),
                                  'description': $('p').not('table p').eq(0).text(), // this is hopefully the description.
                                  'ingredientNames' : ingredientNames,
                                  'ingredientAmount' : ingredientAmount
                                };
              }, function (err) {
                  console.error("%s; %s", err.message, url);
                });
  };

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
                            var ingredient = $(this).text().split('cl');
                            ingredientNames.push( ingredient[1] );
                            ingredientAmount.push( parseFloat( ingredient[0] )  );
                          });

                    cocktail = {'name': $('#firstHeading').text(),
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


  request('http://en.wikipedia.org/wiki/List_of_IBA_official_cocktails', function (err, resp, body) {
      if ( err )
      {
        throw err;
      }
      $ = cheerio.load(body);
      var lis = $('.multicol li');

      lis
          .each(function (i, elm) {
            var li = $(this);
              if ( !li.find('a.new').length && i < 10)
              {
                scrapePromise('http://en.wikipedia.org' + li.find('a').eq(0).attr('href') ).done(function (res) {
                  console.log( res );
                  //todo save object to mysql
                  });
              }
            });
    });