package scaladinext.model

trait Id[T] {
  def id: T
}

trait Name {
  def name: String
}

trait ParentId[T] {
  def parentId: Option[T]
}

trait Hierarchical[T] extends Name with ParentId[T] with Id[T]
