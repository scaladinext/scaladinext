package scaladinext.mongo

import net.liftweb.record.Record

/**
 * Macro for creating a column for the grid
 */
object column {
  import net.liftweb.record.Field

  import scala.language.experimental.macros
//  import scala.reflect._
  import scala.reflect.macros._
//  import scala.reflect.runtime.universe._
//  import org.d5i.traumweb.view.components.mongo.MongoGrid.Column

  def apply[T, RT <: Record[RT]](field: Field[T,RT]): MongoGrid.Column[T, RT] = macro impl1[T, RT]
  def apply[T, RT <: Record[RT]](field: Field[T,RT], caption: String): MongoGrid.Column[T, RT] = macro impl2[T, RT]

  def impl1[T, RT <: Record[RT]](c: whitebox.Context)(field: c.Expr[Field[T,RT]]): c.Expr[MongoGrid.Column[T, RT]] = {
    import c.universe._

    val fieldStr = showCode(q"$field")
    val code = columnCode(fieldStr)
    val res = q"scaladinext.mongo.MongoGrid.Column($field, $code)"
    c.Expr(res)
  }

  def impl2[T, RT <: Record[RT]](c: whitebox.Context)(field: c.Expr[Field[T,RT]], caption:c.Expr[String]): c.Expr[MongoGrid.Column[T, RT]] = {
    import c.universe._

    val fieldStr = showCode(q"$field")
    val code = columnCode(fieldStr)
    val res = q"scaladinext.mongo.MongoGrid.Column($field, $code, Some($caption))"
    c.Expr(res)
  }

  // "org.d5i.traumweb.service.dao.TestEmployee.rec.firstName" -> "firstName"
  // "org.d5i.traumweb.service.dao.TestEmployee.rec.name.value.first" -> "name.first"
  // MongoGridSpec.this.employee.firstName  -> "firstName"
  // MongoGridSpec.this.employee.name.value.first.value.code -> "name.first.code"
  // $line16.$read.$iw.$iw.$iw.$iw.employee.firstName -> "firstName"
  def columnCode(full: String): String = {
    val list = full.split('.')
    if (!full.contains("value")) list.last // simple property, no subfields in it
    else {
      val i = list.indexOf("value")-1 // a word before 'value' is needed
      val codesAndValues = list.splitAt(i)._2 // the second part - tail is the code array
      val codes = codesAndValues.filterNot(_ == "value")
      codes.mkString(".")
    }
  }
}

