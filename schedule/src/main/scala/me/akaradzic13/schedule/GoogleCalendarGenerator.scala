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
import com.google.api.services.calendar.model.{Calendar => CalendarModel}
import com.google.api.services.calendar.{CalendarScopes, Calendar => GoogleCalendar}

import scala.util.{Failure, Success, Try}

/**
  * Loosely based on https://github.com/google/google-api-java-client-samples
  */
class GoogleCalendarGenerator(val SECRETS_FILEPATH: String = "/Users/andrejk/Downloads/client_secrets.json",
                              val DATA_STORE_DIR: String = "/Users/andrejk/IdeaProjects/komponente/schedule/store"
                             ) {

  private val JSON_FACTORY = JacksonFactory.getDefaultInstance
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport
  private val dataStoreFactory = new FileDataStoreFactory(new File(DATA_STORE_DIR))
  private var addedCalendar: Try[CalendarModel] = Failure(new Exception("Calendar not initialized."))

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
      println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar " + "into calendar-cmdline-sample/src/main/resources/client_secrets.json")
      System.exit(1)
    }
    // set up authorization code flow
    val flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory).build
    // authorize
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver).authorize("user")
  }

  def calendar: Try[CalendarModel] = {
    val cal = addedCalendar

    // if calendar is not initialized or the previous call failed, create and return an instance
    cal.recoverWith {
      case _ =>
        val batch: BatchRequest = client.batch

        val callback: JsonBatchCallback[CalendarModel] = new JsonBatchCallback[CalendarModel]() {
          def onSuccess(calendar: CalendarModel, responseHeaders: HttpHeaders): Unit = {
            addedCalendar = Success(calendar)
          }

          def onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders): Unit = {
            Failure(new Exception(e.getMessage))
          }
        }

        client.calendars.insert(new CalendarModel().setSummary("Softverske Komponente (TEST)")).queue(batch, callback)

        // blocks until all callbacks have returned
        batch.execute()

        addedCalendar
    }
  }

}

