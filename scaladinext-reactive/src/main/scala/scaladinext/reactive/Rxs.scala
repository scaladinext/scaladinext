package scaladinext.reactive

import rx._
import rx.core.Obs
import vaadin.scala.event.ValueChangeNotifier
import vaadin.scala.{Property, PropertyViewer}

/**
 * Mixin that has a couple of reactive variables: Rx and Var. To use this
 *
 * @tparam T type of the underlying object that is being hold/updated.
 */
trait Rxs[T] extends PropertyViewer {
  /**
   * Trait can be mixed to any object that has implemented a 'PropertyViewer' trait.
   */
  self: PropertyViewer with ValueChangeNotifier =>

  protected var _rx: Rx[Option[T]] = _
  protected var _obsRx:Obs = _
  protected var _var: Var[Option[T]] = _

  /**
   * Initializes a new Rx reactive recalculating variable for this given object. After initialization, internally it
   * creates an Obs[erver] that is being called when propery value is changed.
   *
   * @param newRx a new Rx variable.
   */
  def rx_=(newRx: Rx[Option[T]]) = {
    _rx = newRx
    _obsRx = Obs(_rx) {
      property.foreach { p => if (p.value != rx()) p.value = rx() }
    }
  }

  /**
   * Initializes a new Rx reactive recalculating variable for this given object. After initialization, internally it
   * creates an Obs[erver] that is being called when propery value is changed.
   *
   * @param newRx a new Rx variable.
   */
  def rx(newRx: Rx[Option[T]]): this.type = {
    rx = newRx
    this
  }

  /**
   * Getter for a 'rx' variable.
   *
   * @return
   */
  def rx: Rx[Option[T]] = _rx

  /**
   * A syntactic sugar method used to update a new variable
   *
   * @param newVal
   */
  def update(newVal: Option[T]) = { _var() = newVal }

  /**
   * Another syntactic sugar method to get a value of a reactive variable.
   *
   * @return a content of the reactive variable
   */
  def apply() = _var()

  /**
   * Builds an instance of new reactive Var variable and adds valueChange listeners so that the variable is updated
   * when a property is changed.
   *
   * @return a new instance of initialized variable.
   */
  protected def buildVar(): Var[Option[T]] = {
    val res = Var(property.flatMap(_.value.asInstanceOf[Option[T]]))
    valueChangeListeners += { e =>
      val propVal = property.flatMap(_.value).asInstanceOf[Option[T]]
      val resVal = res()
//      if (resVal != propVal)
        res() = propVal //

//      val resVal2 = res()
//      logger.debug(s"valueChangeListeners: ${caption.getOrElse("")}.propVal = $propVal, before = $resVal, after = $resVal2")

    }
    res
  }

  override def property_=(prop: Option[Property[_, _]]): Unit = {
    super.property = prop
    _var = buildVar()
  }

  override def property_=(prop: Property[_, _]): Unit = {
    super.property = prop
    _var = buildVar()
  }
}
