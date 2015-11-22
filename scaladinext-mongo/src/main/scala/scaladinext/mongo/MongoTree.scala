package scaladinext.mongo

import net.liftweb.mongodb.record.MongoRecord
import org.bson.types.ObjectId
import vaadin.scala._

import scaladinext.event._

trait MongoTree[T <: MongoRecord[T]] extends AppEvents {
  self: Tree =>

  var mongoData: List[T] = List()

  valueChangeListeners += { event => // by keyboard or mice
    if (event.property != null && event.property.value.isDefined) {
      val id = event.property.value.get.toString
      postEvent(buildEvent(id))
    }
  }

  /**
   * Sets new data for given tree.
   *
   * @param newData
   */
  def update(newData: List[T]) = {
    mongoData = newData
    container = new MongoHierarchContainer[T](mongoData, Some(buildCaption))
    itemCaptionPropertyId = "name"
    expandAll()
  }

  /**
   * Selects a record with given id or a first topmost one.
   *
   * @param id id of the recrod to select.
   */
  def selectGivenOrTopmost(id: ObjectId): ObjectId = {
    val toSelectId = if (mongoData.map(_.id.asInstanceOf[ObjectId]).contains(id)) id else topItemIds().head
    select(toSelectId)
    toSelectId.asInstanceOf[ObjectId]
  }

  def containsObjectId(id: ObjectId): Boolean = mongoData.map(_.id.asInstanceOf[ObjectId]).contains(id)

  def buildCaption(record: T): String = {
    val field = record.fieldByName("name")
    if (field.isDefined) field.openOrThrowException("No field 'name' found").get.toString
    else "No 'name' field"
  }

  def recordById(id: String): T = mongoData.find(_.id.toString == id).get

  def expandAll() = rootItemIds.foreach { expandItemsRecursively }

  def topItemIds() = rootItemIds.map(_.asInstanceOf[ObjectId])

  def buildEvent(id: String): AnyRef


}
