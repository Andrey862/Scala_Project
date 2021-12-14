package MyStore.domain.User

import MyStore.domain.{UserAlreadyExistsError, UserNotFoundError}
import cats.effect.IO
import cats.data.EitherT

trait UserValidationAlgebra {
  def doesNotExist(user: User): EitherT[IO, UserAlreadyExistsError, Unit]

  def exists(userId: Option[Long]): EitherT[IO, UserNotFoundError.type, Unit]
}
