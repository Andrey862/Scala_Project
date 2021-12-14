package MyStore.infrastructure.repository

import MyStore.domain.User.{User, UserAlgebra}
import MyStore.infrastructure.repository.SQLPagination.paginate
import cats.data.OptionT
import cats.effect.IO
import cats.syntax.all._
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}

private object UserSQL {

  val selectAll: Query0[User] = sql"""
    SELECT USERNAME, HASH, ID
    FROM USERS
  """.query

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USERNAME, HASH)
    VALUES (${user.userName}, ${user.hash})
  """.update

  def update(user: User, id: Long): Update0 = sql"""
    UPDATE USERS
    SET USERNAME = ${user.userName}, HASH = ${user.hash}, ID = $id,
    WHERE ID = $id
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT USERNAME, HASH, ID
    FROM USERS
    WHERE ID = $userId
  """.query

  def byUserName(userName: String): Query0[User] = sql"""
    SELECT USERNAME, HASH, ID
    FROM USERS
    WHERE USERNAME = $userName
  """.query[User]

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM USERS WHERE ID = $userId
  """.update

}

class UserInterpreter(val xa: Transactor[IO]) extends UserAlgebra {
  import UserSQL._

  def create(user: User): IO[User] =
    insert(user).withUniqueGeneratedKeys[Long]("id").map(id => user.copy(id = id.some)).transact(xa)

  def update(user: User): OptionT[IO, User] =
    OptionT.fromOption[IO](user.id).semiflatMap { id =>
      UserSQL.update(user, id).run.transact(xa).as(user)
    }

  def deleteByUserName(userName: String): OptionT[IO, User] =
    findByUserName(userName).mapFilter(_.id).flatMap(delete)

  def findByUserName(userName: String): OptionT[IO, User] =
    OptionT(byUserName(userName).option.transact(xa))

  def delete(userId: Long): OptionT[IO, User] =
    get(userId).semiflatMap(user => UserSQL.delete(userId).run.transact(xa).as(user))

  def get(userId: Long): OptionT[IO, User] = OptionT(select(userId).option.transact(xa))

  def list(pageSize: Int, offset: Int): IO[List[User]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)
}
object UserInterpreter {
  def apply(xa: Transactor[IO]): UserInterpreter =
    new UserInterpreter(xa)
}
