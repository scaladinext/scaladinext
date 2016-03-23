package scaladinext.mongo

import com.typesafe.scalalogging.LazyLogging
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.record._
import vaadin.scala.event.{ItemClickEvent, SelectionEvent}
import vaadin.scala.{Item, Grid, IndexedContainer}

import scala.reflect.runtime.universe._
import scaladinext.event.{SelectedItemEvent, AppBus}

object MongoGrid extends LazyLogging { self =>

  /**
   * Represents a column in a table container. Internal class. Use a macro column() instead.
   *
   * @param field Mongo field
   * @param captionParam caption of the column
   * @tparam T underlying scala type, like String, Double, etc
   * @tparam RT RT - Record Type - the type of the record, like Employee, etc.
   */
  case class Column[T, RT <: Record[RT]](field: Field[T, RT], code: String, captionParam: Option[String] = None)(implicit tag: TypeTag[T])
      extends LazyLogging {

    def scalaFieldClass = {
      val classSymbol = tag.tpe.typeSymbol.asClass
      val runtime = runtimeMirror(self.getClass.getClassLoader)
      runtime.runtimeClass(classSymbol)
    }

    def caption = captionParam match {
      case Some(s) => s
      case None => field.displayName
    }
  }

  /**
   * Represents a mongo mix to the standard scaladin grid. It builds and initializes columns, headers
   * and data automatically based on the list of columns, represented by MongorRecord fields.
   *
   * Example Usage:
   *
   * <pre>
   *   class EmplMongoGrid extends Grid with MongoGrid.Mix[TestEmployee] {
   *     width = 350 px;
   *     heightMode = Grid.HeightMode.Row
   *     heightByRows = container.size
   *
   *     override def columnDefs =
   *        column(TestEmployee.rec.pin, "PIN") ::
   *        column(TestEmployee.rec.lastName) ::
   *        column(TestEmployee.rec.firstName) ::
   *        column(TestEmployee.rec.name.value.first) ::
   *        column(TestEmployee.rec.salary) ::
   *        column(TestEmployee.rec.salaryCurrency) :: Nil
   *
   *     override def records = TestEmployee.testEmps
   *   }
   * </pre>
   *
   * @tparam T type of the MongoRecord, like Employee or Department.
   */
  trait Mix[T <: MongoRecord[T]] { self: Grid =>
    val dataSource = buildContainer(columnDefs)
    container = dataSource

    // this can happen later
    dataSource.load(columnDefs, loadRecords())

    setupCaptions(this, columnDefs)

    selectionListeners += { e: SelectionEvent => AppBus.post(SelectedItemEvent(selectedItem.get)) }
    itemClickListeners += { e: ItemClickEvent => {}}

    def selectedItem: Option[Item] = {
      val selectedId = self.selectedRow
      //    logger.debug("selectedId: " + selectedId)
      val item = selectedId match {
        case Some(id) =>
          val i = container.getItem(id)
          val values = i.propertyIds.map { propId => i.getProperty(propId).value}
          // logger.debug("vals: " + values)
          Some(i)
        case None =>
          if (container.size > 0) Some(container.getItem(container.getIdByIndex(0)))
          else None
      }

      item.foreach(i => {
        //      logger.debug("!!!! itemExist: " + i)
      })

      //    logger.debug("selectedItem: " + item)
      item
    }


    /**
     * Column definitions. Based on the Column.field object,
     *
     * @return
     */
    def columnDefs: List[Column[_, _]]

    /**
     * a fuction that load data
     *
     * @return
     */
    def loadRecords: () => List[T]
  }

  /**
   * Creates a new instance of IndexedContainer
   *
   * @param columns
   * @return
   */
  def buildContainer(columns: List[Column[_, _]]) = {
    val cont = new IndexedContainer

    columns.foreach{ column =>
      val (name, clazz) = (column.code, javaClassOf(column.scalaFieldClass))
      if (!cont.addContainerProperty(name, clazz))
        logger.error("cannot add container property (name, class): " + (name, clazz))
    }

    cont
  }



