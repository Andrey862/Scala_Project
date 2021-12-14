package MyStore.domain.User

import cats.data.OptionT
import cats.effect.IO

trait UserAlgebra {
  def create(user: User): IO[User]


  // [ITFPSL] Andrey here, why do they use OptionT[IO, _] here and IO[Option[_]] everywhere else? what the difference? why?
  def update(user: User): OptionT[IO, User]

  def get(userId: Long): OptionT[IO, User]

  def delete(userId: Long): OptionT[IO, User]

  def findByUserName(userName: String): OptionT[IO, User]

  def deleteByUserName(userName: String): OptionT[IO, User]

  def list(pageSize: Int, offset: Int): IO[List[User]]

}