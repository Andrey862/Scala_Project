package MyStore.domain.Review

case class Review(
    description: String,
    score: Int,
    authorId: Long,
    productId: Long,
    id: Option[Long] = None,
)
