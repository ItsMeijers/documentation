package com.itsmeijers.scraper

import org.jsoup.nodes.Document
import com.itsmeijers.models.DocumentationItems._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import cats.syntax.flatMap._
import cats.data.Reader
import scala.util.Try

object DocumentationItemScraper {

  type Scraper[A] = Reader[Element, A]

  implicit def toReader[A](f: Element => A): Scraper[A] = Reader(f)

  def scrapeDocumentationItem: Scraper[DocumentationItem] = for {
    name <- scrapeName
    packageOwner <- scrapePackageOwner
    kind <- scrapeKind
    relatedDocs <- scrapeRelatedDocs
    signature <- scrapeSignature
    comment <- scrapeComment
    documentAttributes <- scrapeDocumentAttributes
    linearSuperTypes <- scrapeLinearSuperTypes
    knownSubclasses <- scrapeKnownSubclasses
    constructors <- scrapeConstructors
    typeMembers <- scrapeTypeMembers
    allValueMembers <- scrapeAllValueMembers
  } yield DocumentationItem(
      name, packageOwner, kind, relatedDocs, signature, comment, documentAttributes,
      linearSuperTypes, knownSubclasses, constructors, typeMembers, allValueMembers.valueMembers,
      allValueMembers.abstractValueMembers, allValueMembers.concreteValueMembers,
      allValueMembers.shadowedImplicitValueMembers, allValueMembers.deprecatedValueMembers)

  private val definition = element("#definition")
  private val owner = element("#owner")

  def scrapeName: Scraper[String] = (document: Element) =>
    document >> definition >> text("h1")

  // Change this function only the Scala package doesn't have an owner since its "THE owner"
  def scrapePackageOwner: Scraper[List[String]] = (document: Element) =>
    (document >> definition >?> owner)
      .map(x => (x >> elements(".extype")).toList.map(_ >> text("a")))
      .getOrElse(List())

  def scrapeKind: Scraper[Kind] = (document: Element) =>
    (document
      >> definition
      >> element("img")).attr("alt").split('/').head match {
        case "Trait" => Trait
        case "Class" => Class
        case "Object" => Object
        case "Package" => Package
      }

  def scrapeRelatedDocs: Scraper[List[Related]] = (document: Element) =>
    (document
      >> definition
      >?> element(".morelinks")) map { ml =>
        (ml >> elements("a")).map { moreLinks =>
          // extract kind and name
          val (kind, name) = extractKindAndName((moreLinks >> text("a")))
          // extract the name of the package of the Related item
          val packageName = moreLinks.attr("name")
          Related(kind, name, packageName)
        } toList
      } getOrElse List()

  def extractKindAndName(s: String): (Kind, String) = {
    val splitPos = s.indexOf(' ')
    val (kindString, name) = s.splitAt(splitPos)
    val kind = getKind(kindString)
    (kind, name)
  }

  def getKind(s: String) = s match {
    case "trait" => Trait
    case "class" => Class
    case "object" => Object
    case "package" => Package
    case "case class" => CaseClass
    case "type" => Type
  }

  def scrapeSignature: Scraper[Signature] = (document: Element) => {
    val signature = (document >> element("#signature"))

    val modifier = signature >?> text(".modifier")
    val kind = getKind(signature >> text(".kind"))
    val name = signature >> text(".name")
    val typeParams = (signature >?> element(".tparams")) map { elm =>
      (elm >> elements("span")).map(_ >> text("span")) toList
    } getOrElse List()
    val parents = signature >?> text(".result")

    Signature(modifier, kind, name, typeParams, parents)
  }

  /**
  * Imporove parsing not just retrieving text but analayuzing it wich are links and code to markdown
  */
  def scrapeComment: Scraper[Option[Comment]] = (document: Element) =>
   (document >?> text("#comment")).flatMap { t =>
     if(t.isEmpty) None else Some(Comment(t))
   }

