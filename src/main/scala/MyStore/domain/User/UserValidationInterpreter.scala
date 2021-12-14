package MyStore.domain.User

import MyStore.domain.{UserAlreadyExistsError, UserNotFoundError}
import cats.effect.IO
import cats.data.EitherT
import cats.syntax.all._


// [ITFPSL] Andrey here and I don't know what all this code does
class UserValidationInterpreter(userRepo: UserAlgebra)
  extends UserValidationAlgebra {
  def doesNotExist(user: User): EitherT[IO, UserAlreadyExistsError, Unit] =
    userRepo
      .findByUserName(user.userName)
      .map(UserAlreadyExistsError)
      .toLeft(())

  def exists(userId: Option[Long]): EitherT[IO, UserNotFoundError.type, Unit] =
    userId match {
      case Some(id) =>
        userRepo
          .get(id)
          .toRight(UserNotFoundError)
          .void
      case None =>
        EitherT.left[Unit](UserNotFoundError.pure[IO])
    }
}

object UserValidationInterpreter {
  def apply(repo: UserAlgebra): UserValidationAlgebra =
    new UserValidationInterpreter(repo)
}
