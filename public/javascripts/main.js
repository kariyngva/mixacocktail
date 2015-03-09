(function(){
  var $ = jQuery,
      $html = $( 'html' ),
      $win =  $( window ),
      $body = $( 'body' ),
      $doc = $( document );


  // =========================================================================================================================
  //   Actions
  // =========================================================================================================================

  var prepNav = function(){
          var links = $('.nav');
          var cLink = links.find('.list');
          var cLinkHref = cLink.attr('href');
          var sLink = links.find('.searchLink');
          var sLinkHref = cLink.attr('href');
          var page = 0;
          var listofcocktails;
          //fyrir listofcocktails takkann
          cLink
              .on('click', function(e){
                e.preventDefault();
                if(!$(this).is('.current'))
                {
                  $('.results').empty();
                  $('.search .fi_btn input').val('Filter');
                  $('.nav .current').removeClass('current');
                  $(this).addClass('current'); 
                  getCocktails( cLinkHref, '/' + page,false );
                  page++;
                  if(listofcocktails)
                  {
                    $('.results').prepend(listofcocktails);
                  }
                  $win.on('scroll', function(){
                    //Skroll fyrir listofcocktails takkann
                    if( $win.scrollTop() == $doc.height() - $win.height() )
                    {
                      getCocktails(cLinkHref, '/' + page,false );
                      page++;
                    }
                  });
                }             
              });
          //fyrir search takkann
          sLink
              .on('click', function(e){
                e.preventDefault();
                if(!$(this).is('.current'))
                {
                  if($('.results').contents().length )
                  {
                    listofcocktails = $('.results').contents();
                  }
                  $('.search .fi_btn input').val('Add');
                  $('.nav .current').removeClass('current');
                  $(this).addClass('current'); 
                  getCocktails( sLinkHref, '/' + page,true );
                  page++;
                  //Skroll fyrir search takkann
                  $win.on('scroll', function(){
                    if( $win.scrollTop() == $doc.height() - $win.height() )
                    {
                      //getCocktails(sLinkHref, '/' + page,false );
                      //page++;
                    }
                  });
                }
              });

  };

  var prepSearch = function () {
          var form = $('.search form'),
              searchInput = form.find('.fi_txt input'),
              tagList = $('.tags ul');

          form
              .on('submit', function (e) {
                  e.preventDefault();

                  if ( searchInput.val().length )
                  {
                    if( $('.searchLink').is('.current') ) //ef leit er valin
                    {
                      tagList.prepend('<li><span>' + searchInput.val() + '</span><a class="removetag" href="#removetag">x</a></li>');
                      searchInput.val('');
                      //framkvæma leit);
                      getCocktails( form.attr('action'), '?' + getTagList(),true );                      
                    }
                    else //annars
                    {
                      //filtera listann
                    }
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

                  getCocktails( form.attr('action'), '?' + getTagList(),true );

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

  var getCocktails = function ( url, queryString, empty ) {
          if ( queryString.length )
          {
            $html.addClass('ajax-wait');
            $.get(
                  url +  queryString
                )
              .done(function(data) {
                  empty && $('.results').empty();
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
  prepNav();

})();