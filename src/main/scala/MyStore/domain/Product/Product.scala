package MyStore.domain.Product

case class Product(
  id: Option[Long] = None,
  title: String,
  description: String,
  posterId: Long)