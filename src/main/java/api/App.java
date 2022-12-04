package api;

import api.v1.*;
import database.*;
import io.javalin.Javalin;
import io.javalin.http.*;
import io.javalin.http.staticfiles.Location;
import io.javalin.openapi.OpenApiInfo;
import io.javalin.openapi.plugin.*;
import java.io.*;
import org.slf4j.*;
import utils.Utils;

public class App {
  private static final Logger logger = LoggerFactory.getLogger("api.App");

  static {
    // Ensure that the connection gets instantiated during startup
    GetConnection.withConnection(conn -> {});
  }

  public abstract static class Endpoint implements Handler {
    public abstract String getPath();

    public abstract Object handleEndpoint(Context ctx);

    @Override
    public final void handle(Context ctx) {
      try {
        Object output = this.handleEndpoint(ctx);
        if (output instanceof ApiError) {
          var e = (ApiError)output;
          ctx.status(e.status);
          ctx.json(e);

          return;
        }

        ctx.status(200);
        ctx.json(output);
      } catch (Exception e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
      }
    }

    public final void addTo(Javalin app) { app.get("/api" + getPath(), this); }
  }

  public static class ApiError {
    private final int status;
    private final String message;

    public ApiError(String message) { this(400, message); }
    public ApiError(int status, String message) {
      this.message = message;
      this.status = status;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
  }

  public static final int PORT = 4358;

  public static Javalin makeApp() {
    Javalin app = Javalin.create(config -> {
      config.plugins.enableCors(cors -> { // It's a public API
        cors.add(it -> { it.anyHost(); });
      });

      var description =
          "Schedge is an API to NYU's course catalog. "
          + "Please note that <b>this API is a beta build currently under "
          + "active development and is subject to change</b>."
          + "<br/><br/>If you'd like to contribute, "
          + "<a href=\"https://github.com/A1Liu/schedge\">"
          + "check out the repository</a>.<br /> <br />"
          + "<b><big>SCHEDGE DEVELOPMENT BUILD</big></b>";

      var info = new OpenApiInfo();
      info.setVersion("2.0.0 beta");
      info.setTitle("Schedge");
      info.setDescription(description);

      var jsonPath = "/api/swagger.json";
      var openApiConfig = new OpenApiConfiguration();
      openApiConfig.setInfo(info);
      openApiConfig.setDocumentationPath(jsonPath);
      config.plugins.register(new OpenApiPlugin(openApiConfig));

      config.staticFiles.add(staticFiles -> { // NextJS UI
        staticFiles.hostedPath = "/";
        staticFiles.directory = "/next";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = true;
      });

      config.staticFiles.add(staticFiles -> { // ReDoc API Docs
        staticFiles.hostedPath = "/api";
        staticFiles.directory = "/api";
        staticFiles.location = Location.CLASSPATH;
        staticFiles.precompress = false;
      });
    });

    app.exception(Exception.class, (e, ctx) -> {
      String stackTrace = Utils.stackTrace(e);

      String message = String.format(
          "Uncaught Exception: %s\nQuery Parameters are: %s\nPath: %s\n",
          stackTrace, ctx.queryParamMap().toString(), ctx.path());
      logger.warn(message);
    });

    new SchoolInfoEndpoint().addTo(app);
    new CampusEndpoint().addTo(app);
    new ListTermsEndpoint().addTo(app);
    new Health().addTo(app);

    new SearchEndpoint().addTo(app);
    new GenerateScheduleEndpoint().addTo(app);
    new CoursesEndpoint().addTo(app);

    ScrapingEndpoint.add(app);

    return app;
  }

  public static void run() {
    var app = makeApp();
    app.start(PORT);
  }
}
