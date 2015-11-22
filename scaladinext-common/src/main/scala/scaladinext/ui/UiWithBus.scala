package scaladinext.ui

import vaadin.scala.UI
import scaladinext.event.AppBus

trait UiWithBus { self => UI
  val appBus = new AppBus
}
