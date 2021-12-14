package MyStore.infrastructure.repository

import MyStore.domain.Product.{Product, ProductAlgebra}
import MyStore.infrastructure.repository.SQLPagination.paginate
import cats.data.OptionT
import cats.effect.IO
import cats.syntax.all._
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Transactor, Update0}

private object ProductSQL {

  val selectAll: Query0[Product] = sql"""
    SELECT ID, TITLE, DESCRIPTION, POSTERID
    FROM PRODUCT
  """.query

  def insert(product: Product): Update0 =
    sql"""
    INSERT INTO PRODUCT (TITLE, DESCRIPTION, POSTERID)
    VALUES (${product.title}, ${product.description}, ${product.posterId})
  """.update

  def update(product: Product, id: Long): Update0 =
    // "SET ID = $id," - очень важный костыль
    sql"""
    UPDATE PRODUCT
    SET ID = $id, TITLE = ${product.title}, DESCRIPTION = ${product.description}, POSTERID = ${product.posterId}
    WHERE ID = $id
  """.update

  def select(id: Long): Query0[Product] =
    sql"""
    SELECT ID, TITLE, DESCRIPTION, POSTERID
    FROM PRODUCT
    WHERE ID = $id
  """.query

  def delete(id: Long): Update0 =
    sql"""
    DELETE FROM PRODUCT WHERE ID = $id
  """.update

  def byTitle(title: String): Query0[Product] =
    sql"""
    SELECT TITLE, DESCRIPTION
    FROM PRODUCT
    WHERE TITLE = $title
  """.query
}

class ProductInterpreter(val xa: Transactor[IO]) extends ProductAlgebra {
  import ProductSQL._

  def create(product: Product): IO[Product] =
    insert(product)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => product.copy(id = id.some))
      .transact(xa)

  def update(product: Product): IO[Option[Product]] =
    OptionT
      .fromOption[ConnectionIO](product.id)
      .semiflatMap(id => ProductSQL.update(product, id).run.as(product))
      .value
      .transact(xa)

  def get(id: Long): IO[Option[Product]] = select(id).option.transact(xa)

  def delete(id: Long): IO[Option[Product]] =
    OptionT(select(id).option)
      .semiflatMap(product => ProductSQL.delete(id).run.as(product))
      .value
      .transact(xa)

  def list(pageSize: Int, offset: Int): IO[List[Product]] =
    paginate(pageSize, offset)(selectAll).to[List].transact(xa)

  override def findByTitle(title: String): IO[List[Product]] =
    byTitle(title).to[List].transact(xa)
}

object ProductInterpreter {
  def apply(xa: Transactor[IO]): ProductInterpreter =
    new ProductInterpreter(xa)
}
