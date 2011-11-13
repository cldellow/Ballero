package com.cldellow.ballero.ui

import com.cldellow.ballero.R
import greendroid.widget.ActionBarItem.Type
import com.cldellow.ballero.service._
import com.cldellow.ballero.data._
import android.graphics._
import scala.collection.JavaConversions._
import android.app._
import android.content._
import android.location._
import android.os.Bundle
import android.util.Log
import android.text._
import android.view._
import android.widget._
import greendroid.app._
import se.fnord.android.layout._
import greendroid.widget._
import greendroid.widget.item._
import android.net._
import java.io._

class ProjectDetailsActivity extends ProjectishActivity {
  val TAG = "ProjectDetailsActivity"
  val isProject = true
  val patternId = None

  var cachedProject: Option[Project] = None

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    btnTakePhoto.setVisibility(View.GONE)
  }

  override def onHandleActionBarItemClick(item: ActionBarItem, position: Int): Boolean =
    item.getItemId match {
      case R.id.action_bar_refresh =>
        fetch(ForceNetwork)
        true
      case _ =>
        true
    }

  var pending = 0

  def onProjectLoaded(projects: List[Project], delta: Int) {
    pending += delta
    if(pending <= 0) {
      pending = 0
      refreshButton.setLoading(false)
    }

    btnEditNotes.setVisibility(View.VISIBLE)
    yarnRequirementsLayout.setVisibility(View.GONE)
    myYarnLayout.setVisibility(View.GONE)
    projects.headOption.map { project =>
      cachedProject = Some(project)
      setTitle(project.uiName)
      patternName.setVisibility(View.GONE)

      project.pattern_name.foreach { name =>
        patternName.setVisibility(View.VISIBLE)
        patternName.setText(name)
      }

      val imageUrls = project.photos.map { photos =>
        photos.map { photo =>
          photo.square_url.getOrElse(photo.thumbnail_url)
        }
      }.getOrElse(Nil)

      if(imageUrls.length > 0) {
        val imageAdapter = new AsyncImageViewAdapter(this, imageUrls.toArray)
        gallery.setAdapter(imageAdapter)
        gallery.setSelection(imageUrls.length / 2)
        gallery.setVisibility(View.VISIBLE)
      } else {
        gallery.setVisibility(View.GONE)
      }

      status.setText(project.status.human)

      // TODO: set default happiness to grey unknown
      project.rating.foreach { happiness =>
        val img = happiness match {
          case 0 => getResources.getDrawable(R.drawable.rating0)
          case 1 => getResources.getDrawable(R.drawable.rating1)
          case 2 => getResources.getDrawable(R.drawable.rating2)
          case 3 => getResources.getDrawable(R.drawable.rating3)
          case 4 => getResources.getDrawable(R.drawable.rating4)
        }
        imageViewHappiness.setImageDrawable(img)
      }

      progressBar.setVisibility(View.VISIBLE)
      progressBar.setMax(100)
      progressBar.setProgress(0)

      if(project.progress.isDefined) {
        progressBar.setProgress(project.progress.get)
      }

      var notes = project.notes.getOrElse("")
      if(notes.trim=="")
        notesValue.setText("(no notes)")
      else
        notesValue.setText(notes)

      var madeFor = project.made_for.getOrElse("")
      if(madeFor.trim == "")
        madeFor = "unknown"

      madeForValue.setText(madeFor)

      yarnLayout.setVisibility(View.GONE)
      val adapter: ItemAdapter = new ItemAdapter(this)
      project.packs.foreach { packs =>

        packs.foreach { pack =>
          yarnLayout.setVisibility(View.VISIBLE)
          val title = pack.yarn.flatMap { _.yarn_company_name }.getOrElse("Unknown brand")
          val subtitle = pack.yarn.flatMap { _.name }.getOrElse("Unknown yarn")
          val subtitle2 = List[Option[String]](
            pack.colorway, pack.skeins.map { x => "%s skeins".format(if(x == x.toInt) x.toInt else x) },
            pack.total_grams.map { x => "%s g".format(x) },
            pack.total_yards.map { x => "%s yards".format(x.toInt) }).flatten.filter { _ != "" }.mkString(", ")

          adapter.add(new SubtitleItem2(title, subtitle, subtitle2))
        }
      }
      listViewYarn.setAdapter(adapter)
      listViewYarn.invalidate


      lblCompletedOnValue.setText("unknown")
      project.completed.map { c => lblCompletedOnValue.setText(c) }

      lblStartedOnValue.setText("unknown")
      project.started.map { c => lblStartedOnValue.setText(c) }
      needleLayout.setVisibility(View.GONE)
      project.needle_sizes.foreach { needle_sizes =>
        val needles = needle_sizes.map { needle =>
          needle.name
        }.flatten.mkString("\n")
        if(needles != "") {
          needleLayout.setVisibility(View.VISIBLE)
          needleDetails.setText(needles)
        }
      }

      tagsLayout.setVisibility(View.GONE)
      tagsContentLayout.removeAllViews()
      project.tag_names.foreach { tag_names =>
        tag_names.foreach { tag_name =>
          tagsLayout.setVisibility(View.VISIBLE)
          val textView = new TextView(this)
          textView.setVisibility(View.VISIBLE)
          textView.setText(tag_name)
          textView.setBackgroundColor(Color.LTGRAY)
          textView.setPadding(10,2,10,2)
          textView.setTextColor(Color.BLACK)
          textView.setSingleLine(true)
          tagsContentLayout.addView(textView, new PredicateLayout.LayoutParams(10,4))
        }
      }
    }
    progressBarLoading.setVisibility(View.GONE)
    linearLayout.setVisibility(View.VISIBLE)
  }

  def sign[T <: Product](request: RestRequest[T])(implicit mf: Manifest[T]): RestRequest[T] = {
    val oauthCred = Data.currentUser.get.oauth_token.get
    val token = oauthCred.auth_token
    val secret = oauthCred.signing_key

    RestRequest[T](
      request.url,
      request.verb,
      Crypto.signParams(
        request.url,
        request.params,
        token,
        secret),
      parseFunc = Parser.helperParseAsList[T])
  }

  override def startedOnClick(v: View) {
    showDialog(DATE_DIALOG_STARTED)
  }

  override def completedOnClick(v: View) {
    showDialog(DATE_DIALOG_COMPLETED)
  }

  def btnEditNotesClick(v: View) {
    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    builder.setTitle("Notes")
    val input: EditText = new EditText(this)
    input.setSingleLine(false)
    cachedProject.map { _.notes.map { input.setText(_) } }
    builder.setView(input);
    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, item: Int) {
        restServiceConnection.request(
          sign(RestRequest[ProjectWrapper](
            "http://api.ravelry.com/projects/%s/%s.json"
              .format(Data.currentUser.get.name, currentId),
            POST,
            Map("project.notes" -> input.getText().toString),
            parseFunc = Parser.helperParseAsList[ProjectWrapper]))) { response =>
          response.parsedVals.map { project =>
            saveAndUpdate(project)
          }
        }
        toast("Updating Ravelry...")
      }
    });
    val alert: AlertDialog = builder.create()
    alert.show()
  }


  override def madeForClick(v: View) {
    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    builder.setTitle("Made for")
    val input: EditText = new EditText(this)
    cachedProject.map { _.made_for.map { input.setText(_) } }
    builder.setView(input);
    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, item: Int) {
        restServiceConnection.request(
          sign(RestRequest[ProjectWrapper](
            "http://api.ravelry.com/projects/%s/%s.json"
              .format(Data.currentUser.get.name, currentId),
            POST,
            Map("project.made_for" -> input.getText().toString),
            parseFunc = Parser.helperParseAsList[ProjectWrapper]))) { response =>
          response.parsedVals.map { project =>
            saveAndUpdate(project)
          }
        }
        toast("Updating Ravelry...")
      }
    });
    val alert: AlertDialog = builder.create()
    alert.show()
  }


  final val DATE_DIALOG_STARTED = 0
  final val DATE_DIALOG_COMPLETED = 1

  case class RavDate(year: Int, month: Int, day: Int)

  def parseDate(str: Option[String]): RavDate = {
    val dateRe = "([0-9]{1,4})/([0-9]{1,2})/([0-9]{1,2})".r
    val rv = str.flatMap { str =>
      str match {
        case dateRe(year, month, day) => Some(RavDate(year.toInt, month.toInt, day.toInt))
        case _ => None
      }
    }

    rv.getOrElse {
      val d = new java.util.Date()
      RavDate(d.getYear() + 1900, d.getMonth() + 1, d.getDate())
    }
  }

  override def onCreateDialog(id: Int): Dialog = id match {
    case DATE_DIALOG_STARTED =>
      val date = parseDate(cachedProject.flatMap { _.started })
      new DatePickerDialog(this, setDate("started"), date.year, date.month - 1, date.day)
    case DATE_DIALOG_COMPLETED =>
      val date = parseDate(cachedProject.flatMap { _.completed })
      new DatePickerDialog(this, setDate("completed"), date.year, date.month - 1, date.day)
    case _ => null
  }

  def setDate(field: String): DatePickerDialog.OnDateSetListener =
    new DatePickerDialog.OnDateSetListener() {
      def onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        restServiceConnection.request(
          sign(RestRequest[ProjectWrapper](
            "http://api.ravelry.com/projects/%s/%s.json"
              .format(Data.currentUser.get.name, currentId),
            POST,
            Map("project.%s".format(field) -> "%s/%s/%s".format(year, month + 1, day)),
            parseFunc = Parser.helperParseAsList[ProjectWrapper]))) { response =>
          response.parsedVals.map { project =>
            saveAndUpdate(project)
          }
        }
        toast("Updating Ravelry...")
      }
    }

  def happinessClick(v: View) {
    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    builder.setTitle("Rate this project")

    val adapter = new ItemAdapter(this)
    val items = List(
      R.drawable.rating0 -> "ugh",
      R.drawable.rating1 -> "meh",
      R.drawable.rating2 -> "it's ok",
      R.drawable.rating3 -> "like it",
      R.drawable.rating4 -> "love it")
    items.foreach { item =>
      val thumb = new ThumbnailItem(item._2, "", item._1)
      thumb.viewId = R.layout.black_thumbnail_item_view
      adapter.add(thumb)
    }
    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, item: Int) {
        restServiceConnection.request(
          sign(RestRequest[ProjectWrapper](
            "http://api.ravelry.com/projects/%s/%s.json"
              .format(Data.currentUser.get.name, currentId),
            POST,
            Map("project.rating" -> item.toString),
            parseFunc = Parser.helperParseAsList[ProjectWrapper]))) { response =>
          response.parsedVals.map { project =>
            saveAndUpdate(project)
          }
        }
        toast("Updating Ravelry...")
      }
    });
    val alert: AlertDialog = builder.create()
    alert.show()
  }

  def saveAndUpdate(project: ProjectWrapper) {
    onProjectLoaded(List(project.project), 0)
    Data.save("project_%s".format(project.project.id), Parser.serializeList[ProjectWrapper](List(project)))
    toast("Saved.")
  }

  def progressClick(v: View) {
    val items = ((0 to 20) map { x => "%s%%".format(x * 5).asInstanceOf[CharSequence]}).toArray

    val builder: AlertDialog.Builder = new AlertDialog.Builder(this)
    builder.setTitle("Update progress")
    builder.setItems(items, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, item: Int) {
        restServiceConnection.request(
          sign(RestRequest[ProjectWrapper](
            "http://api.ravelry.com/projects/%s/%s.json"
              .format(Data.currentUser.get.name, currentId),
            POST,
            Map("project.progress" -> items(item).toString.replace("%", "")),
            parseFunc = Parser.helperParseAsList[ProjectWrapper]))) { response =>
          response.parsedVals.map { project =>
            saveAndUpdate(project)
          }
        }
        toast("Updating Ravelry...")
      }
    });
    val alert: AlertDialog = builder.create()
    alert.show()
  }

  override def onResume() {
    super.onResume()
    progressBarLoading.setVisibility(View.VISIBLE)
    linearLayout.setVisibility(View.GONE)
    fetch(FetchIfNeeded)
  }
  override def btnTakePhotoClick(v: View) {
    val intent: Intent = new Intent("android.media.action.IMAGE_CAPTURE");
    startActivityForResult(intent, 0);
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode == Activity.RESULT_OK && requestCode == 0) {
      val result: String = data.toURI()
      info("got result %s".format(result))
      val imageUri: Uri = data.getData()
      info("got uri:%s".format(imageUri))
      val inStream: InputStream = getContentResolver().openInputStream(imageUri)
      val image: Bitmap = BitmapFactory.decodeStream(inStream)
      info("bitmap %s".format(image))
      val width = image.getWidth
      val height = image.getHeight
      info("bitmap width=%s, height=%s".format(width, height))

      // Create a scaled copy that is 1024 px on its longest edge
      val scaleFactor: Double = (width.max(height) / 1024.0).max(1.0)
      val (newWidth, newHeight) = ((width / scaleFactor).toInt, (height / scaleFactor).toInt)
      val scaled = Bitmap.createScaledBitmap(image, newWidth, newHeight, false)
      val byteArrayOutputStream = new java.io.ByteArrayOutputStream(512 * 1024)
      scaled.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)

      val bytes = byteArrayOutputStream.toByteArray()
      info("new length %s".format(bytes.length))

    }
  }


  def fetch(policy: RefreshPolicy) {
    pending += 2
    refreshButton.setLoading(true)
    RavelryApi.makeProjectDetailsResource(currentId).render(policy, onProjectLoaded)
  }
}
