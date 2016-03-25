package com.itsmeijers.models

package object DocumentationItems {

   trait DocumentationItemDefinition {
      // Name of class, trait, object package
      def name: String

      // the package in which the object receides
      def packageOwner: List[String]

      // Kind: Trait, Class, Object, Package
      def kind: Kind

      // Related documentation items
      def relatedDocs: List[Related]

      // The signature of the documentation item
      def signature: Signature

      // Explaination / comment of the DocumentationItem
      def comment: Option[Comment]

      /* List of possible attributes in the documentation as Version, SelfType,
      * Annotations, Since and Source
      */
      def documentAttributes: List[DocumentAttribute]

      // List of supertypes
      def linearSuperTypes: List[Type]

      // List of known subclasses
      def knownSubclasses: List[Type]

      // List of constructors of documentationItem (can be empty)
      def constructors: List[Constructor]

      // List of Type Members of the documentationItem (can be empty)
      def typeMembers: List[TypeMember]

      // All sorts of value members -> each own list -> no sorting -> could be quicker?
      def valueMembers: ValueMembers

      def abstractValueMembers: AbstractValueMembers

      def concreteValueMembers: ConcreteValueMembers

      def shadowedImplicitValueMembers: ShadowedImplicitValueMembers

      def deprecatedValueMembers: DeprecatedValueMembers
   }

   case class DocumentationItem (
     name: String,
     packageOwner: List[String],
     kind: Kind,
     relatedDocs: List[Related],
     signature: Signature,
     comment: Option[Comment],
     documentAttributes: List[DocumentAttribute],
     linearSuperTypes: List[Type],
     knownSubclasses: List[Type],
     constructors: List[Constructor],
     typeMembers: List[TypeMember],
     valueMembers: ValueMembers,
     abstractValueMembers: AbstractValueMembers,
     concreteValueMembers: ConcreteValueMembers,
     shadowedImplicitValueMembers: ShadowedImplicitValueMembers,
     deprecatedValueMembers: DeprecatedValueMembers
   ) extends DocumentationItemDefinition

   sealed trait Kind
   case object Package extends Kind
   case object Class extends Kind
   case object Object extends Kind
   case object Trait extends Kind
   case object CaseClass extends Kind
   case object Type extends Kind

   sealed trait DocumentAttribute
   case class SelfType(text: String) extends DocumentAttribute
   case class Source(name: String, url: String) extends DocumentAttribute
   case class Since(text: String) extends DocumentAttribute
   case class Annotations(annotations: List[String]) extends DocumentAttribute // change to List[Annotation]
   case class Version(text: String) extends DocumentAttribute
   case class Deprecated(text: String) extends DocumentAttribute
   case class SeeAlso(text: String) extends DocumentAttribute
   case class Attributes(text: String) extends DocumentAttribute
   case class Note(text: String) extends DocumentAttribute

   case class Annotation(name: String, arguments: List[String]) // changeTo List[Argument]

   case class Type(name: String, location: String)

   case class Related(
     kind: Kind,
     name: String,
     packageName: String)

   case class Signature(
     modifier: Option[String],
     kind: Kind,
     name: String,
     typeParams: List[String],
     parents: Option[String])

   case class Comment(comment: String)

   case class Constructor(
      modifier: Option[String],
      kind: String,
      name: String,
      params: List[Parameter])

  case class Parameter(name: String, kind: String) // improve

   // Add a param
   // trait Param
   // case FunctionParam extends Param
   // case ObjectParam extends Param

   trait Visibility
   case object Private extends Visibility
   case object Public extends Visibility

   // Improve quality!!
   case class TypeMember(
     name: String,
     visibility: Option[Visibility],
     kind: Kind,
     parents: Option[String],
     comment: Option[String],
     attributes: List[String])

   sealed trait Modifier
   case object Abstract extends Modifier
   case object Macro extends Modifier
   case object Final extends Modifier

   type ValueMembers = List[ValueMember]
   type AbstractValueMembers = List[ValueMember]
   type ConcreteValueMembers = List[ValueMember]
   type ShadowedImplicitValueMembers = List[ValueMember]
   type DeprecatedValueMembers = List[ValueMember]

   case class ValueMember(
     name: Option[String], // change this needs to all have a name!!
     visibility: Option[Visibility],
     typeParameters: Option[String],
     modifier: Option[Modifier],
     params: List[String],
     returnType: String,
     shortCommunt: Option[String],
     fullComment: Option[String])
     // add all these much more information needed!!!!
    //  valueComments: List[(String, String)], // name or returns -> description
    //  definitionClasses: List[String],
    //  note: Option[String],
    //  seeAlso: List[String])

}
