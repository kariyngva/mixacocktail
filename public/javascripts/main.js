(function(){
  var $ = jQuery,
      $html = $( 'html' ),
      $win =  $( window ),
      $body = $( 'body' ),
      $doc = $( document ),
      canSubmit = false;


  // =========================================================================================================================
  //   Actions
  // =========================================================================================================================
    /**
     * Aðferð: Bindur atburði við hlekki valmyndar og framkvæmir leitir
     *
     **/
  var prepNav = function(){
          var links = $('.nav');
          var cLink = links.find('.list');
          var cLinkHref = cLink.attr('href');
          var sLink = links.find('.searchLink');
          var sLinkHref = cLink.attr('href');
          var page = 0;
          var listofcocktails;
          //fyrir listofcocktails takkann, athugum hvort hann sé með klasann current
          //ákveðið margar kokteil niðurstöður birtast, load-ast meiri kokteilar
          //þegar scrollað er á neðsta part síðunar.
          cLink
              .on('click', function(e){
                e.preventDefault();

                if(!$(this).is('.current'))
                {
                  $('.results').empty();
                  $('.search .fi_btn input').val('Filter');
                  $('body').addClass('listActive');
                  $('.nav .current').removeClass('current');
                  $(this).addClass('current');
                  getCocktails( cLinkHref, '/' + page,false );
                  page++;
                  if(listofcocktails)
                  {
                    $('.results').prepend(listofcocktails);
                  }
                  $win.on('scroll.bottom', function(){
                    //Skroll fyrir listofcocktails takkann
                    if( $win.scrollTop() == $doc.height() - $win.height() )
                    {
                      getCocktails(cLinkHref, '/' + page,false );
                      page++;
                    }
                  });
                }
              });
          //fyrir Search takkann, athugum hvort hann sé með klasann current
          sLink
              .on('click', function(e){
                e.preventDefault();

                if(!$(this).is('.current'))
                { 
                  if( $('.results').contents().length )
                  {
                    listofcocktails = $('.results').contents();
                    $('.results').empty();
                  }
                  $('body').removeClass('listActive');
                  $('.search .fi_btn input').val('Add');
                  $('.nav .current').removeClass('current');
                  $(this).addClass('current');
                  getCocktails( '/findCocktailByIngredient', '?' + getTagList(), true );
                  //Skroll fyrir search takkann
                  $win.off('scroll.bottom');

                }
              });

  };
    /**
     * Aðferð: Bindir atburði við leitar form, tekur streng úr leitarformi og setur í hráefna-lista
     *         Framkvæmir leit út frá hráefnalista
     **/
  var prepSearch = function () {
          var form = $('.search form'),
              searchInput = form.find('.fi_txt input'),
              tagList = $('.tags ul');

    var availableTags = [
      "Rum",
      "Vodka",
      "Tequila",
      "Gin",
      "Passoa",
      "Coke",
      "Strawberries",
      "Tonic",
      "Sugar"
    ];
    searchInput.autocomplete({

      source: function(request, response){


        $.ajax({
          url: '/getIngredients/' + request.term,
          dataType: "json",
          data:request.term,


          success: function( data ) {
            var results = [];
            for (var i = 0; i < data.length; i++) {
              delete data[i].id;
              results.push(data[i].name);
            };
            response( results );
          }
        });
      },  
      autoFocus: true,
      minLength:3,

      change: function (event, ui) {
          console.log('change');
          if(ui.item)
          {
            canSubmit = true;
          }
          else
          {
            canSubmit = false;
          }

        },
      select : function(event, ui) {
          if( $('.searchLink').is('.current')) //ef leit er valin
          {
            tagList.prepend('<li><span>' + ui.item.value + '</span><a class="removetag" href="#removetag">x</a></li>');
            searchInput.val('');
            getCocktails( form.attr('action'), '?' + getTagList(), true );
          }
        return false;
      }

    });

          form
              .on('submit', function (e) {
                  e.preventDefault();
                  if ( searchInput.val().length )
                  {
                    if( $('.searchLink').is('.current')) //ef leit er valin
                    {
                      // $( "#search" ).on( "autocompletechange", function( event, ui ) {
                      // } );
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
 /**
  * Aðferð: Breytir hráefnalista í streng þar sem hráefnin eru aðskilin með bandstriki
  *
  * @return: Skilar streng af hráefnum.
  **/
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
 /**
  * Aðferð: Sækir kokteila á JSON formi og setur þá inn í .results
  *
  **/
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
 /**
  * Aðferð: Tekur inn fylki af kokteilum á JSON formi
  *
  * @return: Skilar Markup fyrir hvern kokteil í fylkinu
  **/
  var generateMarkup = function( data ) {

          var results = $('<div class="rescontainer"></div>');
          for (var i = 0; i < data.length; i++) {
              var cjson = data[i],
              message = parseInt( cjson.message ) > 0 ? '<p>Missing : ' + cjson.message +' Ingredients<p>' : ''
                  ingredients = $('<ul></ul>'),
                  cocktailElm = $('<div class="cocktail">' +
                                    '<h2>' + cjson.name + '</h2>' +
                                    message +
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