package org.apache.spark.sql.sources

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.sources.RawDDLObjectType.RawDDLObjectType
import org.apache.spark.sql.sources.RawDDLStatementType.RawDDLStatementType
import org.apache.spark.sql.types.StructType

case object RawDDLObjectType {

  sealed trait RawDDLObjectType {
    val name: String
    override def toString: String = name
  }

  sealed abstract class BaseRawDDLObjectType(val name: String) extends RawDDLObjectType
  sealed trait RawData

  case object PartitionFunction extends BaseRawDDLObjectType("partition function")
  case object PartitionScheme   extends BaseRawDDLObjectType("partition scheme")
  case object Collection        extends BaseRawDDLObjectType("collection") with RawData
  case object Series            extends BaseRawDDLObjectType("table") with RawData
  case object Graph             extends BaseRawDDLObjectType("graph") with RawData
}

case object RawDDLStatementType {

  sealed trait RawDDLStatementType

  case object Create extends RawDDLStatementType
  case object Drop   extends RawDDLStatementType
  case object Append extends RawDDLStatementType
}

/**
  * Indicates that a datasource supports the raw sql interface
  */
trait RawSqlSourceProvider {
  /**
    * Forwards a query to the engine/source it refers to
    *
    * @param sqlCommand SQL String that has to be queried
    * @return RDD representing the result of the SQL String
    */
  def getRDD(sqlCommand: String): RDD[Row]

  /**
    * Determines the schema of the raw sql string. Usually this happens by executing query to
    * extract the schema.
    *
    * @param sqlCommand user defined sql string
    * @return schema of the result of sqlCommand parameter
    */
  def getResultingAttributes(sqlCommand: String): Seq[Attribute]

  /**
    * Runs a DDL statement
    *
    * @param identifier the identifier created by the DDL
    * @param objectType the type of the database object to modify
    * @param statementType the type of the SQL statement to execute
    * @param sparkSchema the type of the columns to be created, optional
    * @param sqlCommand user defined sql string
    * @param options the create options
    */
  def executeDDL(identifier: String,
                 objectType: RawDDLObjectType,
                 statementType: RawDDLStatementType,
                 sparkSchema: Option[StructType],
                 sqlCommand: String,
                 options: Map[String, String]): Unit
}
