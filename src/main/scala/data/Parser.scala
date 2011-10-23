package cldellow.ballero.data

import java.lang.reflect._
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

  def parse[T <: Product](str: String)(implicit mf: Manifest[T]): T = {
    sanityCheck(mf.erasure)
    val jsonObject = new JSONObject(str)
    parse(jsonObject, mf.erasure)
  }

  private def parseOption(jsonObject: JSONObject, name: String, desiredType: Class[_]): Option[_] =
    if(jsonObject has name)
      Some(parseTypeFromObject(jsonObject, name, desiredType))
    else
      None

  private def parseArray(jsonArray: JSONArray, desiredType: Class[_]): List[_] =
    if(jsonArray.length == 0)
      Nil
    else {
      val listBuffer = new collection.mutable.ListBuffer[Any]
      for(i <- 0 until jsonArray.length) {
        listBuffer += (desiredType.getCanonicalName match {
          case "int" => jsonArray.getInt(i)
          case "java.lang.String" => jsonArray.getString(i)
          case "scala.math.BigDecimal" => BigDecimal(jsonArray.getDouble(i))
          case x if instanceOf(desiredType, classOf[Product]) =>
            parse(jsonArray.getJSONObject(i), desiredType)
          case x => error("can't parse %s from array".format(desiredType))
        })
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
      .map { actualInstanceOf(_, target) }.reduceLeft { _ || _ }

  private def parseTypeFromObject(jsonObject: JSONObject, name: String, desiredType: Type): Any = desiredType match {
    case desiredType: Class[_] =>
      desiredType.getName match {
        case "int" => jsonObject.getInt(name)
        case "java.lang.String" => jsonObject.getString(name)
        case "scala.math.BigDecimal" => BigDecimal(jsonObject.getDouble(name))
        case x if instanceOf(desiredType, classOf[Product]) =>
          parse(jsonObject.getJSONObject(name), desiredType)
        case x => error("unknown class: %s (%s)"
          .format(x,
            desiredType
          ))
      }
    case desiredType: ParameterizedType =>
      // OK, desiredType.getActualTypeArguments gives Class... now, how do we know
      // what the outer one is?
      require(desiredType.getRawType.isInstanceOf[Class[_]], "rawType is not an instance of Class[T]")
      require(desiredType.getActualTypeArguments.forall { arg => arg.isInstanceOf[Class[_]] },
        "one of the actualTypeArguments is not an instance of Class[T]")
      val rawType = desiredType.getRawType.asInstanceOf[Class[_]]
      val actualTypes = desiredType.getActualTypeArguments.collect { case x: Class[_] => x }
      rawType.getName match {
        case "scala.collection.immutable.List" =>
          val array = jsonObject.getJSONArray(name)
          // Thanks for special casing everything, jerkwads.
          parseArray(array, actualTypes.head)
        case "scala.Option" =>
          parseOption(jsonObject, name, actualTypes.head)
      }
    case x => 
      println("x is TypeVar: %s".format(x.isInstanceOf[TypeVariable[_]]))
      println("x is ParamType: %s".format(x.isInstanceOf[ParameterizedType]))
      error("unknown type: %s".format(x))
  }

  private def javaObject(x: Any): Object = x.asInstanceOf[Object]

  def parse[T <: Product](jsonObject: JSONObject, klazz: Class[_]): T = {
    val constructors = klazz.getConstructors

    val fields = klazz.getDeclaredFields.filter { !_.getName.startsWith("$")}.toList

    /** Default arguments -- totally non-supported, but hey, it works. What could go wrong? */
    val defaultArgument = "apply$default"
    val methods = klazz.getDeclaredMethods.filter { method =>
      method.getParameterTypes.length == 0 && method.getName.contains(defaultArgument)
    }.toList

    val nameToIndex = Map() ++ fields.map { _.getName }.zipWithIndex

    val defaultValues = Map() ++ methods.map { method =>
      val index = (method.getName.reverse.takeWhile { _ != '$' }.reverse.toInt) - 1
      fields(index).getName -> method
    }

    val constructor = constructors.head

    // Need to pair up stuff from the JSON object with fields in the case class
    // constructor.
    val values: Map[String, Any] = Map() ++ fields.flatMap { field =>
      val name = field.getName
      if(jsonObject has name) {
        val desiredType = field.getGenericType// :: higherKinds
        List(name -> parseTypeFromObject(jsonObject, name, desiredType))
      } else if(defaultValues contains name) {
        // get the default value
        List(name -> defaultValues(name).invoke(null))
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
    //val x = ParserHelper.construct(constructor, javaObjects.toArray[Object])
    val x = constructor.newInstance(javaObjects:_*)
    x.asInstanceOf[T]
  }
}

// vim: set ts=2 sw=2 et:
