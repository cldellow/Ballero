package com.cldellow.ballero.data

import java.lang.reflect.{Field, Method, Constructor, TypeVariable, ParameterizedType, Type}
import com.cldellow.ballero.service.ParseFuncs
import scala.collection.JavaConversions
import android.util.Log
import org.codehaus.jackson._

/*
 JsonFactory jsonFactory = new JsonFactory(); // or, for data binding, org.codehaus.jackson.mapper.MappingJsonFactory 
  JsonParser jp = jsonFactory.createJsonParser(file); // or URL, Stream, Reader, String, byte[]*/

final object Parser {
  lazy val jsonFactory = new JsonFactory()

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

  final def parseListFromBytes[T](bytes: Array[Byte], size: Int)(implicit mf: Manifest[T]): List[T] = {
    val jsonParser = jsonFactory.createJsonParser(bytes, 0, size)
    parseListFromParser[T](jsonParser)
  }


  final def parseList[T](str: String)(implicit mf: Manifest[T]): List[T] = {
    val jsonParser = jsonFactory.createJsonParser(str)
    parseListFromParser[T](jsonParser)
  }

  private final def parseListFromParser[T](jsonParser: JsonParser)(implicit mf: Manifest[T]): List[T] = {
    // consume the START_ARRAY token
    val startObject = jsonParser.nextToken()
    require(startObject == JsonToken.START_ARRAY, "expected start array, got %s".format(jsonParser.getCurrentToken))
    val rv = parseArray(jsonParser, mf.erasure).asInstanceOf[List[T]]
    //Log.i("PARSER", "parseArray parse took %s".format(System.currentTimeMillis - newtm))
    jsonParser.close()
    rv
  }

  final def helperParseAsList[T <: Product](implicit mf: Manifest[T]): ParseFuncs[T] =
    ParseFuncs[T](parseAsList[T] _, parseAsListFromBytes[T] _)

  final def parseAsList[T <: Product](str: String)(implicit mf: Manifest[T]): List[T] = {
    //Log.i("PARSER", "trying to parse %s from %s".format(mf.erasure.getName, str))
    List(parse[T](str))
  }

  final def parseAsListFromBytes[T <: Product](bytes: Array[Byte], size: Int)(implicit mf: Manifest[T]): List[T] = {
    //Log.i("PARSER", "trying to parse %s from %s".format(mf.erasure.getName, str))
    List(parseFromBytes[T](bytes, size))
  }

  final def parseFromBytes[T <: Product](bytes: Array[Byte], size: Int)(implicit mf: Manifest[T]): T = {
    //sanityCheck(mf.erasure)
    val jsonParser = jsonFactory.createJsonParser(bytes, 0, size)
    parseFromParser[T](jsonParser)
  }


  final def parse[T <: Product](str: String)(implicit mf: Manifest[T]): T = {
    //sanityCheck(mf.erasure)
    val jsonParser = jsonFactory.createJsonParser(str)
    parseFromParser[T](jsonParser)
  }

  private final def parseFromParser[T <: Product](jsonParser: JsonParser)(implicit mf: Manifest[T]): T = {
    val tm = System.currentTimeMillis
    // consume the START_OBJECT token
    val startObject = jsonParser.nextToken()
    require(startObject == JsonToken.START_OBJECT, "expected start object")

    //Log.i("PARSER", "parse parse took %s".format(System.currentTimeMillis - tm))

    val rv = parse[T](jsonParser, mf.erasure)
    jsonParser.close()
    rv
  }

  private final def parseOption(parser: JsonParser, desiredType: Type): Option[_] = {
    if(parser.getCurrentToken == JsonToken.VALUE_NULL)
      None
    else
      Some(parseType(parser, desiredType))
  }

