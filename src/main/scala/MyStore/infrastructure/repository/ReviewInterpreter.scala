package MyStore.infrastructure.repository

import MyStore.domain.Review.{Review, ReviewAlgebra}
import MyStore.infrastructure.repository.SQLPagination.paginate
import cats.data.OptionT
import cats.effect.IO
import cats.syntax.all._
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Transactor, Update0}

private object ReviewSQL {
  val selectAll: Query0[Review] = sql"""
    SELECT USER_NAME, HASH, ID
    FROM USERS
  """.query

  def insert(review: Review): Update0 = sql"""
    INSERT INTO REVIEW (DESCRIPTION, SCORE, AUTHORID, PRODUCTID)
    VALUES (${review.description}, ${review.score}, ${review.authorId}, ${review.productId})
  """.update

  // maybe do not update author and product??? idk
  def update(review: Review, id: Long): Update0 = sql"""
    UPDATE REVIEW
    SET DESCRIPTION = ${review.description}, SCORE = ${review.score}, AUTHORID = ${review.authorId}, PRODUCTID = ${review.productId}, ID= $id
    WHERE ID = $id
  """.update

  def select(reviewId: Long): Query0[Review] = sql"""
    SELECT DESCRIPTION, SCORE, AUTHORID, PRODUCTID, ID
    FROM REVIEW
    WHERE ID = $reviewId
  """.query

  def byRpoduct(productId: Long): Query0[Review] = sql"""
    SELECT DESCRIPTION, SCORE, AUTHORID, PRODUCTID, ID
    FROM REVIEW
    WHERE PRODUCTID = $productId
  """.query[Review]

  def delete(reviewId: Long): Update0 = sql"""
    DELETE FROM REVIEW WHERE ID = $reviewId
  """.update
}

class ReviewInterpreter(val xa: Transactor[IO]) extends ReviewAlgebra {
  import ReviewSQL._

  override def create(review: Review): IO[Review] = insert(review)
    .withUniqueGeneratedKeys[Long]("id")
    .map(id => review.copy(id = id.some))
    .transact(xa)

  override def update(review: Review): IO[Option[Review]] =
    OptionT
      .fromOption[ConnectionIO](review.id)
      .semiflatMap(id => ReviewSQL.update(review, id).run.as(review))
      .value
      .transact(xa)

  override def get(id: Long): IO[Option[Review]] = select(id).option.transact(xa)

  override def delete(id: Long): IO[Option[Review]] = OptionT(select(id).option)
    .semiflatMap(product => ReviewSQL.delete(id).run.as(product))
    .value
    .transact(xa)

  override def findByProduct(pageSize: Int, offset: Int, productId: Long): IO[List[Review]] =
    paginate(pageSize, offset)(ReviewSQL.byRpoduct(productId)).to[List].transact(xa)
}

object ReviewInterpreter {
  def apply(xa: Transactor[IO]) = new ReviewInterpreter(xa)
}
