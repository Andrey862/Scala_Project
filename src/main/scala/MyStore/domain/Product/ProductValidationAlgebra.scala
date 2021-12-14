package MyStore.domain.Product

import MyStore.domain.{ProductAlreadyExistsError, ProductNotFoundError}
import cats.effect.IO
import cats.data.EitherT

trait ProductValidationAlgebra {
  def doesNotExist(product: Product): EitherT[IO, ProductAlreadyExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[IO, ProductNotFoundError.type, Unit]
}
