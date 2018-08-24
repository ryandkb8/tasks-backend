package utils

import java.time.ZoneId
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference

import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTimeZone, LocalDate}
import play.api.Mode.{Dev, Prod}
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

class ClockModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    environment.mode match {
      case Prod | Dev => Seq(bind[Clock].to[DefaultClock])
      case _ => Seq(bind[Clock].to[MockClock])
    }
  }
}

// this lets us have consistent dates when running tests
trait Clock {

  def currentDate: LocalDate

}

@Singleton
class DefaultClock @Inject() () extends Clock {
  override def currentDate: LocalDate = new LocalDate(ZoneId.of("America/New_York"))
}

@Singleton
class MockClock @Inject() () extends Clock {

  private val date = new AtomicReference[LocalDate](new LocalDate())

  def setDate(date: LocalDate): Unit = this.date.set(date)

  override def currentDate: LocalDate = date.get()

}