  def scrapeDocumentAttributes: Scraper[List[DocumentAttribute]] =
    toReader { (document: Element) =>
      (for {
        comment <- document >?> element("#comment")
        attributes <- comment >?> element(".attributes")
      } yield (attributes >> texts("dt")).zip(attributes >> elements("dd")).map {
         case ("Self Type", elm) => extractSelfType(elm)
         case ("Annotations", elm) => extractAnnotations(elm)
         case ("Source", elm) => extractSource(elm)
         case ("Since", elm) => extractSince(elm)
         case ("Version", elm) => extractVersion(elm)
         case ("Deprecated", elm) => extractDeprecated(elm)
         case ("See also", elm) => extractSeeAlso(elm)
         case ("Attributes", elm) => extractAttributes(elm)
         case ("Note", elm) => extractNote(elm)
         //case ("Example", elm) => TODO
      }.toList).getOrElse(List())
    }

  def extractSelfType(elm: Element): DocumentAttribute = SelfType(elm >> text("dd"))

  def extractAnnotations(elm: Element): DocumentAttribute = Annotations((elm >> texts("a")).toList)

  def extractSource(elm: Element): DocumentAttribute = {
    val name = elm >> text("a")
    val link = (elm >> element("a")).attr("href")
    Source(name, link)
  }

  def extractSince(elm: Element): DocumentAttribute = Since(elm >> text("dd"))

  def extractVersion(elm: Element): DocumentAttribute = Version(elm >> text("dd"))

  def extractDeprecated(elm: Element): DocumentAttribute = Deprecated(elm >> text("dd"))

  def extractSeeAlso(elm: Element): DocumentAttribute = SeeAlso(elm >> text("dd"))

  def extractAttributes(elm: Element): DocumentAttribute = Attributes(elm >> text("dd"))

  def extractNote(elm: Element): DocumentAttribute = Note(elm >> text("dd"))

  def scrapeTypes(htmlClass: String): Element => List[Type] = (document: Element) =>
    document >?> element(htmlClass) >?> elements(".extype") map { elms =>
      elms.toList.map( elm => Type(elm.text, elm.attr("name")))
    } getOrElse List()

  def scrapeLinearSuperTypes: Scraper[List[Type]] = scrapeTypes(".superTypes")

  def scrapeKnownSubclasses: Scraper[List[Type]] = scrapeTypes(".subClasses")

  def scrapeConstructors: Scraper[List[Constructor]] = (document: Element) =>
    (document >?> element("#constructors"))
      .flatMap(c => Try(c >> elements("li") >> extractConstructors).toOption)
      .getOrElse(List())

  val extractConstructors = new HtmlExtractor[List[Constructor]] {
    def extract(docs: Elements): List[Constructor] = docs.toList map { doc =>
      val modifier = (doc >?> text(".modifier")).flatMap(s => if(s.isEmpty) None else Some(s))
      val kind = (doc >> text(".kind"))
      val name = (doc >> text(".name"))
      val params = doc >> element(".params") >> elements("span") >> extractParams
      Constructor(modifier, kind, name, params)
    }
  }

  // improve for extracting all params including types etc....
  val extractParams = new HtmlExtractor[List[Parameter]] {
    def extract(docs: Elements): List[Parameter] = docs.toList.map { doc =>
      val paramText = doc >> text("span")
      val position = paramText.indexOf(':')
      val (name, kind) = paramText.splitAt(position)
      Parameter(name, kind.drop(2))
    }
  }

  def scrapeTypeMembers: Scraper[List[TypeMember]] = (document: Element) =>
    (document >?> element("#types"))
      .map(_ >> elements("li") >> extractTypeMembers)
      .getOrElse(List())

  // Check if these are the only ones!!!
  def extractVisibility: String => Option[Visibility] = {
    case "prt" => Some(Private)
    case "pub" => Some(Public)
    case _ => None
  }

