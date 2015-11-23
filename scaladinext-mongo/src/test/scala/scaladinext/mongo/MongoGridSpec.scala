package scaladinext.mongo

import com.typesafe.scalalogging._
import net.liftweb.mongodb.record.field.{BsonRecordField, ObjectIdPk}
import net.liftweb.mongodb.record.{BsonMetaRecord, BsonRecord, MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.joda.JodaTimeField
import net.liftweb.record.field.{DecimalField, DoubleField, LongField, StringField}
import org.joda.time.DateTime
import org.scalactic.Snapshots._
import org.scalatest._
import vaadin.scala.IndexedContainer

import scaladinext.mongo.MongoGrid._

class MongoGridSpec extends FlatSpec with Matchers with LazyLogging {

  val employee = TestEmployee.createRecord.
    pin(1231).customerId("o_vpetr").firstName("Valery").lastName("Petrov").hired(DateTime.now).
    name(EmployeeName.createRecord.first("Valery2").last("Veselov"))

  "MongoGrid object" should "have a function 'javaClassOf(class)' working" in {
    javaClassOf(classOf[Short])   should be (classOf[java.lang.Short])
    javaClassOf(classOf[Int])     should be (classOf[java.lang.Integer])
    javaClassOf(classOf[Long])    should be (classOf[java.lang.Long])
    javaClassOf(classOf[Double])  should be (classOf[java.lang.Double])
    javaClassOf(classOf[String])  should be (classOf[java.lang.String])
  }

  it should "be able to build a container" in {
    val emp = TestEmployee.createRecord

    val columns = column(emp.lastName) ::
      column(emp.firstName) ::
      column(emp.name.value.first) ::
      column(emp.salary) ::
      column(emp.salaryCurrency) :: Nil

    val c = buildContainer(columns)
    c shouldBe a [IndexedContainer]

    val ids = c.propertyIds.toList
    logger.debug(snap(ids).toString)

    c.propertyIds.toList should be (List("lastName", "firstName", "name.first", "salary", "salaryCurrency"))
  }

  it should "correctly split complex fields" in {
    splitSubfield("name") should be (List("name"))
    splitSubfield("name.first") should be (List("name", "first"))
  }

  it should "be able identify if given field code is subfield or not" in {
    isSubfield("name.first") should be (true)
    isSubfield("name.first.another") should be (true)
    isSubfield("name") should be (false)
  }

  it should "provide a value of the object field" in {
    objectValue(Some(employee), "firstName") should be (Some(employee.firstName.value))
    objectValue(Some(employee), "name") should be (Some(employee.name.value))
  }

  it should "provide a value of field" in {
    // org.d5i.traumweb.service.dao.TestEmployee.rec.name.value.first was ""
    logger.debug(snap(TestEmployee.rec.name.value.first).toString)

    fieldValue(employee, "firstName") should be (Some(employee.firstName.value))
    fieldValue(employee, "name") should be (Some(employee.name.value))
    val res = fieldValue(employee, "name.first")
    res should be (Some(employee.name.value.first.value))
  }

  it should "have a macro for building a column" in {
    column(employee.firstName) should be (Column(employee.firstName, "firstName"))
    column(employee.name.value.first) should be (Column(employee.name.value.first, "name.first"))
    column(TestEmployee.rec.firstName) should be (Column(TestEmployee.rec.firstName, "firstName"))
    column(TestEmployee.rec.name.value.first) should be (Column(TestEmployee.rec.name.value.first, "name.first"))

    column(employee.firstName, "My First Name") should be (Column(employee.firstName, "firstName", Some("My First Name")))
    column(employee.name.value.first, "My Name First") should be (Column(employee.name.value.first, "name.first", Some("My Name First")))
    column(TestEmployee.rec.firstName, "My First Name") should be (Column(TestEmployee.rec.firstName, "firstName", Some("My First Name")))
    column(TestEmployee.rec.name.value.first, "My Name First") should be (Column(TestEmployee.rec.name.value.first, "name.first", Some("My Name First")))
  }

  it should "have an ability to get a type of the record field" in {
    column(TestEmployee.rec.firstName).scalaFieldClass.isAssignableFrom(classOf[String]) should be (true)
    column(TestEmployee.rec.pin).scalaFieldClass.isAssignableFrom(classOf[Long]) should be (true)
    column(employee.name.value.first).scalaFieldClass.isAssignableFrom(classOf[String]) should be (true)
  }

}


class TestEmployee private() extends MongoRecord[TestEmployee] with ObjectIdPk[TestEmployee] /*with IndexedRecord[TestEmployee]*/ {
  def meta = TestEmployee
  object pin extends LongField(this) {
    override def displayName = "Pin"
  }
  object customerId extends StringField(this, 255) {
    override def displayName = "Customer Id"
  }
  object firstName extends StringField(this, 255) {
    override def displayName = "First Name"
  }
  object lastName extends StringField(this, 255) {
    override def displayName = "Last Name"
  }
  object salary extends DoubleField(this) {
    override def displayName = "Salary"
  }
  object salaryBigDecimal extends DecimalField(this, BigDecimal(0.00)) {
    override def displayName = "SalaryBG"
  }
  object salaryCurrency extends StringField(this, 3) {
    override def displayName = "Cur"
  }
  object hired extends JodaTimeField(this) {
    override def displayName = "Hired"
  }
  object name extends BsonRecordField(this, EmployeeName)
}

object TestEmployee extends TestEmployee with MongoMetaRecord[TestEmployee]  {

  override def collectionName = "employees"

  val testEmps = List(
    TestEmployee.createRecord.
      customerId("o_vpetr").firstName("Valery").lastName("Petrov").hired(DateTime.now).
      name(EmployeeName.createRecord.first("Valery2").last("Veselov")).salary(2000.0).salaryCurrency("USD").salaryBigDecimal(BigDecimal(2100.00)),
    TestEmployee.createRecord.
      pin(1232).customerId("o_vpetr!").firstName("Valery!").lastName("Petrov!").hired(DateTime.now).
      name(EmployeeName.createRecord.first("Valery2!").last("Veselov!"))
  )

  val rec = createRecord
}

class EmployeeName private() extends BsonRecord[EmployeeName] {
  def meta = EmployeeName
  object first extends StringField(this, 100) { override def displayName = "First" }
  object last extends StringField(this, 100) { override def displayName = "Last" }
}
object EmployeeName extends EmployeeName with BsonMetaRecord[EmployeeName] {
  override def fieldOrder = List(first, last)
}

