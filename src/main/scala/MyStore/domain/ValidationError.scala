package MyStore.domain

//import MyStore.domain.User.User
import MyStore.domain.Product.{Product => DomainProduct}


sealed trait ValidationError extends Product with Serializable
case class ProductAlreadyExistsError(product: DomainProduct) extends ValidationError
case class ReviewAlreadyExistsError(product: DomainProduct) extends ValidationError
case object ProductNotFoundError extends ValidationError
case object ReviewNotFoundError extends ValidationError
case object UserNotFoundError extends ValidationError
case class UserAlreadyExistsError(user: User.User) extends ValidationError
case class UserAuthenticationFailedError(userName: String) extends ValidationError
