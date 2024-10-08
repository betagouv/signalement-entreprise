import _root_.controllers._
import actors.InseeTokenActor
import clients.GeoApiClient
import clients.InseeClient
import clients.InseeClientImpl
import config.ApplicationConfiguration
import orchestrators._
import play.api._
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

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import config.SignalConsoConfiguration.HashedTokenReader
import org.apache.pekko.actor.typed
import org.apache.pekko.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import org.apache.pekko.util.Timeout
import org.flywaydb.core.Flyway
class Main extends ApplicationLoader {
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
    with SlickComponents {

  val applicationConfiguration: ApplicationConfiguration = ConfigSource.default.loadOrThrow[ApplicationConfiguration]

  // Run database migration scripts
  Flyway
    .configure()
    .dataSource(
      applicationConfiguration.flyway.jdbcUrl,
      applicationConfiguration.flyway.user,
      applicationConfiguration.flyway.password
    )
    // DATA_LOSS / DESTRUCTIVE / BE AWARE ---- Keep to "false"
    // Be careful when enabling this as it removes the safety net that ensures Flyway does not migrate the wrong database in case of a configuration mistake!
    // This is useful for initial Flyway production deployments on projects with an existing DB.
    // See https://flywaydb.org/documentation/configuration/parameters/baselineOnMigrate for more information
    .baselineOnMigrate(applicationConfiguration.flyway.baselineOnMigrate)
    .load()
    .migrate()

  val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.dbConfig[JdbcProfile](DbName("default"))

  val companyDataRepository: EtablissementRepositoryInterface =
    new EtablissementRepository(dbConfig, applicationConfiguration.app)

  val enterpriseImportInfoRepository: EnterpriseImportInfoRepository = new EnterpriseImportInfoRepository(
    dbConfig
  )

  val inseeClient: InseeClient = new InseeClientImpl(applicationConfiguration.app.inseeToken)

  val geoApiClient = new GeoApiClient()

  val inseeAuthTokenActor: typed.ActorRef[InseeTokenActor.Command] = actorSystem.spawn(
    InseeTokenActor(inseeClient),
    "insee-auth-token-actor"
  )

  implicit val timeout: Timeout = 30.seconds
  val etablissementService =
    new EtablissementImportService(
      inseeAuthTokenActor,
      inseeClient,
      geoApiClient,
      companyDataRepository,
      enterpriseImportInfoRepository,
      applicationConfiguration.app
    )

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = Duration(10, TimeUnit.MINUTES), interval = 1.day) { () =>
    etablissementService.importEtablissements(): Unit
  }: Unit

  val companyOrchestrator = new EtablissementService(
    companyDataRepository
  )

  val companyController = new EtablissementController(
    companyOrchestrator,
    etablissementService,
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
    Seq(new LoggingFilter(), csrfFilter, securityHeadersFilter, allowedHostsFilter, corsFilter)

}
