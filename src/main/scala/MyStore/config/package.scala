package MyStore
import io.circe.Decoder
import io.circe.generic.semiauto._

// [ITFPSL] Andrey here, I don't know that this folder does so i just copy pasted in it from the pet store

package object config {
  implicit val srDec: Decoder[ServerConfig] = deriveDecoder
  implicit val dbconnDec: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbDec: Decoder[DatabaseConfig] = deriveDecoder
  implicit val psDec: Decoder[PetStoreConfig] = deriveDecoder
}
