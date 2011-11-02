package com.cldellow.ballero.data

import java.lang.reflect._
import scala.collection.JavaConversions
import android.util.Log
import org.json.{JSONArray, JSONObject}

object Parser {

  // Java reflection doesn't keep the names of parameters; but if we require T to be a
  // Product, we can rely on the totally unstated and non-supported reality that
  // getFields() after Dexing returns the fields in alphabetical order.
  //
  // Other options include using the Scala compiler library to parse the bytecode--
  // this bloats the # of classes to an unacceptable level.
  private def sanityCheck(klazz: Class[_]) {
    val constructors = klazz.getConstructors

    val crazyFields = klazz.getDeclaredFields.filter { _.getName.startsWith("$") }
    if(!crazyFields.isEmpty)
      error("class %s: cannot deserialize nested classes".format(klazz))

    if(constructors.length != 1)
      error("class %s: cannot deserialize class with more than 1 constructor".format(klazz))

    // Are there any generic fields that don't have higher kind attributes?
  }

  def parseList[T](str: String)(implicit mf: Manifest[T]): List[T] = {
    val arr = new JSONArray(str)
    parseArray(arr, mf.erasure).asInstanceOf[List[T]]
  }
  def parse[T <: Product](str: String)(implicit mf: Manifest[T]): T = {
    //sanityCheck(mf.erasure)
    val jsonObject = new JSONObject(str)
    parse(jsonObject, mf.erasure)
  }

  private def parseOption(value: Any, desiredType: Type): Option[_] =
    if(value != null && value.asInstanceOf[AnyRef] != null && value.toString != "null")
      Some(parseType(value, desiredType))
    else
      None

  private def parseArray(jsonArray: JSONArray, desiredType: Type): List[_] =
    if(jsonArray.length == 0)
      Nil
    else {
      val listBuffer = new collection.mutable.ListBuffer[Any]
      for(i <- 0 until jsonArray.length) {
        listBuffer += parseType(jsonArray.get(i), desiredType)
      }
      listBuffer.toList
    }

  private def actualInstanceOf(what: Class[_], target: Class[_]): Boolean =
    if(what.getName == target.getName)
      true
    else
      what.getSuperclass match {
        case null => false
        case x => instanceOf(x, target)
      }

  private def instanceOf(what: Class[_], target: Class[_]): Boolean =
    (what :: what.getInterfaces.toList)
      .exists { actualInstanceOf(_, target) }

  private def parseTypeFromObject(jsonObject: JSONObject, name: String, desiredType: Type): Any = {
    //Log.i("PARSER", "parsing field %s".format(name))
    parseType(jsonObject.opt(name), desiredType)
  }

  private def parseType(value: Any, desiredType: Type): Any = desiredType match {
    case desiredType: Class[_] =>
      //Log.i("PARSER", "attempting to parse %s".format(value))
      desiredType.getName match {
        case "int"|"java.lang.Integer" => value.asInstanceOf[Int]
        case "boolean" => value.asInstanceOf[Boolean]
        case "java.lang.String" => value.asInstanceOf[String]
        case "scala.math.BigDecimal" => BigDecimal(
          if(value.isInstanceOf[Int])
            value.asInstanceOf[Int]
          else if(value.isInstanceOf[Double])
            value.asInstanceOf[Double]
          else
            error("unknown numeric type: %s".format(value))
          )
        case x if instanceOf(desiredType, classOf[Product]) =>
          parse(value.asInstanceOf[JSONObject], desiredType)
        case x => error("unknown class: %s (%s)"
          .format(x,
            desiredType
          ))
      }
    case desiredType: ParameterizedType =>
      //Log.i("PARSER", "attempting to parse %s".format(value))
      // OK, desiredType.getActualTypeArguments gives Class... now, how do we know
      // what the outer one is?
      require(desiredType.getRawType.isInstanceOf[Class[_]], "rawType is not an instance of Class[T]")
      require(desiredType.getActualTypeArguments.forall { arg => arg.isInstanceOf[Class[_]] ||
      arg.isInstanceOf[ParameterizedType] },
        "one of the actualTypeArguments is not an instance of Class[T]")
      val rawType = desiredType.getRawType.asInstanceOf[Class[_]]
      rawType.getName match {
        case "scala.collection.immutable.List" =>
          val array = value.asInstanceOf[JSONArray]
          parseArray(array, desiredType.getActualTypeArguments.head)
        case "scala.Option" =>
          parseOption(value, desiredType.getActualTypeArguments.head)
      }
    case x => 
      println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
      println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
      error("unknown type: %s".format(x))
  }

  private def javaObject(x: Any): Object = x.asInstanceOf[Object]

