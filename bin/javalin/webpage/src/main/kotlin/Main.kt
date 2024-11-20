import ch.qos.logback.classic.Level
import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
val logger = LoggerFactory.getLogger(object {}.javaClass)

var pagesServed = 0;

fun main() {
    // https://www.jetbrains.com/help/idea/create-your-first-kotlin-app.html#package-as-jar
    // https://kotlinlang.org/docs/command-line.html
    rootLogger.level = Level.INFO

    val app = Javalin.create().start(7070)
    app.get("/verse-of-the-day") { ctx ->
        ctx.result(returnCorrectFile())
    }
}

fun returnCorrectFile(): String {
    pagesServed++
    if (pagesServed % 2 == 0) {
        return readResourceFile("Psalm.html")
    }

    return readResourceFile("Matthew.html")
}

fun readResourceFile(fileName: String): String {
    logger.info("pagesServed=${pagesServed}; fileName=${fileName}")

    val inputStream = object {}.javaClass.classLoader.getResourceAsStream(fileName)
    return inputStream.bufferedReader().use { it.readText() }
}