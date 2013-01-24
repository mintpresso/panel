###
  Author: Jinhyuk Lee
###

jQuery ->
  $body = $('body')

  event =
    afterLoad: () ->
      console.log "AA", mintpresso.doneLoading, mintpresso.waitForLoading
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
      # event.waitForLoading = true
      # a = () ->
      #   event.waitForLoading = false
      # setTimeout a, 2000
      $(window).load (e) ->
        mintpresso.doneLoading = true
        event.afterLoad()
        

###
event =
    afterLoad: () ->
      if event.doneLoading is true
        logo = $('#animation-mask #logo')
        if event.waitForLoading is true
          if logo.is(':visible')
            logo.delay(1000).fadeOut {
              duration: 1000
              easing: 'easeOutQuint'
              complete: (e) ->
                event.afterLoad()
                true
              }
          else
            setTimeout event.afterLoad, 500
            console.log ">", event.waitForLoading, event.doneLoading
          return false
        else
          logo.delay(200).fadeOut {
            duration: 500
            easing: 'easeOutQuint'
            complete: (e) ->
              $('#single-page div.modal').animate {marginTop: '15.5px'}, 1000, 'easeOutQuint'
              $('#animation-mask').fadeOut {
                duration: 1000
                easing: 'easeOutQuint'
              }
              true
            }
      true
    waitForLoading: false
    doneLoading: true

  $meta = $('meta[name=animation]')
  if $meta.length > 0 and $meta isnt undefined
    content = $meta[0].getAttribute('content')
    if content is "fadein"
      color = if $('#single-page').is('.white') then 'white' else 'black'
      $body.append """
        <div id="animation-mask" class="#{color}">
          <div id="logo"></div>
        </div>
      """
      $('#single-page').show()
      $(window).load (e) ->
        event.afterLoad()
###