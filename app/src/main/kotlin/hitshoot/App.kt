package hitshoot

import hitshoot.App.apiClient
import hitshoot.App.arguments
import hitshoot.App.configManager
import hitshoot.App.logger
import hitshoot.App.templateManager
import hitshoot.App.vertx
import hitshoot.App.vertxInstance
import hitshoot.api.ApiClient
import hitshoot.config.ConfigException
import hitshoot.config.ConfigManager
import hitshoot.route.setupControllerRoutes
import hitshoot.route.setupMiddlewareRoutes
import hitshoot.route.setupSpecialRoutes
import hitshoot.route.setupStaticRoutes
import hitshoot.template.TemplateManager
import hitshoot.util.appDir
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/**
 * Instance data and utilities
 * @since 1.0.0
 */
object App {
    /**
     * Instance logger
     * @since 1.0.0
     */
    val logger: Logger = LoggerFactory.getLogger(App::class.java)

    /**
     * Instance configuration manager
     * @since 1.0.0
     */
    val configManager = ConfigManager()

    /**
     * Instance page template manager
     * @since 1.0.0
     */
    val templateManager = TemplateManager()

    /**
     * The arguments passed to the application at runtime
     * @since 1.0.0
     */
    var arguments: Namespace = object: Namespace(null) {}

    /**
     * Instance API client
     * @since 1.0.0
     */
    val apiClient = ApiClient()

    var vertxInstance: Vertx? = null

    /**
     * Vert.x instance
     * @since 1.0.0
     */
    val vertx: Vertx
        get() = vertxInstance!!
}

// Application entry point
@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>) {
    try {
        // Setup argument parser
        val parser = ArgumentParsers.newFor(Constants.APP_NAME).build()
                .version(Constants.VERSION)
                .defaultHelp(true)
                .description("Runs the ${Constants.APP_NAME} server")
                .epilog("Running ${Constants.APP_NAME} v${Constants.VERSION} (version integer: ${Constants.VERSION_INT})")

        // Define arguments
        parser.addArgument("-c", "--config")
                .nargs(1)
                .dest("config file path")
                .setDefault(listOf(appDir() + "/config.yml"))
                .help("Sets the path of the configuration file to read")
        parser.addArgument("-r", "--recreate-config")
                .nargs("?")
                .setConst(true)
                .setDefault(false)
                .type(Boolean::class.java)
                .dest("recreate config")
                .help("Recreates the configuration file, replacing it with the default")

        try {
            // Parse arguments
            arguments = parser.parseArgs(args)
        } catch(e: ArgumentParserException) {
            parser.handleError(e)
            exitProcess(1)
        }

        // Figure out config file path
        val path = arguments.getList<String>("config file path")[0]

        // Check for --recreate-config
        if(arguments.getBoolean("recreate config")) {
            logger.info("(Re-)creating config at location \"$path\"...")
            configManager.writeDefaultConfig(path)
            logger.info("Finished")
            return
        }

        logger.info("Loading config...")
        try {
            configManager.initialize(path)
        } catch(e: ConfigException) {
            logger.error("Failed to load config: ${e.message}")
        }
        val config = configManager.config

        // Setup Vert.x
        vertxInstance = Vertx.vertx(VertxOptions()
                .setWorkerPoolSize(config.server.workerThreads))

        logger.info("Loading templates...")
        templateManager.load()

        logger.info("Initializing API client...")
        runBlocking(vertx.dispatcher()) {
            apiClient.init()
        }

        logger.info("Starting server...")

        // Setup web server on context
        runBlocking(vertx.dispatcher()) {
            // Create server
            val server = vertx.createHttpServer()

            // Create router
            val router = Router.router(vertx)

            // Register routes
            setupMiddlewareRoutes(router)
            setupControllerRoutes(router)
            setupStaticRoutes(router)
            setupSpecialRoutes(router)

            // Mount router
            server.requestHandler(router)

            // Start server
            server.listen(config.server.port, config.server.host).await()
        }

        logger.info("Server is running on ${config.server.host}:${config.server.port}")
    } catch(e: Throwable) {
        logger.error("Encountered error while starting up:")
        e.printStackTrace()
        // Close Vert.x and exit the process with a non-zero status
        vertx.close().onComplete {
            exitProcess(1)
        }
    }
}