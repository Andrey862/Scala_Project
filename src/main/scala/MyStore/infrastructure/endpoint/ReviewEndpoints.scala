package MyStore.infrastructure.endpoint

import MyStore.domain.Review.{Review, ReviewService}
import MyStore.domain.ReviewNotFoundError
import MyStore.infrastructure.endpoint.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}

class ReviewEndpoints extends Http4sDsl[IO] {
  implicit val reviewDecoder: EntityDecoder[IO, Review] = jsonOf[IO, Review]
  type Magic = PartialFunction[Request[IO], IO[Response[IO]]]

  def endpoints(
                 reviewService: ReviewService,
               ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      getReviewEndpoint(reviewService)
        .orElse(createReviewEndpoint(reviewService))
        .orElse(deleteReviewEndpoint(reviewService))
        .orElse(listReviewEndpoint(reviewService))
    }

  private def getReviewEndpoint(reviewService: ReviewService): Magic = {
    case GET -> Root / LongVar(id) =>
      reviewService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(ReviewNotFoundError) => NotFound("The product was not found")
      }
  }

  private def createReviewEndpoint(reviewService: ReviewService): Magic = {
    case req @ POST -> Root =>
      for {
        product <- req.as[Review]
        result <- reviewService.postReview(product)
        resp <- Ok(result.asJson)
      } yield resp
  }


  private def deleteReviewEndpoint(reviewService: ReviewService): Magic = {
    case DELETE -> Root / LongVar(id) =>
      for {
        _ <- reviewService.delete(id)
        resp <- Ok()
      } yield resp
  }


  object ProductIdMatcher extends QueryParamDecoderMatcher[Long]("productId")

  private def listReviewEndpoint(reviewService: ReviewService): Magic = {
    case GET -> Root :? ProductIdMatcher(productId) :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
    offset,
    ) =>
      for {
        retrieved <- reviewService.findByProduct(pageSize.getOrElse(10), offset.getOrElse(0), productId)
        resp <- Ok(retrieved.asJson)
      } yield resp
  }
}

object ReviewEndpoints {
  def apply() = new ReviewEndpoints()
}