  private final def parseArray(jsonParser: JsonParser, desiredType: Type): List[_] = {
    val listBuffer = new collection.mutable.ListBuffer[Any]

    while(jsonParser.nextToken != JsonToken.END_ARRAY) {
      listBuffer += parseType(jsonParser, desiredType)
    }
    listBuffer.toList
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

  private final def parseType(jsonParser: JsonParser, desiredType: Type): Any = desiredType match {
    case desiredType: Class[_] =>
      //Log.i("PARSER", "attempting to parse %s".format(value))
      desiredType.getName match {
        case "int"|"java.lang.Integer" => jsonParser.getNumberValue().intValue()
        case "boolean" => 
          jsonParser.getCurrentToken == JsonToken.VALUE_TRUE
        case "java.lang.String" => jsonParser.getText()
        case "scala.math.BigDecimal" => BigDecimal(jsonParser.getNumberValue().doubleValue())
        case x if instanceOf(desiredType, classOf[Product]) =>
          parse(jsonParser, desiredType)
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
          parseArray(jsonParser, actualTypes.head)
        case "scala.Option" =>
          parseOption(jsonParser, actualTypes.head)
      }
    case x => 
      println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
      println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
      error("unknown type: %s".format(x))
  }

  private final def javaObject(x: Any): Object = x.asInstanceOf[Object]


  case class ParseInfo(
    fields: List[Field],
    nameToIndex: Map[String, Int],
    constructor: Constructor[_],
    fieldNames: List[String],
    fieldNameSet: Set[String],
    fieldMap: Map[String, Field])
  val klazzReflectors: collection.mutable.ConcurrentMap[Class[_], ParseInfo] = 
    new java.util.concurrent.ConcurrentHashMap[Class[_], ParseInfo]


  final def parse[T <: Product](jsonParser: JsonParser, klazz: Class[_]): T = {
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
      val fieldMap: Map[String, Field] = Map() ++ fields.map { f => f.getName -> f }
      klazzReflectors(klazz) = ParseInfo(fields, nameToIndex, constructor, fieldNames, fieldNameSet, fieldMap)
    }
    val parseInfo = klazzReflectors(klazz)
    val fields = parseInfo.fields
    val fieldNames = parseInfo.fieldNames
    val fieldNameSet = parseInfo.fieldNameSet
    val nameToIndex = parseInfo.nameToIndex
    val constructor = parseInfo.constructor
    val fieldMap = parseInfo.fieldMap

    // Need to pair up stuff from the JSON object with fields in the case class
    // constructor.
    val map: collection.mutable.Map[String, Any] = collection.mutable.Map()
    while(jsonParser.nextToken() != JsonToken.END_OBJECT) {
      /*
      val inputs: List[Any] = fields.map { field =>
        val name = field.getName
        val desiredType = field.getGenericType
        //Log.i("PARSER", "parsing field: %s".format(name))
        parseType(jsonParser.opt(name), desiredType)
      }
      */
      val fieldName = jsonParser.getCurrentName()
      jsonParser.nextToken()
      // do we recognize this field?
      if(fieldNames.contains(fieldName)) {
        //println("known field: %s".format(fieldName))
        map(fieldName) = parseType(jsonParser, fieldMap(fieldName).getGenericType)
      } else {
        // Unknown field, skip it
        //println("unknown field: %s".format(fieldName))
        jsonParser.skipChildren()
      }
    }

    val inputs = fieldNames.map { name => map.getOrElse(name, None) }

    // Invoke the constructor
    //Log.i("PARSER", "fields: %s".format(fields))
    //Log.i("PARSER", "values: %s".format(values))
    //Log.i("PARSER", "inputs: %s".format(inputs))
    val javaObjects = inputs.map { javaObject(_) }
    //Log.i("PARSER", "java inputs: %s".format(javaObjects))
    val x = constructor.newInstance(javaObjects:_*)

    x.asInstanceOf[T]
  }

  final def serialize[T <: Product](item: T)(implicit mf: Manifest[T]): String = {
    val sw = new java.io.StringWriter()
    val generator = jsonFactory.createJsonGenerator(sw)
    serialize(generator, item, mf.erasure)
    generator.close()
    sw.toString
  }

  private final def toSerializedForm(gen: JsonGenerator, _type: Type, value: Any) {
    _type match {
      case desiredType: Class[_] =>
        Some(desiredType.getName match {
          case "int"|"java.lang.Integer" => gen.writeNumber(value.asInstanceOf[Int])
          case "boolean" => gen.writeBoolean(value.asInstanceOf[Boolean])
          case "java.lang.String" => gen.writeString(value.asInstanceOf[String])
          case "scala.math.BigDecimal" => gen.writeNumber(value.asInstanceOf[BigDecimal].toDouble)
          case x if instanceOf(desiredType, classOf[Product]) =>
            serialize(gen, value.asInstanceOf[Product], desiredType)
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
            gen.writeStartArray
            listValue.asInstanceOf[List[_]].foreach { item =>
              toSerializedForm(gen, actualTypes.head, item)
            }
            gen.writeEndArray
          case "scala.Option" =>
            val optionalValue = value
            if(optionalValue != None) {
              val realValue = optionalValue.asInstanceOf[Option[_]].get
              toSerializedForm(gen, actualTypes.head, realValue)
            } else {
              gen.writeNull()
            }
        }
      case x => error("unknown type: %s".format(x))
    }
  }

  final def serializeList[T](xs: List[T])(implicit mf: Manifest[T]): String = {
    val sw = new java.io.StringWriter()
    val gen = jsonFactory.createJsonGenerator(sw)

    gen.writeStartArray()
    xs.foreach { item => 
      toSerializedForm(gen, mf.erasure, item)
    }
    gen.writeEndArray()

    gen.close()
    sw.toString
  }

  private final def serializeTypeToObject(gen: JsonGenerator, item: Any, field: Field, _type: Type) {
    // see http://stackoverflow.com/questions/6756442/scala-class-declared-fields-and-access-modifiers
    field.setAccessible(true)
    val name = field.getName
    gen.writeFieldName(name)
    _type match {
      case desiredType: Class[_] =>
        toSerializedForm(gen, _type, field.get(item))
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
            gen.writeStartArray()
            listValue.asInstanceOf[List[_]].foreach { item =>
              toSerializedForm(gen, actualTypes.head, item)
            }
            gen.writeEndArray()
          case "scala.Option" =>
            val optionalValue = field.get(item)
            if(optionalValue != None) {
              val realValue = optionalValue.asInstanceOf[Option[_]].get
              toSerializedForm(gen, actualTypes.head, realValue)
            } else {
              gen.writeNull()
            }
        }
      case x => 
        println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
        println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
        error("unknown type: %s".format(x))
      }
  }

  final def serialize[T <: Product](gen: JsonGenerator, item: T, klazz: Class[_]) {
    gen.writeStartObject
    val fields = klazz.getDeclaredFields.filter { field => !field.getName.contains("$") &&
    !field.getName.startsWith("_")}.toList
    fields.foreach { field =>
      serializeTypeToObject(gen, item, field, field.getGenericType)
    }
    gen.writeEndObject
  }

}
