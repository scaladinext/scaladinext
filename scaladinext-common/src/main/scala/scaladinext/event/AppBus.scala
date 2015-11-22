package scaladinext.event

import com.google.common.eventbus.{EventBus => GoogleEventBus, SubscriberExceptionContext, SubscriberExceptionHandler}
import com.typesafe.scalalogging.LazyLogging

class AppBus extends SubscriberExceptionHandler with LazyLogging {
  private val eventBus = new GoogleEventBus(this)

  def post(event: AnyRef) { eventBus.post(event) }
  def register(`object`: AnyRef) { eventBus.register(`object`) }
  def unregister(`object`: AnyRef) { eventBus.unregister(`object`) }

  override def handleException(exception: Throwable, context: SubscriberExceptionContext): Unit = {
    logger.error(exception.getMessage + " context: " + context, exception)
//    UI.current.asInstanceOf[YOUR_UI].showError(exception)
  }
}

object AppBus extends AppBus

