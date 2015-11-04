package scaladinext.mongo

import com.vaadin.data.util.VaadinPropertyDescriptor
import vaadin.scala.Wrapper
import vaadin.scala.mixins.DelegatingVaadinPropertyDescriptor

class MongoPropertyDescriptor[T](val name: String, val propertyType: Class[_]) extends Wrapper {

  val p = new VaadinPropertyDescriptor[T] with DelegatingVaadinPropertyDescriptor[T]
  p.wrapper = this

//  def createProperty[T](record: T): Property[_, _] = {
//    new MongoProperty[T](propertyType.asInstanceOf[Class[_ <: T]], record)
//  }

}
