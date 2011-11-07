package com.cldellow.ballero.data

import java.lang.reflect._
import scala.collection.JavaConversions
import android.util.Log
import org.json.{JSONArray, JSONObject}

final object Parser {
  // Java reflection doesn't keep the names of parameters; but if we require T to be a
  // Product, we can rely on the totally unstated and non-supported reality that
  // getFields() after Dexing returns the fields in alphabetical order.
  //
  // Other options include using the Scala compiler library to parse the bytecode--
  // this bloats the # of classes to an unacceptable level.
  private final def sanityCheck(klazz: Class[_]) {
    val constructors = klazz.getConstructors

    val crazyFields = klazz.getDeclaredFields.filter { _.getName.startsWith("$") }
    if(!crazyFields.isEmpty)
      error("class %s: cannot deserialize nested classes".format(klazz))

    if(constructors.length != 1)
      error("class %s: cannot deserialize class with more than 1 constructor".format(klazz))

    // Are there any generic fields that don't have higher kind attributes?
  }

  final def parseList[T](str: String)(implicit mf: Manifest[T]): List[T] = {
    val tm = System.currentTimeMillis
    val arr = new JSONArray(str)
    val newtm = System.currentTimeMillis
    val rv = parseArray(arr, mf.erasure).asInstanceOf[List[T]]
    //Log.i("PARSER", "parseArray parse took %s".format(System.currentTimeMillis - newtm))
    rv
  }

  final def parseAsList[T <: Product](str: String)(implicit mf: Manifest[T]): List[T] = {
    //Log.i("PARSER", "trying to parse %s from %s".format(mf.erasure.getName, str))
    List(parse[T](str))
  }

  final def parse[T <: Product](str: String)(implicit mf: Manifest[T]): T = {
    //sanityCheck(mf.erasure)
    //Log.i("PARSER", "trying to parse %s".format(str))
    val tm = System.currentTimeMillis
    val jsonObject = new JSONObject(str)
    //Log.i("PARSER", "parse parse took %s".format(System.currentTimeMillis - tm))
    parse(jsonObject, mf.erasure)
  }

  private final def parseOption(value: Any, desiredType: Type): Option[_] =
    if(value != null && value.asInstanceOf[AnyRef] != null && value != JSONObject.NULL)
      Some(parseType(value, desiredType))
    else
      None

  private final def parseArray(jsonArray: JSONArray, desiredType: Type): List[_] = {
    val length = jsonArray.length

    if(length == 0)
      Nil
    else {
      val listBuffer = new collection.mutable.ListBuffer[Any]
      for(i <- 0 until length) {
        listBuffer += parseType(jsonArray.get(i), desiredType)
      }
      listBuffer.toList
    }
  }

  import scala.collection.JavaConversions._
  val isProductMap: collection.mutable.ConcurrentMap[(Class[_], Class[_]), Boolean] = 
    new java.util.concurrent.ConcurrentHashMap[(Class[_], Class[_]), Boolean]

  private final def actualInstanceOf(what: Class[_], target: Class[_]): Boolean =
    if(what.getName == target.getName)
      true
    else
      what.getSuperclass match {
        case null => false
        case x => instanceOf(x, target)
      }

  private final def instanceOf(what: Class[_], target: Class[_]): Boolean = {
    if(isProductMap.contains(what, target)) {
      isProductMap(what, target)
    }
    else {
      val rv = (what :: what.getInterfaces.toList)
        .exists { actualInstanceOf(_, target) }
      isProductMap((what, target)) = rv
      rv
    }
  }

