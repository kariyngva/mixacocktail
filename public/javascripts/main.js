(function(){
  var $ = jQuery,
      $html = $( 'html' ),
      $win =  $( window ),
      $body = $( 'body' ),
      $doc = $( document );


  // =========================================================================================================================
  //   Actions
  // =========================================================================================================================


  var prepSearch = function () {
          var form = $('.search form'),
              searchInput = form.find('.fi_txt input'),
              tagList = $('.tags ul');

          form
              .on('submit', function (e) {
                  e.preventDefault();

                  if ( searchInput.val().length )
                  {
                    tagList.prepend('<li><span>' + searchInput.val() + '</span><a class="removetag" href="#removetag">x</a></li>');
                    searchInput.val('');
                    //framkvæma leit);
                    getCocktails( form.attr('action'), getTagList() );
                  }
                  else if ( !tagList.find('li').length )
                  {
                    $('.results').empty();
                  }
                });

          $doc
              .on('click', '.search .removetag', function (e) {
                  e.preventDefault();
                  var link = $(this),
                      li  = link.parent(),
                      liTextElm = link.parent().find('span');

                  li.remove();

                  if ( !tagList.find('li').length )
                  {
                    $('.results').empty();
                  }

                  getCocktails( form.attr('action'), getTagList() );

                });
    };

  var getTagList = function () {
          var tagList = $('.tags ul'),
              tagString = "";

          tagList.find('li')
              .each(function (i, elm) {
                  var item = $(this),
                      text = item.find('span').text();

                  tagString += i === 0 ? text : '-' + text;
                });

          return tagString;
    };

  var getCocktails = function ( url, queryString ) {
          if ( queryString.length )
          {
            $html.addClass('ajax-wait');
            $.get(
                  url + '?' +  queryString
                )
              .done(function(data) {
                  $('.results').empty();
                  $('.results').append( generateMarkup(data) );
                })
              .always(function() {
                  $html.removeClass('ajax-wait');
                });
          }
    }

  var generateMarkup = function( data ) {
          //[{"id":"1","name":"Mojito","description":"Desc here","ingredients":[{"id":"1","name":"Rum"},{"id":"9","name":"Sugar"}]},{"id":"4","name":"Strawberry Daquiri","description":"Desc here","ingredients":[{"id":"1","name":"Rum"},{"id":"7","name":"Strawberries"}]}]
          var results = $('<div class="rescontainer"></div>');
          for (var i = 0; i < data.length; i++) {
              var cjson = data[i],
                  ingredients = $('<ul></ul>'),
                  cocktailElm = $('<div class="cocktail">' +
                                    '<h2>' + cjson.name + '</h2>' +
                                    '<p>Description:<br/>' + cjson.description + '</p>' +
                                    '<p>' + 'Ingredients : '+ '</p>'+
                                    '<div class="rating">no rating yet</div>' +
                                  '</div>');

              //Iterate over ingredients for given cocktail
              for (var j = 0; j < cjson.ingredients.length; j++) {
                  var ingredient = cjson.ingredients[j];
                  ingredients.append('<li>' + ingredient.name + '</li>');
              }

              //add ingredients to element
              cocktailElm.append( ingredients );
              results.append( cocktailElm );
          }
          return results;
    };

  // =========================================================================================================================
  //   Run Init Actions
  // =========================================================================================================================

  prepSearch();

})();