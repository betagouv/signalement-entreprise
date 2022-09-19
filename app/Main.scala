import _root_.controllers._

import config.ApplicationConfiguration
import orchestrators._
import play.api._
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.DbName
import play.api.db.slick.SlickComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import repositories.entrepriseimportinfo.EnterpriseImportInfoRepository
import repositories.insee.EtablissementRepository
import repositories.insee.EtablissementRepositoryInterface
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import Token.ClearToken
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import config.SignalConsoConfiguration.HashedTokenReader
class Main() extends ApplicationLoader {
  var components: SignalConsoComponents = _

  override def load(context: ApplicationLoader.Context): Application = {
    components = new SignalConsoComponents(context)
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    components.application
  }
}

class SignalConsoComponents(
    context: ApplicationLoader.Context
) extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents
    with play.filters.cors.CORSComponents
    with AhcWSComponents
    with SlickComponents
    with SlickEvolutionsComponents
    with EvolutionsComponents {

  applicationEvolutions

  val applicationConfiguration: ApplicationConfiguration = ConfigSource.default.loadOrThrow[ApplicationConfiguration]

  val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.dbConfig[JdbcProfile](DbName("default"))

  val companyDataRepository: EtablissementRepositoryInterface = new EtablissementRepository(dbConfig)

  val enterpriseImportInfoRepository: EnterpriseImportInfoRepository = new EnterpriseImportInfoRepository(
    dbConfig
  )

  val inseeClient: InseeClient = new InseeClientImpl(applicationConfiguration.app.inseeToken)

  val etablissementService =
    new EtablissementImportService(inseeClient, companyDataRepository, enterpriseImportInfoRepository)

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = Duration(10, TimeUnit.MINUTES), interval = 1.day) { () =>
    etablissementService.importEtablissement()
    ()
  }

  val companyOrchestrator = new EtablissementService(
    companyDataRepository
  )

  val companyController = new EtablissementController(
    companyOrchestrator,
    controllerComponents,
    applicationConfiguration.app.apiAuthenticationToken
  )

  io.sentry.Sentry.captureException(
    new Exception("This is a test Alert, used to check that Sentry alert are still active on each new deployments.")
  )

  // Routes
  lazy val router: Router =
    new _root_.router.Routes(
      httpErrorHandler,
      companyController
    )

  override def httpFilters: Seq[EssentialFilter] =
    Seq(csrfFilter, securityHeadersFilter, allowedHostsFilter, corsFilter)

}
