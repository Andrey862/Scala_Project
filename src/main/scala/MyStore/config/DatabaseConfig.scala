package MyStore.config

import cats.effect.{Blocker, ContextShift, IO, Resource, Sync}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)

case class DatabaseConfig(
    url: String,
    driver: String,
    user: String,
    password: String,
    connections: DatabaseConnectionsConfig,
)

object DatabaseConfig {
  def dbTransactor(
      dbc: DatabaseConfig,
      connEc: ExecutionContext,
      blocker: Blocker,
  ): Resource[IO, HikariTransactor[IO]] = {

//    val dbExecutionContext = ExecutionContext.global // https://stackoverflow.com/questions/60227855/scala-cannot-find-an-implicit-value-for-contextshiftcats-effect-io
//    implicit val contextShift: ContextShift[IO] = IO.contextShift(dbExecutionContext)
    implicit val contextShift: ContextShift[IO] = IO.contextShift(connEc)
    HikariTransactor
      .newHikariTransactor[IO](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)
  }

  /** Runs the flyway migrations against the target database
    */
  def initializeDb(cfg: DatabaseConfig)(implicit S: Sync[IO]): IO[Unit] =
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      fw.migrate()
    }.as(())
}
