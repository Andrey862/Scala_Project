package MyStore.domain.Review

import cats.effect.IO


trait ReviewAlgebra {
  def create(pet: Review): IO[Review]

  def update(pet: Review): IO[Option[Review]]

  def get(id: Long): IO[Option[Review]]

  def delete(id: Long): IO[Option[Review]]

  def findByProduct(pageSize: Int, offset: Int, productId: Long): IO[List[Review]]
}
