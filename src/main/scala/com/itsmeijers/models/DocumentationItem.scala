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
      def comment: Comment

      // Source of the documentation item with link pointing to git repo (github)
      def source: Source

      // List of supertypes
      def linearSuperTypes: List[String]

      // List of known subclasses
      def knownSubclasses: List[String]

      // List of constructors of documentationItem (can be empty)
      def constructors: List[Constructor]

      // List of Type Members of the documentationItem (can be empty)
      def typeMembers: List[TypeMember]

      // All sorts of value members -> each own list -> no sorting -> could be quicker?
      def valueMembers: List[ValueMember]

      def abstractValueMembers: List[ValueMember]

      def concreteValueMembers: List[ValueMember]

      def shadowedImplicitValueMembers: List[ValueMember]
   }

   case class DocumentationItem (
     name: String,
     packageOwner: List[String],
     kind: Kind,
     relatedDocs: List[Related],
     signature: Signature,
     comment: Comment,
     source: Source,
     linearSuperTypes: List[String],
     knownSubclasses: List[String],
     constructors: List[Constructor],
     typeMembers: List[TypeMember],
     valueMembers: List[ValueMember],
     abstractValueMembers: List[ValueMember],
     concreteValueMembers: List[ValueMember],
     shadowedImplicitValueMembers: List[ValueMember]
   ) extends DocumentationItemDefinition

   trait Kind
   case object Package extends Kind
   case object Class extends Kind
   case object Object extends Kind
   case object Trait extends Kind
   case object CaseClass extends Kind

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

   case class Source(name: String, url: String)

   case class Constructor(
      modifier: Option[String],
      kind: String,
      name: String,
      params: List[String])

   // Add a param
   // trait Param
   // case FunctionParam extends Param
   // case ObjectParam extends Param

   case class TypeMember(
     kind: Kind,
     name: String,
     typeParams: List[String],
     parents: List[String],
     comment: String,
     definitionClasses: List[String])

   case class ValueMember(
     name: String,
     isAbstract: Boolean,
     params: List[String],
     returnType: String,
     comment: Option[String],
     valueComments: List[(String, String)], // name or returns -> description
     definitionClasses: List[String],
     note: Option[String],
     seeAlso: List[String])

}
