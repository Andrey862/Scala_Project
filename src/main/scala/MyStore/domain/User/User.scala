package MyStore.domain.User


case class User(
    userName: String,
    hash: String,
    id: Option[Long] = None)