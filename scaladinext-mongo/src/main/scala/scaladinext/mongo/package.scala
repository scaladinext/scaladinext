package scaladinext

import net.liftweb.record.{Field, MandatoryTypedField, OptionalTypedField, Record}

import scala.reflect.ClassTag

/**
  * Contains implicit conversion for this package.
  */
package object mongo {
  type MandatoryField[T, OwnerType <: Record[OwnerType]] = Field[T, OwnerType] with MandatoryTypedField[T]
  type OptionalField[T, OwnerType <: Record[OwnerType]] = Field[T, OwnerType] with OptionalTypedField[T]

  /**
    * A useful converter from Mongo Field like 'employee.name' to ScaladinProperty to be used in bindings of for example
    * a TextField to employee.name.
    *
    * @param field field of a Mongo Record to be bound to a Vaadin field
    * @tparam T
    * @tparam OwnerType
    * @return
    */
  implicit def mandatoryFieldToPropertyConversion[T: ClassTag, OwnerType <: Record[OwnerType]](field: MandatoryField[T, OwnerType]): MongoProperty[T, OwnerType] = MongoProperty(field)
//    (field: Field[T, OwnerType] with MandatoryTypedField[T]): MongoProperty[T, OwnerType] = MongoProperty(field)

  implicit def optionalFieldToPropertyConversion[T: ClassTag, OwnerType <: Record[OwnerType]](field: OptionalField[T, OwnerType]): MongoProperty[T, OwnerType] = MongoProperty(field)
//    (field: Field[T, OwnerType] with OptionalTypedField[T]): MongoProperty[T, OwnerType] = MongoProperty(field)
}