  val extractTypeMembers = new HtmlExtractor[List[TypeMember]] {
    def extract(docs: Elements): List[TypeMember] = docs.toList.flatMap { doc =>
      doc >?> text(".name") map { name =>
        val visibility = (doc >?> attr("li")("visbl")) flatMap extractVisibility

        val kind = getKind(doc >> text(".kind"))
        val parents = doc >?> text(".result") // improve
        val comment = doc >?> text(".comment")

        val attributes = (doc >?> element(".attributes"))
          .map { a =>
            (a >> texts("dt")).zip(a >> texts("dd")).map {
              case (s, s2) => s"$s $s2"
            }.toList
          }.getOrElse(List()) // extend to extractAttributes

        List(TypeMember(name, visibility, kind, parents, comment, attributes))
      } getOrElse(List())
    }
  }

  case class AllValueMembers(
    valueMembers: ValueMembers,
    abstractValueMembers: AbstractValueMembers,
    concreteValueMembers: ConcreteValueMembers,
    shadowedImplicitValueMembers: ShadowedImplicitValueMembers,
    deprecatedValueMembers: DeprecatedValueMembers)

  object AllValueMembers {
    def empty = AllValueMembers(List(), List(), List(), List(), List())
  }

  val extractAllValueMembers = new HtmlExtractor[AllValueMembers] {
    def extract(docs: Elements): AllValueMembers =
      updateValueMembers(AllValueMembers.empty, docs.toList)

    def updateValueMembers(start: AllValueMembers, docs: List[Element]): AllValueMembers =
      docs match {
        case h :: t =>
          val updated = h >> text("h3") match {
            case "Value Members" =>
              start.copy(valueMembers = (h >> elements("li") >> extractValueMembers))
            case "Abstract Value Members" =>
              start.copy(abstractValueMembers = (h >> elements("li") >> extractValueMembers))
            case "Concrete Value Members" =>
              start.copy(concreteValueMembers = (h >> elements("li") >> extractValueMembers))
            case "Shadowed Implicit Value Members" =>
              start.copy(shadowedImplicitValueMembers = (h >> elements("li") >> extractValueMembers))
            case "Deprecated Value Members" =>
              start.copy(deprecatedValueMembers = (h >> elements("li") >> extractValueMembers))
          }
          updateValueMembers(updated, t)
        case Nil => start
      }
  }

  def extractModifier: String => Option[Modifier] = {
    case "abstract" => Some(Abstract)
    case "macro" => Some(Macro)
    case "final" => Some(Final)
    case _ => None // on empty string!
  }

  val extractValueMembers = new HtmlExtractor[List[ValueMember]] {
    def extract(docs: Elements): List[ValueMember] = docs.toList.map { doc =>
      val name = List(
        doc >?> text(".name"),
        doc >?> text(".implicit")
      ).flatten.headOption

      val visibility = (doc >?> attr("li")("visbl"))
        .map(_.trim)
        .flatMap(extractVisibility)

      val typeParameters = doc >?> text(".tparams")

      val modifier = (doc >?> text(".modifier")) flatMap extractModifier

      val params = (doc >?> text(".params"))
        .map(pt => pt.drop(1).dropRight(1).split(',').toList)
        .getOrElse(List())

      // Super ugly fix this!!!!!!
      val returnType = (doc >?> text(".result")).getOrElse{
        "  "
      }.drop(2)

      val shortComment = doc >?> text(".shortcomment")

      val fullComment = doc >?> text(".fullcomment")

      ValueMember(name, visibility, typeParameters, modifier, params, returnType, shortComment, fullComment)
    }
  }

  // Needs allot of improvement currently only scraping the allMembers and none else
  def scrapeAllValueMembers: Scraper[AllValueMembers] = (document: Element) =>
    (document
      >> element("#template")
      >> element("#allMembers")
      >> elements("#values")
      >> extractAllValueMembers)

}
