package MyStore.infrastructure.endpoint

import MyStore.domain.Product.{Product, ProductService}
import MyStore.domain.{ProductAlreadyExistsError, ProductNotFoundError}
import MyStore.infrastructure.endpoint.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}

class ProductEndpoints extends Http4sDsl[IO] {
  implicit val productDecoder: EntityDecoder[IO, Product] = jsonOf[IO, Product]
  type Magic = PartialFunction[Request[IO], IO[Response[IO]]]

  def endpoints(
      productService: ProductService,
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      getProductEndpoint(productService)
        .orElse(createProductEndpoint(productService))
        .orElse(updateProductEndpoint(productService))
        .orElse(deleteProductEndpoint(productService))
        .orElse(listProductEndpoint(productService))
    }

  private def getProductEndpoint(productService: ProductService): Magic = {
    case GET -> Root / LongVar(id) =>
      productService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(ProductNotFoundError) => NotFound("The product was not found")
      }
  }

  private def createProductEndpoint(productService: ProductService): Magic = {
    case req @ POST -> Root =>
      val action = for {
        product <- req.as[Product]
        result <- productService.create(product).value
      } yield result

      action.flatMap {
        case Right(saved) =>
          Ok(saved.asJson)
        case Left(ProductAlreadyExistsError(existing)) =>
          Conflict(s"The product ${existing.title} already exists")
      }
  }

  private def updateProductEndpoint(productService: ProductService): Magic = {
    case req @ PUT -> Root / LongVar(_) =>
      val action = for {
        pet <- req.as[Product]
        result <- productService.update(pet).value
      } yield result

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(ProductNotFoundError) => NotFound("The product was not found")
      }
  }

  private def deleteProductEndpoint(productService: ProductService): Magic = {
    case DELETE -> Root / LongVar(id) =>
      for {
        _ <- productService.delete(id)
        resp <- Ok()
      } yield resp
  }

  private def listProductEndpoint(productService: ProductService): Magic = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
          offset,
        ) =>
      for {
        retrieved <- productService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        resp <- Ok(retrieved.asJson)
      } yield resp
  }

}

object ProductEndpoints {
  def apply = new ProductEndpoints
}
