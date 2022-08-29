package company

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.AbstractController
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class EnterpriseImportController(
    enterpriseSyncOrchestrator: EnterpriseImportOrchestrator,
    controllerComponents: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  val logger: Logger = Logger(this.getClass)

  implicit val timeout: akka.util.Timeout = 5.seconds

  def startEtablissementFile = Action { _ =>
    enterpriseSyncOrchestrator.startEtablissementFile
    Ok
  }

  def startUniteLegaleFile = Action { _ =>
    enterpriseSyncOrchestrator.startUniteLegaleFile
    Ok
  }

  def cancelAllFiles = Action { _ =>
    enterpriseSyncOrchestrator.cancelUniteLegaleFile
    enterpriseSyncOrchestrator.cancelEntrepriseFile
    Ok
  }

  def cancelEtablissementFile = Action { _ =>
    enterpriseSyncOrchestrator.cancelEntrepriseFile
    Ok
  }

  def cancelUniteLegaleFile = Action { _ =>
    logger.info("§§§§§§§§§§§")
    enterpriseSyncOrchestrator.cancelUniteLegaleFile
    Ok
  }

  def getSyncInfo = Action.async { _ =>
    for {
      etablissementImportInfo <- enterpriseSyncOrchestrator.getLastEtablissementImportInfo()
      uniteLegaleInfo <- enterpriseSyncOrchestrator.getUniteLegaleImportInfo()
    } yield Ok(
      Json.obj(
        "etablissementImportInfo" -> etablissementImportInfo,
        "uniteLegaleInfo" -> uniteLegaleInfo
      )
    )
  }
}
