package scaladinext.ui

import vaadin.scala.mixins.ContainerIndexedMixin
import vaadin.scala.{BeanItem, Container, Wrapper}

import scala.reflect.ClassTag
import scaladinext.model.Id

trait BeanContainerMixin extends ContainerIndexedMixin { self: com.vaadin.data.util.BeanContainer[_,_] => }

class BeanContainer[ID, BT <: Id[ID]](override val p: com.vaadin.data.util.BeanContainer[ID, BT] with BeanContainerMixin)
  extends Wrapper with Container.Indexed {
  def addItemAt(index: Int, id: ID, bean: BT) = p.addItemAt(0, bean.id, bean)

  p.wrapper = this

  def this()(implicit m: ClassTag[BT]) = {
    this(new com.vaadin.data.util.BeanContainer[ID, BT](m.runtimeClass.asInstanceOf[Class[BT]]) with BeanContainerMixin)
    p.setBeanIdProperty("id")
  }

  def this(beans: Seq[BT])(implicit m: ClassTag[BT]) = {
    this()
    addAll(beans)
  }

  def addItem(id: ID, bean: BT) = p.addItem(id, bean)

  def save() = {
    // https://vaadin.com/forum#!/thread/306062

  }

  def addNestedContainerBean(propertyName: String) = p.addNestedContainerBean(propertyName)

  def addAll(beans: Seq[BT]) = {
    beans.map(b => addItem(b.id.asInstanceOf[ID], b))
  }

  override def getItem(itemId: Any): BeanItem[BT] =
    super.getItem(itemId).asInstanceOf[BeanItem[BT]]

  override def getItemOption(itemId: Any): Option[BeanItem[BT]] =
    super.getItemOption(itemId).map(_.asInstanceOf[BeanItem[BT]])

  def wrapItem(unwrapped: com.vaadin.data.Item): BeanItem[BT] = {
    // must create BeanItem with the constructor that takes a Vaadin BeanItem not a bean.
    new BeanItem[BT](unwrapped.asInstanceOf[com.vaadin.data.util.BeanItem[BT]])
  }
}
