package de.ellpeck.rockbottom.log;

import de.ellpeck.rockbottom.init.AbstractGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Logging {

    public static Logger mainLogger;
    public static Logger chatLogger;
    public static Logger glfwLogger;
    public static Logger nettyLogger;

    public static void init() {
        mainLogger = LogManager.getLogger(AbstractGame.NAME);

        chatLogger = createLogger("Chat");
        glfwLogger = createLogger("GLFW");
        nettyLogger = createLogger("Netty");
    }

    public static Logger createLogger(String name) {
        return LogManager.getLogger(name);
    }
}
