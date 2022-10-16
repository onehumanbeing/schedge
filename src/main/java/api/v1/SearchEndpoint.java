package api.v1;

import api.*;
import database.GetConnection;
import database.courses.SearchRows;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import java.util.*;
import java.util.stream.Collectors;
import utils.Nyu;

public final class SearchEndpoint extends App.Endpoint {

  public String getPath() { return "/search/{term}"; }

  public OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          openApiOperation.description(
              "This endpoint returns a list of courses for a year and semester, given search terms.");
          openApiOperation.summary("Search Endpoint");
        })
        .pathParam("term", String.class,
                   openApiParam -> {
                     openApiParam.description(
                         SchoolInfoEndpoint.TERM_PARAM_DESCRIPTION);
                   })
        .queryParam(
            "full", Boolean.class, false,
            openApiParam -> {
              openApiParam.description(
                  "if present and equal to 'true', then you'll get full output");
            })
        .queryParam("query", String.class, false,
                    openApiParam -> {
                      openApiParam.description(
                          "A query string to pass to the search engine.");
                    })
        .queryParam(
            "limit", Integer.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The maximum number of top-level sections to return. Capped at 50.");
            })
        .queryParam("school", String.class, false,
                    openApiParam -> {
                      openApiParam.description("The school to search within.");
                    })
        .queryParam(
            "subject", String.class, false,
            openApiParam -> {
              openApiParam.description(
                  "The subject to search within. Can work cross school.");
            })
        .json("400", App.ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .jsonArray("200", Nyu.Course.class,
                   openApiParam -> { openApiParam.description("OK."); });
  }

  public Object handleEndpoint(Context ctx) {
    String termString = ctx.pathParam("term");
    var term = SchoolInfoEndpoint.parseTerm(termString);

    String args = ctx.queryParam("query");
    if (args == null) {
      throw new RuntimeException("Need to provide a query.");
    } else if (args.length() > 50) {
      throw new RuntimeException("Query can be at most 50 characters long.");
    }

    String school = ctx.queryParam("school"), subject =
                                                  ctx.queryParam("subject");

    int resultSize, titleWeight, descriptionWeight, notesWeight, prereqsWeight;
    try {
      resultSize = Optional.ofNullable(ctx.queryParam("limit"))
                       .map(Integer::parseInt)
                       .orElse(50);

      titleWeight = Optional.ofNullable(ctx.queryParam("titleWeight"))
                        .map(Integer::parseInt)
                        .orElse(2);

      descriptionWeight =
          Optional.ofNullable(ctx.queryParam("descriptionWeight"))
              .map(Integer::parseInt)
              .orElse(1);

      notesWeight = Optional.ofNullable(ctx.queryParam("notesWeight"))
                        .map(Integer::parseInt)
                        .orElse(0);

      prereqsWeight = Optional.ofNullable(ctx.queryParam("prereqsWeight"))
                          .map(Integer::parseInt)
                          .orElse(0);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Limit needs to be a positive integer.");
    }

    Object output = GetConnection.withConnectionReturning(conn -> {
      String fullData = ctx.queryParam("full");
      if (fullData != null && fullData.toLowerCase().equals("true")) {
        return RowsToCourses
            .fullRowsToCourses(
                SearchRows.searchFullRows(conn, term, subject, school, args))
            .limit(resultSize)
            .collect(Collectors.toList());
      } else {
        return RowsToCourses
            .rowsToCourses(
                SearchRows.searchRows(conn, term, subject, school, args))
            .limit(resultSize)
            .collect(Collectors.toList());
      }
    });

    return output;
  }
}