  def parse[T <: Product](jsonObject: JSONObject, klazz: Class[_]): T = {
    val constructors = klazz.getConstructors

    // Sort them alphabetically descending so dexing can't screw us up
    val fields = klazz.getDeclaredFields.filter { f => 
      val name = f.getName
      !name.startsWith("$") && !name.startsWith("_")
    }.toList.sortBy { _.getName }

    val nameToIndex = Map() ++ fields.map { _.getName }.zipWithIndex

    val constructor = constructors.head

    // Need to pair up stuff from the JSON object with fields in the case class
    // constructor.
    val values: Map[String, Any] = Map() ++ fields.flatMap { field =>
      val name = field.getName
      if(jsonObject has name) {
        val desiredType = field.getGenericType
        List(name -> parseTypeFromObject(jsonObject, name, desiredType))
      } else {
        // not found: if it's optional, use none
        field.getGenericType match {
          case pt: ParameterizedType =>
            pt.getRawType match {
              case c: Class[_] =>
                if(c.getName == "scala.Option")
                  List(name -> None)
                else
                  Nil
              case _ => Nil
            }
          case _ => Nil
        }
      }
    }

    val missingKeys = fields.map { _.getName }.toSet -- values.keySet
    if(!missingKeys.isEmpty)
      error("failed to deserialize %s: missing these keys: %s".format(klazz.getSimpleName, missingKeys.mkString(", ")))

    // Invoke the constructor
    //Log.i("PARSER", "fields: %s".format(fields))
    //Log.i("PARSER", "values: %s".format(values))
    val inputs = fields.map { _.getName }.map { values(_) }
    //Log.i("PARSER", "inputs: %s".format(inputs))
    val javaObjects = inputs.map { javaObject(_) }
    //Log.i("PARSER", "java inputs: %s".format(javaObjects))
    val x = constructor.newInstance(javaObjects:_*)
    x.asInstanceOf[T]
  }

  def serialize[T <: Product](item: T)(implicit mf: Manifest[T]): String = serialize(item, mf.erasure).toString

  private def toSerializedForm(_type: Type, value: Any): Option[Any] = {
    _type match {
      case desiredType: Class[_] =>
        Some(desiredType.getName match {
          case "int"|"java.lang.Integer" => value.asInstanceOf[Int]
          case "boolean" => value.asInstanceOf[Boolean]
          case "java.lang.String" => value.asInstanceOf[String]
          case "scala.math.BigDecimal" => value.asInstanceOf[BigDecimal].toDouble
          case x if instanceOf(desiredType, classOf[Product]) =>
            serialize(value.asInstanceOf[Product], desiredType)
          case x => error("unknown: %s".format(x))
        })
      case desiredType: ParameterizedType =>
        // OK, desiredType.getActualTypeArguments gives Class... now, how do we know
        // what the outer one is?
        require(desiredType.getRawType.isInstanceOf[Class[_]], "rawType is not an instance of Class[T]")
        val rawType = desiredType.getRawType.asInstanceOf[Class[_]]
        val actualTypes = desiredType.getActualTypeArguments//.collect { case x: Class[_] => x }
        rawType.getName match {
          case "scala.collection.immutable.List" =>
            val listValue = value
            // Scala's compiler blows at overloaded methods
            val rv = new JSONArray
            listValue.asInstanceOf[List[_]].foreach { item =>
              toSerializedForm(actualTypes.head, item) map { rv.put(_) }
            }
            Some(rv)
          case "scala.Option" =>
            val optionalValue = value
            if(optionalValue != None) {
              val realValue = optionalValue.asInstanceOf[Option[_]].get
              toSerializedForm(actualTypes.head, realValue)
            } else {
              None
            }
        }
      case x => error("unknown type: %s".format(x))
    }
  }

  def serializeList[T](xs: List[T])(implicit mf: Manifest[T]): String = {
    new JSONArray(
      JavaConversions.asJavaCollection(xs.map { item => 
        toSerializedForm(mf.erasure, item) }.flatten)).toString
  }

  private def serializeTypeToObject(item: Any, field: Field, _type: Type, jsonObject: JSONObject) {
    // see http://stackoverflow.com/questions/6756442/scala-class-declared-fields-and-access-modifiers
    field.setAccessible(true)
    val name = field.getName
    _type match {
      case desiredType: Class[_] =>
        toSerializedForm(_type, field.get(item)) map { rv => jsonObject.put(name, rv) }
      case desiredType: ParameterizedType =>
        // OK, desiredType.getActualTypeArguments gives Class... now, how do we know
        // what the outer one is?
        require(desiredType.getRawType.isInstanceOf[Class[_]], "rawType is not an instance of Class[T]")
        val rawType = desiredType.getRawType.asInstanceOf[Class[_]]
        val actualTypes = desiredType.getActualTypeArguments//.collect { case x: Class[_] => x }
        rawType.getName match {
          case "scala.collection.immutable.List" =>
            val listValue = field.get(item)
            // Scala's compiler blows at overloaded methods
            jsonObject.put(name, new JSONArray)
            listValue.asInstanceOf[List[_]].foreach { item =>
              toSerializedForm(actualTypes.head, item) map { rv => 
                jsonObject.accumulate(name, rv)
              }
            }
          case "scala.Option" =>
            val optionalValue = field.get(item)
            if(optionalValue != None) {
              val realValue = optionalValue.asInstanceOf[Option[_]].get
              toSerializedForm(actualTypes.head, realValue) map { rv =>
                jsonObject.put(name, rv)
              }
            }
        }
      case x => 
        println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
        println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
        error("unknown type: %s".format(x))
      }      
  }

  def serialize[T <: Product](item: T, klazz: Class[_]): JSONObject = {
    val result = new JSONObject
    val fields = klazz.getDeclaredFields.filter { field => !field.getName.startsWith("$") &&
    !field.getName.startsWith("_")}.toList
    fields.foreach { field =>
      serializeTypeToObject(item, field, field.getGenericType, result)
    }

    result
  }

}
