###
  Author: Jinhyuk Lee
###

jQuery ->
  $body = $('body')

  event =
    afterLoad: () ->
      logo = $('#animation-mask #logo')
      if mintpresso.doneLoading is true
        if mintpresso.waitForLoading is true
          if logo.is(':hidden')
            logo.fadeIn 1000, 'easeInQuint', (e) ->
              event.afterLoad()
          else
            setTimeout event.afterLoad, 500
        else
          logo.hide()
          $('#single-page div.modal').animate {marginTop: '15.5px'}, 1000, 'easeOutQuint'
          $('#animation-mask').fadeOut {
            duration: 1000
            easing: 'easeOutQuint'
          }
      else
        if logo.is(':hidden')
          logo.fadeIn 1000, 'easeInQuint', (e) ->
            event.afterLoad()
        else
          setTimeout event.afterLoad, 500
      true

  mintpresso.waitForLoading = false if mintpresso.waitForLoading is undefined
  mintpresso.doneLoading = false if mintpresso.doneLoading is undefined
  mintpresso.loadingInterval = 60 * 3

  $meta = $('meta[name=animation]')
  if $meta.length > 0 and $meta isnt undefined
    content = $meta[0].getAttribute('content')
    if content is "fadein"
      color = if $('#single-page').is('.white') then 'white' else 'black'
      $body.append """
        <div id="animation-mask" class="#{color}">
          <div id="logo" class="hide #{color}"></div>
        </div>
      """
      $('#single-page').show()
      $(window).load (e) ->
        mintpresso.doneLoading = true
        event.afterLoad()

  $meta = $('meta[name=panel]')
  if $meta.length > 0 and $meta isnt undefined
    mintpresso.page = $meta[0].getAttribute('content')

    if mintpresso.page is 'overview'
      $submenu = $("#submenu")
      $content = $("#content")

      $submenu.find('[data-menu=usage]').click (e) ->
        console.log "dd"
        $block = $content.find('[data-content=usage]')
        if $block.is('.active')
          return true

        $content.find('.active').fadeOut {
          duration: 300
          easing: 'easeOutQuint'
        }

        $submenu.removeClass('active')
        $(this).addClass('active')

        state = $block.data('state')
        if state is "0" or state < new Date().getTime() - mintpresso.loadingInterval
          routes.controllers.Panel.overview_index(sessionStorage.id)
            .ajax()
            .success (e) ->
              console.log "zzz"
              $block.html e
              $block.addClass('active').fadeIn {
                duration: 300
                easing: 'easeOutQuint'
              }
        else
          $block.addClass('active').fadeIn {
            duration: 300
            easing: 'easeOutQuint'
          }
        true



      mintpresso.waitForLoading = false
    else if mintpresso.page is 'data'
    else if mintpresso.page is 'support'
    else
      alert("페이지를 불러올 수 없습니다. ")


