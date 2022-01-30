package scraping.query;

import static utils.TryCatch.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import nyu.Term;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TryCatch;

public final class QuerySchool {

  private static final Logger logger =
      LoggerFactory.getLogger("scraping.query.QuerySchool");
  private static final Uri ROOT_URI =
      Uri.create("https://m.albert.nyu.edu/app/catalog/classSearch");

  public static String querySchool(Term term) {
    logger.info("querying school for term={}", term);

    Request request =
        new RequestBuilder().setUri(ROOT_URI).setRequestTimeout(10000).build();

    TryCatch tc = tcNew(logger, "Failed to get school list: term={}", term);

    return tc.log(() -> GetClient.sendSync(request).getResponseBody());
  }
}
