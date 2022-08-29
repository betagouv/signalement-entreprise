package controllers.error

sealed trait AppError extends Throwable with Product with Serializable

case class InseeTokenGenerationError(message: String) extends AppError
case class InseeEtablissementError(message: String) extends AppError

sealed trait ApiError extends AppError {
  val `type`: String
  val title: String
  val details: String
}

sealed trait UnauthorizedError extends ApiError
sealed trait NotFoundError extends ApiError
sealed trait BadRequestError extends ApiError
sealed trait ForbiddenError extends ApiError
sealed trait ConflictError extends ApiError
sealed trait InternalAppError extends ApiError
sealed trait PreconditionError extends ApiError

object ApiError {

  final case class ServerError(message: String, cause: Option[Throwable] = None) extends InternalAppError {
    override val `type`: String = "SC-0001"
    override val title: String = message
    override val details: String = "Une erreur inattendue s'est produite."
  }

  final case object MalformedBody extends BadRequestError {
    override val `type`: String = "SC-0014"
    override val title: String = "Malformed request body"
    override val details: String = s"Le corps de la requête ne correspond pas à ce qui est attendu par l'API."
  }

  final case class MalformedSIRET(InvalidSIRET: String) extends BadRequestError {
    override val `type`: String = "SC-0034"
    override val title: String = "Malformed SIRET"
    override val details: String =
      s"Malformed SIRET : $InvalidSIRET"
  }

  final case class MalformedId(id: String) extends BadRequestError {
    override val `type`: String = "SC-0031"
    override val title: String = "Malformed id"
    override val details: String =
      s"Malformed id : $id"
  }

  final case class MalformedValue(value: String, expectedValidType: String) extends BadRequestError {
    override val `type`: String = "SC-0042"
    override val title: String = s"Malformed value, $value is not a valid value, expecting valid $expectedValidType"
    override val details: String =
      s"La valeur $value ne correspond pas à ce qui est attendu par l'API. Merci de renseigner une valeur valide pour $expectedValidType"
  }

}
