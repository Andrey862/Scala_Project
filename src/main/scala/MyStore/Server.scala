package MyStore
import config._
import domain.User._
import domain.Review._
import domain.Product._
import infrastructure.endpoint._
import infrastructure.repository._
import cats.effect._
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import doobie.util.ExecutionContexts
import io.circe.config.parser
///import domain.authentication.Auth

//import cats.effect.unsafe.implicits.global

object Server extends IOApp{

  def createServer: Resource [IO, H4Server[IO]] = {
    for {
      conf <- Resource.eval(parser.decodePathF[IO, PetStoreConfig]("mystore"))
      // What is it?
      serverEc <- ExecutionContexts.cachedThreadPool[IO]
      // What is it?
      connEc <- ExecutionContexts.fixedThreadPool[IO](conf.db.connections.poolSize)
      // What is it?
      txnEc <- ExecutionContexts.cachedThreadPool[IO]
      xa <- DatabaseConfig.dbTransactor(conf.db, connEc, Blocker.liftExecutionContext(txnEc))
      userRepo = UserInterpreter(xa)
      productRepo = ProductInterpreter(xa)
      reviewRepo = ReviewInterpreter(xa)
      productValidation = new ProductValidationInterpreter(productRepo)
      productService = new ProductService(productRepo, productValidation)
      userValidation = UserValidationInterpreter(userRepo)
      userService = UserService(userRepo, userValidation)
      reviewService = ReviewService(reviewRepo)
      httpApp = Router(
        "/users" -> (new UserEndpoint).endpoints(userService),
        "/review" -> (new ReviewEndpoints).endpoints(reviewService),
        "/product" -> (new ProductEndpoints).endpoints(productService)
      ).orNotFound
      // What is it?
      _ <- Resource.eval(DatabaseConfig.initializeDb(conf.db))
      // What is all of these?
      server <- BlazeServerBuilder[IO](serverEc)
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server
  }

  override def run(args: List[String]): IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)
}
