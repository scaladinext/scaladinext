package scaladinext

import com.vaadin.data.validator._
import vaadin.scala.Validator

package object validator {
  val positiveInt = Validator(new IntegerRangeValidator("Only positive integers are allowed", 0, Int.MaxValue))
  val notNull = Validator(new NullValidator("Please enter a value here", false))
}
