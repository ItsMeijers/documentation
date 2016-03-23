package com.itsmeijers.models

package object DocumentationItems {

   trait DocumentationItem {
      def title: String
      def documentationType: DocumentationType
      def packages: List[String]
      def constructors: List[Constructor]
   }

   case class Constructor(
      modifier: String,
      kind: String,
      name: String,
      params: List[Param])

   case class Param(
      name: String,
      paramType: String,
      paramExType: String)

   case class DocItem(
      title: String,
      documentationType: DocumentationType,
      packages: List[String],
      constructors: List[Constructor]) extends DocumentationItem

   trait DocumentationType
   case object Package extends DocumentationType
   case object Class extends DocumentationType
   case object Object extends DocumentationType
   case object Trait extends DocumentationType

}
