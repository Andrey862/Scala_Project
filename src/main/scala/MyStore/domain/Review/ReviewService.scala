package MyStore.domain.Review

import MyStore.domain.ReviewNotFoundError
import cats.data.EitherT
import cats.effect.IO

class ReviewService(repository: ReviewAlgebra) {
  def postReview(review: Review): IO[Review] =
    repository.create(review)

  def get(id: Long): EitherT[IO, ReviewNotFoundError.type, Review] =
    EitherT.fromOptionF(repository.get(id), ReviewNotFoundError)

  def delete(id: Long): IO[Unit] =
    repository.delete(id).as(())

  def findByProduct(pageSize: Int, offset: Int, productId: Long): IO[List[Review]] =
    repository.findByProduct(pageSize, offset, productId)
}

object ReviewService {
  def apply(repository: ReviewAlgebra): ReviewService =
    new ReviewService(repository)
}
