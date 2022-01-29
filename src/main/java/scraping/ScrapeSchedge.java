package scraping;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import models.*;
import nyu.*;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.*;
import scraping.query.GetClient;
import utils.*;

public final class ScrapeSchedge {
  private static Logger logger =
      LoggerFactory.getLogger("scraping.ScrapeSchedge");

  private static final String SCHEDGE_URL = "https://nyu.a1liu.com/";

  public static Stream<Course> scrapeFromSchedge(Term term) {
    BiFunction<SubjectCode, Integer, Future<String>> func =
        (subjectCode, idx) -> {
      String[] components =
          new String[] {"" + term.year, term.semString(), subjectCode.code};

      Uri uri =
          Uri.create(SCHEDGE_URL + String.join("/", components) + "?full=true");

      Request request = new RequestBuilder().setUri(uri).build();
      return GetClient.getClient()
          .executeRequest(request)
          .toCompletableFuture()
          .handleAsync((resp, throwable) -> {
            if (resp == null) {
              logger.error("Error (subjectCode={}): {}", subjectCode,
                           throwable.getMessage());

              return null;
            }

            String text = resp.getResponseBody();

            return text;
          });
    };

    return StreamSupport
        .stream(new SimpleBatchedFutureEngine<SubjectCode, String>(
                    SubjectCode.allSubjects().listIterator(), 20, func)
                    .spliterator(),
                false)
        .flatMap(text -> {
          if (text == null) {
            return new ArrayList<Course>().stream();
          }

          List<Course> courses = JsonMapper.fromJsonArray(text, Course.class);

          return courses.stream();
        });
  }
}
