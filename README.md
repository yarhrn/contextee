## Contextee 

Every time operation is executed there is a context of that operation.
This context could be:
- request id for tracing in microservice architecture
- current user
- etc.

A big part of the code doesn't care about context, and there is no reason to pollute every signature of every function to pass context all over the code.
There is the technique for thread per request applications in JVM to hold context in ThreadLocal.
But ThreadLocal doesn't work(almost) in an asynchronous application. 

Luckily, there are several techniques to solve this using functional programming.
We can use Reader monad in conjunction with Tagless final encoding for transparent context passing.
Or create dedicated algebra in Free monad application, that will add operation for context extraction.

This repository is an attempt to create standard typeclass for handling context in the functional application.

```scala
trait KStore[F[_]] {
  def get(key: String): F[String]
}

case class Ctx(key: String)
type Context[F[_]] = Contextee[F, Ctx]

import cats.implicits._
def program[F[_] : Monad : KStore : Context]: F[String] = for {
  ctx <- Contextee[F, Ctx].context
  value <- implicitly[KStore[F]].get(ctx.key)
} yield value

type MonadStack[A] = Reader[Ctx, A]

implicit def kstore[F[_] : Monad] = new KStore[F] {
  override def get(key: String): F[String] = Monad[F].pure(key + key)
}

import Contextee._
program[MonadStack].run(Ctx("jk")) should be("jkjk")
```