package scaladinext.mongo

import net.liftweb.mongodb.record.MongoRecord
import vaadin.scala.{IndexedContainer, Item}

/**
 * Represents a container for the classes that have Id,Name and ParentId properties defined
 *
 * @tparam T type that limits the applicability of the container, this type should have Id,Name and ParentId properties.
 */
class MongoContainer[T <: MongoRecord[T]] extends IndexedContainer {
  var buildCaptionImpl: Option[(T) => String] = None

  def this(elems: Seq[T], buildCaptionParam: Option[(T) => String] = None) = {
    this()
    buildCaptionImpl = buildCaptionParam
    addAllItems(elems)
  }

  /**
   * Adds an object to the container.
   *
   * @param elem element to add to the container
   * @return a scaladin container item
   */
  def addItem(elem: T): Option[Item] = {
    addContainerProperty("name", classOf[String])
    val res = addItem(elem.id)

    getItem(elem.id).getProperty("name").value = buildCaption(elem)


//    setParent(elem) // if we are lucky and the parent element is already added to the container
    res
  }

  def buildCaption(elem: T): String = {
    if (buildCaptionImpl.isDefined) {
      buildCaptionImpl.get.apply(elem)
    } else {
      val field = elem.fieldByName("name")
      field.getOrElse("No 'name' field").asInstanceOf[String]
    }

  }

  /**
   * Sets parent element
   *
   * @param elem
   * @return
   */
//  def setParent(elem: T): Unit = elem.fieldByName("parentId").map { field => // TODO: make it type safe
//    setParent(elem.id, field.get)
//  }

  /**
   * Sets a childrenAllowed flag to false if a give element does not have any children.
   *
   * @param elem
   * @return
   */
//  def initChildrenAllowed(elem: T) = getItemOption(elem.id).foreach { _ => if (!hasChildren(elem.id)) setChildrenAllowed(elem.id, false) }

  /**
   * Adds a full list of elements to the container and initialized parent
   *
   * @param elems
   * @return
   */
  def addAllItems(elems: Seq[T]) = {
    elems.map { addItem }
//    elems.map { setParent }
//    elems.map { initChildrenAllowed }
  }
}

