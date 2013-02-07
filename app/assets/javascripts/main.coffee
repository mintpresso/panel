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

  onBlock = ($block) ->
    state = $block.data('state')
    if state is 0
      $block.data 'state', Math.round(Date.now()/1000)

    $block.addClass('active').fadeIn {
        duration: 300
        easing: 'easeOutQuint'
      }

  offContent = ($content) ->
    $content.find('.active').fadeOut {
      duration: 300
      easing: 'easeOutQuint'
    }

  onMenu = ($submenu, $menu) ->
    $submenu.find('li:not(.reactive)').removeClass('active')
    $menu.addClass('active')
    document.location.hash = '!/' + $menu.data 'menu'

  triggerContent = ($content, $submenu, $menu, callback) ->
    $block = $content.find("[data-content=#{ $menu.data('menu') }]")
    if $menu.is('.active')
      return true

    if $menu.is('.reactive')
      $menu.removeClass('reactive')
    else
      offContent $content

    onMenu $submenu, $menu

    state = $block.data('state')
    if state is 0 or state < Math.round(Date.now()/1000) - mintpresso.loadingInterval
      callback $block
    else
      onBlock $block
    true

  refreshContent = ($content, $submenu, block, callback) ->
    $block = $content.find("[data-content=#{ block }]")
    $block.data('state', 0)
    el = $submenu.find "[data-menu=#{ block }]"
    if el.length > 0
      el.removeClass 'active'
      el.addClass 'reactive'
      el.trigger 'click'
      callback $block
    else
      console.log "Invalid data-menu=? at method refreshContent"

  triggerIndex = ($content, callback) ->
    $block = $content.find('[data-content=index]')
    offContent $content
    $submenu.find('li').removeClass('active')

    state = $block.data('state')
    if state is 0 or state < Math.round(Date.now()/1000) - mintpresso.loadingInterval
      callback $block
    else
      onBlock $block
    true

  triggerHash = ($content, $submenu) ->
    path = document.location.pathname
    hash = document.location.hash
    menu = $('#menu li.active').data('menu')

    if hash is undefined or hash.length is 0
      triggerIndex $content, ($block) ->
        routes.controllers.Panel[menu + '_index'](sessionStorage.id)
          .ajax()
          .success (e) ->
            $block.html e
            onBlock $block
    else if path.charAt(path.length - 1) is "/"
      el = $submenu.find "[data-menu=#{ hash.substr(3) }]"
      if el.length > 0
        el.trigger 'click'
      else
        console.log "Invalid data-menu=? at method triggerHash"
        document.location.hash = '!/index'
        triggerIndex $content, ($block) ->
          routes.controllers.Panel[menu + '_index'](sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block
    else
      false


  $meta = $('meta[name=panel]')
  if $meta.length > 0 and $meta isnt undefined
    mintpresso.page = $meta[0].getAttribute('content')

    if mintpresso.page is 'overview'
      $submenu = $("#submenu")
      $content = $("#content")

      $submenu.find('[data-menu=usage]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.overview_usage(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block

      $submenu.find('[data-menu=account]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.overview_account(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block

      $submenu.find('[data-menu=transaction]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.overview_transaction(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block

      $submenu.find('[data-menu=api]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.overview_api(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              $block.find('div.well span').html mintpresso._api.token
              $block.find('textarea').html mintpresso._api.urls.join '\n'
              onBlock $block

      triggerHash $content, $submenu
      mintpresso.waitForLoading = false
    else if mintpresso.page is 'data'
      $submenu = $("#submenu")
      $content = $("#content")

      $submenu.find('[data-menu=view]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.data_view(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              $block.find('input[name=s]').typeahead { source: mintpresso._types }
              $block.find('input[name=o]').typeahead { source: mintpresso._types }

              $block.find('#selector div.custom-filter a.add').tooltip {title: "Add new filter"}

              $tbody = $block.find('table tbody')
              for p in mintpresso._points.points
                p = p.point
                d1 = moment(p.createdAt).format('YYYY-MM-DD HH:mm:ss')
                $tbody.prepend """
                  <tr>
                    <td><a href="#{p._url}">#{p.id}</a></td>
                    <td>#{p.type}</td>
                    <td>#{p.identifier}</td>
                    <td>#{JSON.stringify(p.data)}</td>
                    <td>
                      <time datetime="#{d1}" class="timeago">#{d1}</time>
                    </td>
                  </tr>
                  """

              $block.find('time.timeago').timeago()
              onBlock $block

      $submenu.find('[data-menu=filter]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.data_filter(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block

      $submenu.find('[data-menu=import]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.data_import(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block
              $block.find('input[name=model]').typeahead { source: mintpresso._sTypes }
              $form = $block.find('form#model')
              $form.submit () ->
                offContent $content
                args =
                  data: {
                    model: $form.find('input[name=model]').val()
                    identifier: $form.find('input[name=identifier]').val()
                    data: $form.find('textarea[name=data]').val()
                  }
                  success: (e) ->
                    refreshContent $content, $submenu, 'import', ($block) ->
                      true
                      #onBlock $block
                      #$form.find('span.help-block').html e
                      

                routes.controllers.Panel.data_import_add(sessionStorage.id).ajax args
                return false
              $block.find('time.timeago').timeago()

      $submenu.find('[data-menu=export]').click (e) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel.data_export(sessionStorage.id)
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block

      triggerHash $content, $submenu
      mintpresso.waitForLoading = false
      mintpresso.triggerContentWith = (name, json) ->
        triggerContent $content, $submenu, $(this), ($block) ->
          routes.controllers.Panel['data_' + name](sessionStorage.id, JSON.stringify(json))
            .ajax()
            .success (e) ->
              $block.html e
              onBlock $block
    else if mintpresso.page is 'support'
    else
      alert("페이지를 불러올 수 없습니다. ")


