package com.itsmeijers.scraper

import org.jsoup.nodes.Document
import com.itsmeijers.models.DocumentationItems._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.jsoup.nodes.Element
import cats.syntax.flatMap._
import cats.data.Reader

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
    source <- scrapeSource
    linearSuperTypes <- scrapeLinearSuperTypes
    knownSubclasses <- scrapeKnownSubclasses
    constructors <- scrapeConstructors
    typeMembers <- scrapeTypeMembers
    valueMembers <- scrapeValueMembers
    abstractValueMembers <- scrapeAbstractValueMembers
    concreteValueMembers <- scrapeConcreteValueMembers
    shadowedImplicitValueMembers <- scrapeShadowedImplicitValueMembers
  } yield DocumentationItem(
            name,
            packageOwner,
            kind,
            relatedDocs,
            signature,
            comment,
            source,
            linearSuperTypes,
            knownSubclasses,
            constructors,
            typeMembers,
            valueMembers,
            abstractValueMembers,
            concreteValueMembers,
            shadowedImplicitValueMembers
          )

  private val definition = element("#definition")
  private val owner = element("#owner")

  def scrapeName: Scraper[String] = (document: Element) =>
    document >> definition >> text("h1")

  def scrapePackageOwner: Scraper[List[String]] = (document: Element) =>
    (document
      >> definition
      >> owner
      >> elements(".extype")).map(_ >> text("a")).toList

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


  def scrapeComment: Scraper[Comment] = ???

  def scrapeSource: Scraper[Source] = ???

  def scrapeLinearSuperTypes: Scraper[List[String]] = ???

  def scrapeKnownSubclasses: Scraper[List[String]] = ???

  def scrapeConstructors: Scraper[List[Constructor]] = ???

  def scrapeTypeMembers: Scraper[List[TypeMember]] = ???

  def scrapeValueMembers: Scraper[List[ValueMember]] = ???

  def scrapeAbstractValueMembers: Scraper[List[ValueMember]] = ???

  def scrapeConcreteValueMembers: Scraper[List[ValueMember]] = ???

  def scrapeShadowedImplicitValueMembers: Scraper[List[ValueMember]] = ???

}
