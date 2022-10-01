package cli;

import static picocli.CommandLine.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.*;
import picocli.CommandLine;
import utils.Client;

/*
   @Todo: Add annotation for parameter.
*/
@Command(name = "scrape",
         description =
             "Query then parse NYU Albert data based on different catagories")
public class Scrape implements Runnable {
  @Spec private CommandLine.Model.CommandSpec spec;

  private static Logger logger = LoggerFactory.getLogger("cli.Scrape");
  @Option(names = {"-h", "--help"}, usageHelp = true,
          description = "display a help message")
  boolean displayHelp;

  @Override
  public void run() {
    throw new CommandLine.ParameterException(spec.commandLine(),
                                             "\nMissing required subcommand.");
  }

  // @Command(name = "catalog",
  //          description = "Scrape catalog based on term, subject codes, "
  //                        + "or school for one or multiple subjects/schools")
  // public void
  // catalog(@Mixin Mixins.Term termMixin, @Mixin Mixins.Subject subjectMixin,
  //         @Option(names = "--batch-size", defaultValue = "20",
  //                 description = "batch size if query more than one catalog")
  //         int batchSize,
  //         @Mixin Mixins.OutputFile outputFileMixin) {
  //   long start = System.nanoTime();

  //   List<scraping.models.Course> courses = ScrapeCatalog.scrapeCatalog(
  //       termMixin.getTerm(), subjectMixin.getSubjects(), batchSize);

  //   outputFileMixin.writeOutput(courses);

  //   Client.close();

  //   long end = System.nanoTime();
  //   double duration = (end - start) / 1000000000.0;
  //   logger.info("{} seconds for {} courses", duration, courses.size());
  // }

  // @Command(name = "school", sortOptions = false,
  //          headerHeading = "Command: ", descriptionHeading =
  //          "%nDescription:%n", parameterListHeading = "%nParameters:%n",
  //          optionListHeading = "%nOptions:%n", header = "Scrape
  //          school/subject", description = "Scrape school/subject based on
  //          term")
  // public void
  // school(@Mixin Mixins.Term termMixin, @Mixin Mixins.OutputFile
  // outputFileMixin)
  //     throws ExecutionException, InterruptedException {
  //   long start = System.nanoTime();

  //   outputFileMixin.writeOutput(ParseSchoolSubjects.parseSchool(
  //       QuerySchool.querySchool(termMixin.getTerm())));

  //   Client.close();

  //   long end = System.nanoTime();
  //   double duration = (end - start) / 1000000000.0;
  //   logger.info(duration + " seconds");
  // }
}
