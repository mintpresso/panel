package models

import play.api.Play.current

case class User(id: Int, email: String, name: String)
// object User {
//   def apply(id: Int, email: String, name: String) = {
//   	new User(id, email, name)
//   }
// }