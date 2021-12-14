package MyStore.domain.Product

import MyStore.domain.{ProductAlreadyExistsError, ProductNotFoundError}
import cats.data.EitherT
import cats.effect.IO

class ProductService(repository: ProductAlgebra, validation: ProductValidationAlgebra) {
  def create(product: Product): EitherT[IO, ProductAlreadyExistsError, Product] =
    for {
      _ <- validation.doesNotExist(product)
      saved <- EitherT.liftF(repository.create(product))
    } yield saved

  def update(product: Product): EitherT[IO, ProductNotFoundError.type, Product] =
    for {
      _ <- validation.exists(product.id)
      saved <- EitherT.fromOptionF(repository.update(product), ProductNotFoundError)
    } yield saved

  def get(id: Long): EitherT[IO, ProductNotFoundError.type, Product] =
    EitherT.fromOptionF(repository.get(id), ProductNotFoundError)

  def delete(id: Long): IO[Unit] =
    repository.delete(id).as(())

  def list(pageSize: Int, offset: Int): IO[List[Product]] =
    repository.list(pageSize, offset)
}

object ProductService {
  def apply(repository: ProductAlgebra, validation: ProductValidationAlgebra) =
    new ProductService(repository, validation)
}
