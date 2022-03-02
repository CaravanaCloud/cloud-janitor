package tasktree.app;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import com.vaadin.quarkus.QuarkusVaadinServlet;

@WebServlet(urlPatterns = "/*", name = "TestServlet", asyncSupported = true, initParams = {
        @WebInitParam(name = "org.atmosphere.websocket.suppressJSR356", value = "true"),
        // TODO: Enable dev mode gizmo when websockets are in use
        @WebInitParam(name = "devmode.gizmo.enabled", value = "false")})
public class TestServlet extends QuarkusVaadinServlet{
    // This servlet is here to give the suppressJSR356 parameter
    // Needed until https://github.com/vaadin/quarkus/issues/5 is fixed
}
