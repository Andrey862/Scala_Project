package MyStore.domain.Product

import cats.effect.IO

trait ProductAlgebra {
  def create(pet: Product): IO[Product]

  def update(pet: Product): IO[Option[Product]]

  def get(id: Long): IO[Option[Product]]

  def delete(id: Long): IO[Option[Product]]

  def list(pageSize: Int, offset: Int): IO[List[Product]]

  def findByTitle(title: String): IO[List[Product]]
}
