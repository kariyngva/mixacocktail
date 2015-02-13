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
                    getCocktails('/cocktails', getTagList() );
                  }

                });

          $doc
              .on('click', '.search .removetag', function (e) {
                  e.preventDefault();
                  var link = $(this),
                      li  = link.parent(),
                      liTextElm = link.parent().find('span');

                  li.remove();
                  getCocktails('/cocktails', getTagList() );

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

          $html.addClass('ajax-wait');
          $.get(
                url + '?' +  queryString
              )
            .done(function(data) {
                data = $(data).find('.results');
                $('.results').append(data);
              })
            .always(function() {
                $html.removeClass('ajax-wait');
              });
    }


  // =========================================================================================================================
  //   Run Init Actions
  // =========================================================================================================================

  prepSearch();

})();