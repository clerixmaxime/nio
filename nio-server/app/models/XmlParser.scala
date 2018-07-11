package models

import org.joda.time.DateTime
import utils.DateUtils
import utils.Result.AppErrors

import scala.util.Try
import scala.xml.{Elem, NodeSeq}

object XmlParser {

  implicit class XmlSyntax(val nodeSeq: Elem) extends AnyVal {
    def validate[T](implicit read: XMLRead[T]): Either[AppErrors, T] =
      read.read(nodeSeq)

    def validateNullable[T](default: Option[T] = None)(
        implicit readNullable: XMLReadNullable[T])
      : Either[AppErrors, Option[T]] =
      readNullable.readNullable(nodeSeq, default)
  }

}

trait XMLRead[T] {
  def read(xml: Elem): Either[AppErrors, T]
}

trait XMLReadNullable[T] {
  def readNullable(xml: Elem,
                   default: Option[T] = None): Either[AppErrors, Option[T]]
}

object XMLReads {
  implicit def readString: XMLRead[String] =
    (xml: Elem) =>
      Try(xml.head.text)
        .map(Right(_))
        .getOrElse(Left(AppErrors.error("invalid.path")))

  implicit def readInt: XMLRead[Int] =
    (xml: Elem) =>
      Try(xml.head.text.toInt)
        .map(Right(_))
        .getOrElse(Left(AppErrors.error("invalid.path")))

  implicit def readNullableString: XMLReadNullable[String] =
    (xml: Elem, default: Option[String]) =>
      xml.headOption
        .map(v => Right(Some(v.text)))
        .getOrElse(Right(default))

  implicit def readNullableInt: XMLReadNullable[Int] =
    (xml: Elem, default: Option[Int]) =>
      xml.headOption
        .map(v => Right(Some(v.text.toInt)))
        .getOrElse(Right(default))

  implicit def readNullableDateTime: XMLReadNullable[DateTime] =
    (xml: Elem, default: Option[DateTime]) =>
      xml.headOption
        .map(v =>
          Right(Option(DateUtils.utcDateFormatter.parseDateTime(v.text))))
        .getOrElse(Right(default))

}

object XmlUtil {

  implicit class XmlCleaner(val elem: Elem) extends AnyVal {

    def clean(): Elem =
      scala.xml.Utility.trim(elem) match {
        case res if res.isInstanceOf[Elem] => res.asInstanceOf[Elem]
      }
  }
}