  private final def parseType(value: Any, desiredType: Type): Any = desiredType match {
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
      val rawRawType = desiredType.getRawType
      val actualTypes = desiredType.getActualTypeArguments
      require(rawRawType.isInstanceOf[Class[_]], "rawType is not an instance of Class[T]")
      require(actualTypes.forall { arg => arg.isInstanceOf[Class[_]] ||
        arg.isInstanceOf[ParameterizedType] },
        "one of the actualTypeArguments is not an instance of Class[T]")
      val rawType = rawRawType.asInstanceOf[Class[_]]
      rawType.getName match {
        case "scala.collection.immutable.List" =>
          val array = value.asInstanceOf[JSONArray]
          parseArray(array, actualTypes.head)
        case "scala.Option" =>
          parseOption(value, actualTypes.head)
      }
    case x => 
      println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
      println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
      error("unknown type: %s".format(x))
  }

  private final def javaObject(x: Any): Object = x.asInstanceOf[Object]


  case class ParseInfo(fields: List[Field], nameToIndex: Map[String, Int], constructor: Constructor[_], fieldNames:
  List[String], fieldNameSet: Set[String])
  val klazzReflectors: collection.mutable.ConcurrentMap[Class[_], ParseInfo] = 
    new java.util.concurrent.ConcurrentHashMap[Class[_], ParseInfo]


  final def parse[T <: Product](jsonObject: JSONObject, klazz: Class[_]): T = {
    if(!klazzReflectors.contains(klazz)) {
      val constructors = klazz.getConstructors

      // Sort them alphabetically descending so dexing can't screw us up
      val fields = klazz.getDeclaredFields.filter { f => 
        val name = f.getName
        !name.contains("$") && !name.startsWith("_")
      }.toList.sortBy { _.getName }

      val nameToIndex = Map() ++ fields.map { _.getName }.zipWithIndex

      val constructor = constructors.head
      constructor.setAccessible(true)
      fields.foreach { field => field.setAccessible(true) }
      val fieldNames = fields.map { _.getName }
      val fieldNameSet = fieldNames.toSet
      klazzReflectors(klazz) = ParseInfo(fields, nameToIndex, constructor, fieldNames, fieldNameSet)
    }
    val parseInfo = klazzReflectors(klazz)
    val fields = parseInfo.fields
    val fieldNames = parseInfo.fieldNames
    val fieldNameSet = parseInfo.fieldNameSet
    val nameToIndex = parseInfo.nameToIndex
    val constructor = parseInfo.constructor

    // Need to pair up stuff from the JSON object with fields in the case class
    // constructor.
    val inputs: List[Any] = fields.map { field =>
      val name = field.getName
      val desiredType = field.getGenericType
      //Log.i("PARSER", "parsing field: %s".format(name))
      parseType(jsonObject.opt(name), desiredType)
    }

    // Invoke the constructor
    //Log.i("PARSER", "fields: %s".format(fields))
    //Log.i("PARSER", "values: %s".format(values))
    //Log.i("PARSER", "inputs: %s".format(inputs))
    val javaObjects = inputs.map { javaObject(_) }
    //Log.i("PARSER", "java inputs: %s".format(javaObjects))
    val x = constructor.newInstance(javaObjects:_*)

    x.asInstanceOf[T]
  }

  final def serialize[T <: Product](item: T)(implicit mf: Manifest[T]): String = serialize(item, mf.erasure).toString

  private final def toSerializedForm(_type: Type, value: Any): Option[Any] = {
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

  final def serializeList[T](xs: List[T])(implicit mf: Manifest[T]): String = {
    new JSONArray(
      JavaConversions.asJavaCollection(xs.map { item => 
        toSerializedForm(mf.erasure, item) }.flatten)).toString
  }

  private final def serializeTypeToObject(item: Any, field: Field, _type: Type, jsonObject: JSONObject) {
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

  final def serialize[T <: Product](item: T, klazz: Class[_]): JSONObject = {
    val result = new JSONObject
    val fields = klazz.getDeclaredFields.filter { field => !field.getName.contains("$") &&
    !field.getName.startsWith("_")}.toList
    fields.foreach { field =>
      serializeTypeToObject(item, field, field.getGenericType, result)
    }

    result
  }

}