  /**
   * A specific converter method for Java Vaadin implementation, as the Grid accepts only java.lang.Double, for example,
   * not scala.Double types.
   *
   * Temporary.
   *
   * @param scalaClass
   * @return
   */
  def javaClassOf(scalaClass: Class[_]): Class[_] = scalaClass match {
    case c if c.isAssignableFrom(classOf[Double]) => classOf[java.lang.Double]
    case c if c.isAssignableFrom(classOf[Short]) => classOf[java.lang.Short]
    case c if c.isAssignableFrom(classOf[Int]) => classOf[java.lang.Integer]
    case c if c.isAssignableFrom(classOf[Long]) => classOf[java.lang.Long]
//    case c if c.isAssignableFrom(classOf[BigDecimal]) => classOf[java.math.BigDecimal]
//    case c if c.isAssignableFrom(classOf[org.joda.time.DateTime]) => classOf[java.util.Date]
    case _ => scalaClass
  }

  /**
   * Setups header captions on a given grid.
   *
   * @param grid grid, where the captions needs to be set up.
   * @param columns columns meta information for the grid.
   */
  def setupCaptions(grid: Grid, columns: List[Column[_, _]]) = columns.foreach { column =>
    setCaption(grid, column.code, column.caption)
  }

  /**
   * Setup header caption for on the grid for a particular column name.
   *
   * @param grid grid, where the captions needs to be set up.
   * @param columnName the coded name of the column.
   * @param caption the header caption that needs to be applied to the given column
   */
  def setCaption(grid: Grid, columnName: String, caption: String) = {
//    logger.debug("setCaption.columnName = " + columnName)
    grid.getColumn(columnName).get.headerCaption = caption
  }

  /**
   * Additional method for the IndexedContainer to provide a 'load' method for it.
   *
   * @param container a container where we need to load the data
   */
  implicit class ContainerWithLoad(container: IndexedContainer) {
    def load[T <: MongoRecord[T]](columns: List[Column[_, _]], data: List[T]) = {
      addAllItemsToContainer(container, columns, data)
      container
    }

  }
/*
  def subfieldValue[T <: BsonRecord[T]](record: T, names: Array[String]) = {
    // "name.first"
    val head = names.head
    // emp.fieldByName("name").get.valueBox.get
    // res36: Any = class org.d5i.traumweb.service.dao.EmployeeName={first=Valery2, last=Veselov}

    // emp.fieldByName("name").get.valueBox.get.asInstanceOf[BsonRecord[_]]
    // net.liftweb.mongodb.record.BsonRecord[_] = class org.d5i.traumweb.service.dao.EmployeeName={first=Valery2, last=Veselov}

    // emp.fieldByName("name").get.valueBox.get.asInstanceOf[BsonRecord[_]].fieldByName("first").get.valueBox.get
    // warning: there were four deprecation warnings; re-run with -deprecation for details
    // res53: Any = Valery2


    val newRec = record.fieldByName(head).get.valueBox.get

    val runtime = runtimeMirror(getClass.getClassLoader)
    val instanceMirror = runtime.reflect(newRec) // instanceMirror.symbol.typeSignature.declarations

    val mod = instanceMirror.symbol.typeSignature.member(newTermName("first")).asModule // error: object first is not a method
    val mfirst = instanceMirror.reflectModule(mod)
    val value: Any = mfirst.instance // is a value of type Any
  }
*/

  /*
  def fullCode(field: Field[_, _]): String = {
    buildColumnCodeFunc(field).mkString(".")
  }

  def buildColumnCodeFunc(field: Field[_, _]): List[String] = {
    val name = field.name

    if (!isSubfield(field))
      name :: Nil
    else {
      val ownerName = buildColumnCodeFunc(field.owner.asInstanceOf[Field[_,_]])
      ownerName ++ List(name)
    }
  }
  */

  def isSubfield(field: Field[_, _]): Boolean = !field.owner.isInstanceOf[MongoRecord[_]]

