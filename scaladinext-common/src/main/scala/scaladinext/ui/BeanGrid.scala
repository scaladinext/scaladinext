package scaladinext.ui

import com.typesafe.scalalogging.LazyLogging
import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent
import vaadin.scala._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scaladinext.model.Id

abstract class BeanGrid[IDT, BT <: Id[IDT]](implicit m: ClassTag[BT]) extends Grid with LazyLogging {
  self =>
  val beanContainer = new BeanContainer[IDT, BT]()
  container = beanContainer

  // BeanFieldGroup is bound to the container so when a container is reset, the binding get lost
  // TODO: override set container method
  val group = new BeanFieldGroup[BT](m.runtimeClass.asInstanceOf[Class[BT]])
  p.setEditorFieldGroup(group)

  p.getEditorFieldGroup.addCommitHandler(new com.vaadin.data.fieldgroup.FieldGroup.CommitHandler {
    override def preCommit(commitEvent: CommitEvent): Unit = {}

    override def postCommit(commitEvent: CommitEvent): Unit = {
      val fieldGroup = commitEvent.getFieldBinder.asInstanceOf[BeanFieldGroup[BT]]
      val bean = fieldGroup.getItemDataSource.getBean
      saveToDb(bean)
    }
  })

  def setData(newData: Future[List[BT]]): Unit = {
    val result = Await.result(newData, 10 seconds)
    setData(result)
  }

  def setData(newData: List[BT]): Unit = {
    beanContainer.removeAllItems()
    newData.map(emp => beanContainer.addItem(emp.id, emp))
//    beanContainer
  }

  def addNewItem(bean: BT) = beanContainer.addItemAt(0, bean.id, bean)

  def addNew(): Unit = {
    val newItem = addNewItem(buildNewBean())
    scrollToStart()
    editItem(newItem.getBean.id)
  }

  def addNestedContainerBean(propertyName: String) = beanContainer.addNestedContainerBean(propertyName)

  def buildNewBean(): BT

  def saveToDb(bean: BT): Unit
}
