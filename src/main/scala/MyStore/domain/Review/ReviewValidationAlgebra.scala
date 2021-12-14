//package MyStore.domain.Review
//
//import MyStore.domain.Review.Review
//import MyStore.domain.{ReviewAlreadyExistsError, ReviewNotFoundError}
//import cats.effect.IO
//import cats.data.EitherT
//
//trait ReviewValidationAlgebra {
//  def doesNotExist(review: Review): EitherT[IO, ReviewAlreadyExistsError, Unit]
//
//  def exists(userId: Option[Long]): EitherT[IO, ReviewNotFoundError.type, Unit]
//}
