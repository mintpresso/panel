package com.mintpresso.i18n

import play.api.i18n.{Lang, Messages}

object Locale {
  def getDateString(timestamp: Long)(implicit lang: Lang): String = {
    val f = new java.text.SimpleDateFormat( Messages("date.fullstring")(lang) )
    return f.format( new java.sql.Timestamp(timestamp) )
  }
}