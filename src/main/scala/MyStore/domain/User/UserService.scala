package MyStore.domain.User

import MyStore.domain.{UserNotFoundError, UserAlreadyExistsError}
import cats.data.EitherT
import cats.effect.IO

class UserService(repository: UserAlgebra, validation: UserValidationAlgebra) {
  def create(user: User): EitherT[IO, UserAlreadyExistsError, User] =
    for {
      _ <- validation.doesNotExist(user)
      saved <- EitherT.liftF(repository.create(user))
    } yield saved

  def update(user: User): EitherT[IO, UserNotFoundError.type, User] =
    for {
      _ <- validation.exists(user.id)
      saved <- repository.update(user).toRight(UserNotFoundError)
    } yield saved

  def get(id: Long): EitherT[IO, UserNotFoundError.type, User] =
    repository.get(id).toRight(UserNotFoundError)

  def delete(id: Long): IO[Unit] =
    repository.delete(id).value.void

  def list(pageSize: Int, offset: Int): IO[List[User]] =
    repository.list(pageSize, offset)
}

object UserService {
  def apply(repository: UserAlgebra, validation: UserValidationAlgebra) =
    new UserService(repository, validation)

}
