package hitshoot.template

/**
 * Exception to be thrown when a template is referenced but does not exist
 * @param msg The exception message
 * @since 1.0.0
 */
class TemplateNotFoundException(msg: String): Exception(msg)