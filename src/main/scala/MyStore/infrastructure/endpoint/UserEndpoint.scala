package MyStore.infrastructure.endpoint

import MyStore.domain.User.{User, UserService}
import MyStore.domain.{UserAlreadyExistsError, UserNotFoundError}
import MyStore.infrastructure.endpoint.Pagination.{OptionalOffsetMatcher, OptionalPageSizeMatcher}
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.{jsonOf, _}
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}

class UserEndpoint extends Http4sDsl[IO] {
  implicit val userDecoder: EntityDecoder[IO, User] = jsonOf[IO, User]

  type Magic = PartialFunction[Request[IO], IO[Response[IO]]]

  def endpoints(
      userService: UserService,
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      getUserEndpoint(userService)
        .orElse(createUserEndpoint(userService))
        .orElse(updateUserEndpoint(userService))
        .orElse(deleteUserEndpoint(userService))
        .orElse(listUserEndpoint(userService))
    }

  private def getUserEndpoint(userService: UserService): Magic = { case GET -> Root / LongVar(id) =>
    userService.get(id).value.flatMap {
      case Right(found) => Ok(found.asJson)
      case Left(UserNotFoundError) => NotFound("The product was not found")
    }
  }

  private def createUserEndpoint(userService: UserService): Magic = { case req @ POST -> Root =>
    val action = for {
      product <- req.as[User]
      result <- userService.create(product).value
    } yield result

    action.flatMap {
      case Right(saved) =>
        Ok(saved.asJson)
      case Left(UserAlreadyExistsError(existing)) =>
        Conflict(s"The product ${existing.userName} already exists")
    }
  }

  private def updateUserEndpoint(userService: UserService): Magic = {
    case req @ PUT -> Root / LongVar(_) =>
      val action = for {
        user <- req.as[User]
        result <- userService.update(user).value
      } yield result

      action.flatMap {
        case Right(saved) => Ok(saved.asJson)
        case Left(UserNotFoundError) => NotFound("The product was not found")
      }
  }

  private def deleteUserEndpoint(userService: UserService): Magic = {
    case DELETE -> Root / LongVar(id) =>
      for {
        _ <- userService.delete(id)
        resp <- Ok()
      } yield resp
  }

  private def listUserEndpoint(userService: UserService): Magic = {
    case GET -> Root :? OptionalPageSizeMatcher(pageSize) :? OptionalOffsetMatcher(
          offset,
        ) =>
      for {
        retrieved <- userService.list(pageSize.getOrElse(10), offset.getOrElse(0))
        resp <- Ok(retrieved.asJson)
      } yield resp
  }
}

object UserEndpoint {
  def apply = new UserEndpoint
}
