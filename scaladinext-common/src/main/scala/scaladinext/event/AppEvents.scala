package scaladinext.event

import scaladinext.ui.UiWithBus
import vaadin.scala.UI

trait AppEvents {
  bus.register(this)

  def postEvent(event: AnyRef) = bus.post(event)
  def unregister(`object`: AnyRef) { bus.unregister(`object`) }

  def bus = UI.current.asInstanceOf[UiWithBus].appBus
}
