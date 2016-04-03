package scaladinext.ui

import com.typesafe.scalalogging.LazyLogging
import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent
import vaadin.scala._

import com.vaadin.ui.{ Grid => VaadinGrid }
import vaadin.scala.mixins.GridMixin

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scaladinext.model._


trait CancelEditorHandler {
  def onCancelEditor(): Unit
}

class MyVaadinGrid(dataSource: com.vaadin.data.Container.Indexed) extends VaadinGrid(dataSource) with LazyLogging {

  var cancelEditorHandler: CancelEditorHandler = _

  def setCancelEditorHandler(handler: CancelEditorHandler): Unit = {
    cancelEditorHandler = handler
  }

  override def doCancelEditor(): Unit = {
    cancelEditorHandler.onCancelEditor()
    super.doCancelEditor()
  }
}

abstract class BeanGrid[IDT, BT <: Id[IDT] with NewRecord](implicit m: ClassTag[BT])
  extends Grid(new MyVaadinGrid(new IndexedContainer().p) with GridMixin) with CancelEditorHandler with LazyLogging {

  val beanContainer = new BeanContainer[IDT, BT]()
  container = beanContainer

  // about cancelling editor
  // https://vaadin.com/forum/#!/thread/9005870/9005869
  // we need to bind this, otherwise an NPE occures
  p.asInstanceOf[MyVaadinGrid].cancelEditorHandler = this

  // BeanFieldGroup is bound to the container so when a container is reset, the binding get lost
  // TODO: override set container method
  val group = new BeanFieldGroup[BT](m.runtimeClass.asInstanceOf[Class[BT]]) { bfg =>
    addCommitHandler(new com.vaadin.data.fieldgroup.FieldGroup.CommitHandler {
      override def preCommit(commitEvent: CommitEvent): Unit = {}

      override def postCommit(commitEvent: CommitEvent): Unit = {
        val fieldGroup = commitEvent.getFieldBinder.asInstanceOf[BeanFieldGroup[BT]]
        val bean = fieldGroup.getItemDataSource.getBean
        saveToDb(bean)
      }
    })
  }
  p.setEditorFieldGroup(group)

  /**
    * This method is called when editing a bean in a table and clicking 'Cancel' button. For a newly created record
    * it means that we need to remove it from the grid.
    */
  override def onCancelEditor() = {
    editedItemId match {
      case Some(id) =>
        val item = beanContainer.getItem(id)
        if (item == null || item.bean == null || item.bean.newRecord) beanContainer.removeItem(id)
      case None => {}
    }
  }

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

