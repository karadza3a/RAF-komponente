package me.akaradzic13.schedule

import java.io.{File, FileInputStream, InputStreamReader}
import java.util.Collections

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.util.{DateTime => GoogleDateTime}
import com.google.api.services.calendar.model.{EventDateTime, Calendar => GoogleCalendarModel, Event => GoogleEventModel}
import com.google.api.services.calendar.{CalendarScopes, Calendar => GoogleCalendar}
import org.joda.time.DateTime

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * Gets credentials for modifying the users calendar and provides methods for adding a new calendar instance as well as
  * populating it with a list of events.
  *
  * Loosely based on https://github.com/google/google-api-java-client-samples
  *
  * @param SECRETS_FILEPATH path to file containing API secrets obtained from Google Cloud Console
  * @param DATA_STORE_DIR   path to directory where user credentials should be stored
  */
class GoogleCalendarGenerator(val SECRETS_FILEPATH: String, val DATA_STORE_DIR: String) {

  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport
  private val dataStoreFactory = new FileDataStoreFactory(new File(DATA_STORE_DIR))
  private var addedCalendar: Try[GoogleCalendarModel] = Failure(new Exception("Calendar not initialized."))

  // lazy eval
  val client: GoogleCalendar = {
    val credentials = authorize
    new GoogleCalendar.Builder(httpTransport, JSON_FACTORY, credentials)
      .setApplicationName("SoftverskeKomponente")
      .build
  }

  /**
    * Authorizes the installed application to access user's protected data
    *
    * @return credentials needed for Google Calendar Builder
    */
  private def authorize = {
    // load client secrets
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(SECRETS_FILEPATH)))
    if (clientSecrets.getDetails.getClientId.startsWith("Enter") || clientSecrets.getDetails.getClientSecret.startsWith("Enter ")) {
      println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar into " + SECRETS_FILEPATH)
      System.exit(1)
    }
    // set up authorization code flow
    val flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory).build
    // authorize
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver).authorize("user")
  }

  /**
    * If calendar is not initialized or the previous call failed, creates and returns a new instance. Otherwise returns
    * the existing calendar instance.
    *
    * Initialization in this context means creating a new calendar in user's account.
    *
    * @return Instance of com.google.api.services.calendar.model.Calendar
    */
  def calendar: Try[GoogleCalendarModel] = {
    val cal = addedCalendar

    cal.recoverWith {
      case _ =>
        val batch: BatchRequest = client.batch

        val callback: JsonBatchCallback[GoogleCalendarModel] = new JsonBatchCallback[GoogleCalendarModel]() {
          def onSuccess(calendar: GoogleCalendarModel, responseHeaders: HttpHeaders): Unit = {
            addedCalendar = Success(calendar)
          }

          def onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders): Unit = {
            addedCalendar = Failure(new Exception(e.getMessage))
          }
        }

        client.calendars.insert(new GoogleCalendarModel().setSummary("Softverske Komponente (TEST)")).queue(batch, callback)

        // blocks until all callbacks have returned
        batch.execute()

        addedCalendar
    }
  }

  implicit def dateTimeToGoogleDateTIme(d: DateTime): EventDateTime = new EventDateTime().setDateTime(new GoogleDateTime(d.getMillis))

  /**
    * Adds all given events to user's calendar.
    *
    * @param events List of events to add
    */
  def addEvents(events: List[CalendarEvent]): Unit = {
    val cal: GoogleCalendarModel = calendar.get

    val batch: BatchRequest = client.batch

    val callback: JsonBatchCallback[GoogleEventModel] = new JsonBatchCallback[GoogleEventModel]() {
      def onSuccess(event: GoogleEventModel, responseHeaders: HttpHeaders): Unit = {}

      def onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders): Unit = throw new Exception(e.getMessage)
    }

    events.foreach { event =>
      client.events.insert(cal.getId,
        new GoogleEventModel()
          .setSummary(event.title)
          .setDescription(event.description)
          .setLocation(event.location)
          .setStart(event.start)
          .setEnd(event.end)
      ).queue(batch, callback)
    }

    // blocks until all callbacks have returned
    batch.execute()
  }
}

