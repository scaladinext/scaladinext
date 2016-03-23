package scaladinext.event

import scaladinext.ui.UiWithBus
import vaadin.scala.{Item, UI}

case class SelectedItemEvent(item: Item)

trait AppEvents {
  bus.register(this)

  def postEvent(event: AnyRef) = bus.post(event)
  def unregister(`object`: AnyRef) { bus.unregister(`object`) }

  def bus = UI.current.asInstanceOf[UiWithBus].appBus
}
