package scaladinext.mongo

//import com.foursquare.index.IndexedRecord
import com.typesafe.scalalogging.LazyLogging
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.record.field.joda.JodaTimeField
import net.liftweb.record.field.{IntField, StringField}
import org.scalatest.{FlatSpec, Matchers}

class TestRec private() extends MongoRecord[TestRec] with ObjectIdPk[TestRec] /*with IndexedRecord[TestRec] */ {
  def meta = TestRec
  object int extends IntField(this)
  object string extends StringField(this, 255)
  object jodaTime extends JodaTimeField(this)
  object finished extends JodaTimeField(this)
}

object TestRec extends TestRec with MongoMetaRecord[TestRec]  {
  override def collectionName = "testRecs"
}

class MongoPropertySpec extends FlatSpec with Matchers with LazyLogging {

  "MongoProperty" should "have all methods working" in {
    val record = TestRec.createRecord

    val prop = MongoProperty(record.int)
    prop.value should be (Some(record.int.value))

    prop.getType should be (classOf[java.lang.Integer])

    record.int(15)
    prop.value should be (Some(record.int.value))

    prop.readOnly should be (false)
    prop.readOnly = true
    prop.readOnly should be (true)

    prop.value = 17
    record.int.value should be (17)

    prop.value = Some(19)
    record.int.value should be (19)
  }

  it should "support String type" in {
    val record = TestRec.createRecord

    val prop = MongoProperty(record.string)
    prop.value should be (Some(record.string.value))
    prop.getType should be (classOf[java.lang.String]) // because this type is used in IntField

    record.string("str")
    prop.value should be (Some("str"))
    prop.value should be (Some(record.string.value))

    prop.value = "str2"
    record.string.value should be ("str2")
    record.string.value should be (prop.value.get)
  }
}
