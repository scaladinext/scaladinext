package scaladinext.mongo

import com.vaadin.data.util.{PropertysetItem => VaadinPropertysetItem}
import vaadin.scala.PropertysetItem

import scala.reflect.runtime.universe._

class MongoItem[T: TypeTag](record: T, propertyDescriptors: Iterable[MongoPropertyDescriptor[T]])
  extends PropertysetItem(new VaadinPropertysetItem)


