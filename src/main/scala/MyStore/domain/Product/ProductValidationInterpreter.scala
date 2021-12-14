package MyStore.domain.Product

import MyStore.domain.{ProductAlreadyExistsError, ProductNotFoundError}
import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all._

class ProductValidationInterpreter(repository: ProductAlgebra) extends ProductValidationAlgebra {
  def doesNotExist(product: Product): EitherT[IO, ProductAlreadyExistsError, Unit] = EitherT {
    repository.findByTitle(product.title).map { matches =>
      if (matches.forall(possibleMatch => possibleMatch.description != product.description)) {
        Right(())
      } else {
        Left(ProductAlreadyExistsError(product))
      }
    }
  }

  def exists(petId: Option[Long]): EitherT[IO, ProductNotFoundError.type, Unit] =
    EitherT {
      petId match {
        case Some(id) =>
          repository.get(id).map {
            case Some(_) => Right(())
            case _ => Left(ProductNotFoundError)
          }
        case _ =>
          Either.left[ProductNotFoundError.type, Unit](ProductNotFoundError).pure[IO]
      }
    }
}

object PetValidationInterpreter {
  def apply(repository: ProductAlgebra) =
    new ProductValidationInterpreter(repository)
}
