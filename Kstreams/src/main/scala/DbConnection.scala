package dbConnection
import cats.effect.IO
import cats.effect.kernel.Resource
import natchez.Trace.Implicits.noop
import skunk.Session

object ConnectionPool {

  def pool(
        host: String, 
        port: Int, 
        username: String, 
        database: String, 
        password: String, 
        maxPoolSize:Int
    ): Resource[IO, Resource[IO, Session[IO]]] = {
        Session.pooled[IO](
            host = host,
            port = port,
            user = username,
            database = database,
            password = Option(password),
            max = maxPoolSize
        )
    }
  
}