package loader

import _root_.controllers._
import actors._
import akka.actor.ActorRef
import akka.actor.Props
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import company.EnterpriseImportController
import company.EnterpriseImportOrchestrator
import company.companydata.CompanyDataRepository
import company.companydata.CompanyDataRepositoryInterface
import company.entrepriseimportinfo.EnterpriseImportInfoRepository
import config.ApplicationConfiguration
import config.SignalConsoConfiguration
import orchestrators._
import play.api._
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.DbName
import play.api.db.slick.SlickComponents
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.libs.mailer.MailerComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import pureconfig.ConfigConvert
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.configurable.localTimeConfigConvert
import pureconfig.generic.auto._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SignalConsoApplicationLoader() extends ApplicationLoader {
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
    with AssetsComponents
    with AhcWSComponents
    with SlickComponents
    with SlickEvolutionsComponents
    with EvolutionsComponents
    with MailerComponents {

  applicationEvolutions

  implicit val localTimeInstance: ConfigConvert[LocalTime] = localTimeConfigConvert(DateTimeFormatter.ISO_TIME)
  val csvStringListReader = ConfigReader[String].map(_.split(",").toList)
  implicit val stringListReader = ConfigReader[List[String]].orElse(csvStringListReader)

  val applicationConfiguration: ApplicationConfiguration = ConfigSource.default.loadOrThrow[ApplicationConfiguration]

  //  Repositories

  val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.dbConfig[JdbcProfile](DbName("default"))

  val companyDataRepository: CompanyDataRepositoryInterface = new CompanyDataRepository(dbConfig)

  val enterpriseImportInfoRepository: EnterpriseImportInfoRepository = new EnterpriseImportInfoRepository(
    dbConfig
  )

  val inseeClient: InseeClient = new InseeClientImpl(applicationConfiguration.app.inseeToken)

  val etablissementService = new EtablissementServiceImpl(inseeClient, companyDataRepository)

  etablissementService.importEtablissement()

  val enterpriseSyncActor: ActorRef = actorSystem.actorOf(
    Props(new EnterpriseSyncActor(enterpriseImportInfoRepository, companyDataRepository)),
    "enterprise-sync-actor"
  )

  val enterpriseImportOrchestrator =
    new EnterpriseImportOrchestrator(enterpriseImportInfoRepository, enterpriseSyncActor)

  val companyOrchestrator = new CompanyOrchestrator(
    companyDataRepository
  )

  val companyController = new CompanyController(
    companyOrchestrator,
    controllerComponents
  )

  val enterpriseImportController =
    new EnterpriseImportController(enterpriseImportOrchestrator, controllerComponents)
  io.sentry.Sentry.captureException(
    new Exception("This is a test Alert, used to check that Sentry alert are still active on each new deployments.")
  )

  // Routes
  lazy val router: Router =
    new _root_.router.Routes(
      httpErrorHandler,
      enterpriseImportController,
      companyController
    )

  override def config: Config = ConfigFactory.load()

  override def httpFilters: Seq[EssentialFilter] =
    Seq(csrfFilter, securityHeadersFilter, allowedHostsFilter, corsFilter)

}
