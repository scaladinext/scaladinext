package scaladinext.mongo


import com.vaadin.data.util.AbstractProperty
import net.liftweb.record.Record
import vaadin.scala._
import vaadin.scala.mixins.ScaladinMixin
import vaadin.scala.util.TypeMapper

import scala.reflect._

/**
 * This class is a work around scala's limitation calling a protected Java method from Scala implementation.
 *
 * @tparam T
 */
abstract class WithAccessor[T] extends AbstractProperty[T] {
  protected def myFireValueChange() = fireValueChange()
}

trait MongoDelegatingPropertyMixin[T, OwnerType <: Record[OwnerType]] extends WithAccessor[T] with ScaladinMixin {
  protected def mySetValue(newValue: T) = setValue(newValue)
  override def wrapper = super.wrapper.asInstanceOf[MongoProperty[T, OwnerType]]
  def getValue: T = wrapper.value.getOrElse(null).asInstanceOf[T]
  protected def setValue(newValue: T): Unit = {
    wrapper.value = newValue
    myFireValueChange()
  }
  def getType: Class[_ <: T] = wrapper.getType
  override def isReadOnly: Boolean = wrapper.readOnly
  override def setReadOnly(readOnly: Boolean) { wrapper.readOnly = readOnly }
}


trait MongoProperty[T, OwnerType <: Record[OwnerType]] extends Property[T, T] {
  private var _readOnly = false
  override def readOnly: Boolean = _readOnly
  override def readOnly_=(ro: Boolean) { _readOnly = ro }

  val p = new WithAccessor[T] with MongoDelegatingPropertyMixin[T, OwnerType]
  p.wrapper = this
}

class MongoMandatoryProperty[T: ClassTag, OwnerType <: Record[OwnerType]](field: MandatoryField[T, OwnerType])
  extends MongoProperty[T, OwnerType] {
  override def getType: Class[_ <: T] = {
    val clazz = classTag[T].runtimeClass
    TypeMapper.toJavaType(clazz).asInstanceOf[Class[_ <: T]]
  }
  override def value: Option[Any] = Option(field.value)
  override def value_=(value: Option[Any]) { this.value = value.orNull }
  override def value_=(value: Any) { field.set(value.asInstanceOf[T]) }
}

class MongoOptionalProperty[T: ClassTag, OwnerType <: Record[OwnerType]](field: OptionalField[T, OwnerType])
  extends MongoProperty[T, OwnerType] {
  override def getType: Class[_ <: T] = {
    val clazz = classTag[T].runtimeClass
    TypeMapper.toJavaType(clazz).asInstanceOf[Class[_ <: T]]
  }
  override def value: Option[Any] = field.value
  override def value_=(value: Option[Any]) { this.value = value.asInstanceOf[Option[T]] }
  override def value_=(value: Any) { field.set(Option(value).asInstanceOf[Option[T]]) }
}

object MongoProperty {
  def apply[T: ClassTag, OwnerType <: Record[OwnerType]](field: MandatoryField[T, OwnerType]): MongoProperty[T, OwnerType] =
    new MongoMandatoryProperty(field)

  def apply[T: ClassTag, OwnerType <: Record[OwnerType]](field: OptionalField[T, OwnerType]): MongoOptionalProperty[T, OwnerType] =
    new MongoOptionalProperty(field)
}