  def objectValue(record: Option[Any], fieldName: String): Option[Any] = {
//    logger.debug("objectValue" + snap(record, fieldName))

    import scala.reflect.runtime.universe._

    record match {
      case None => None
      case Some(rec) =>
        val runtime = runtimeMirror(self.getClass.getClassLoader)
        val instanceMirror = runtime.reflect(rec)

        val mModule = instanceMirror.symbol.typeSignature.member(newTermName(fieldName)).asModule // error: object first is not a method
        val mObject = instanceMirror.reflectModule(mModule)
        val value: Any = mObject.instance.asInstanceOf[Field[_,_]].valueBox.getOrElse(None) // is a value of type Any

        value match {
          case None => None
          case _ => Some(value)
        }
    }

  }

  def subfieldValue(record: Option[Any], names: List[String]): Option[Any] = {
//    logger.debug(snap(record, names).toString)
    val head = names.head
    val last = names.last

    if (head == last)
      objectValue(record, head)
    else {
      val newRecord = objectValue(record, head)
//      logger.debug(snap(newRecord).toString())
      subfieldValue(newRecord, names.tail)
    }
  }

  def fieldValue[T <: MongoRecord[T]](record: T, columnCode: String): Option[Any] = {
    subfieldValue(Some(record), splitSubfield(columnCode))
  }

  def addItemToContainer[T <: MongoRecord[T]](container: IndexedContainer, columns: List[Column[_, _]], record: T, idx: Int) = {
    val item = container.addItem(record.id).get

    columns.foreach { column =>
      fieldValue(record, column.code).foreach { item.getProperty(column.code).value = _ }
    }
  }

  def isSubfield(code: String): Boolean = splitSubfield(code).size > 1
  def splitSubfield(code: String): List[String] = code.split('.').toList

  private def addAllItemsToContainer[T <: MongoRecord[T]](container: IndexedContainer, columns: List[Column[_, _]], elems: List[T]) =
    elems.zipWithIndex.foreach { case (elem, idx) => addItemToContainer(container, columns, elem, idx+1) }

}


/**
* Macro for creating a column for the grid
*/
//object column {
//  import net.liftweb.record.Field
//  import scala.language.experimental.macros
//  import scala.reflect._
//  import scala.reflect.macros._
//  import scala.reflect.runtime.universe._
//  import org.d5i.traumweb.view.components.mongo.MongoGrid.Column
//
//  def apply[T, RT <: Record[RT]](field: Field[T,RT]): MongoGrid.Column[T, RT] = macro impl[T, RT]
//
//  def impl[T, RT <: Record[RT]](c: whitebox.Context)(field: c.Expr[Field[T,RT]]): c.Expr[MongoGrid.Column[T, RT]] = {
//    import c.universe._
//
//    val fieldStr = showCode(q"$field")
//    val code = columnCode(fieldStr)
//    val res = q"org.d5i.traumweb.view.components.mongo.MongoGrid.Column($field, Some($code))"
//    c.Expr(res)
//  }
//
//  // "org.d5i.traumweb.service.dao.TestEmployee.rec.firstName" -> "firstName"
//  // "org.d5i.traumweb.service.dao.TestEmployee.rec.name.value.first" -> "name.first"
//  // MongoGridSpec.this.employee.firstName  -> "firstName"
//  // MongoGridSpec.this.employee.name.value.first.value.code -> "name.first.code"
//  // $line16.$read.$iw.$iw.$iw.$iw.employee.firstName -> "firstName"
//  def columnCode(full: String): String = {
//    val list = full.split('.')
//    if (!full.contains("value")) list.last // simple property, no subfields in it
//    else {
//      val i = list.indexOf("value")-1 // a word before 'value' is needed
//      val codesAndValues = list.splitAt(i)._2 // the second part - tail is the code array
//      val codes = codesAndValues.filterNot(_ == "value")
//      codes.mkString(".")
//    }
//  }
//}

/*
Scala console imports for debuggin macro

val universe: scala.reflect.runtime.universe.type = scala.reflect.runtime.universe
import universe._
import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox
val toolbox = currentMirror.mkToolBox()


import _root_.org.d5i.traumweb.service.dao.TestEmployee
import _root_.org.d5i.traumweb.view.components.column

column(TestEmployee.rec.firstName)
*/


