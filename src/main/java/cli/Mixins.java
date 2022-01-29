package cli;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Mixin;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Spec;
import static utils.PolyFill.*;

import java.util.*;
import java.util.stream.*;
import nyu.*;
import picocli.CommandLine;
import register.RegistrationCourse;
import utils.JsonMapper;
import utils.Utils;

public final class Mixins {
  @Spec private CommandLine.Model.CommandSpec spec;

  public static final class BatchSize {
    @Option(names = "--batch-size-catalog",
            description = "batch size for querying the catalog")
    private Integer catalog;
    @Option(names = "--batch-size-sections",
            description = "batch size for querying sections")
    private Integer sections;

    @Spec private CommandLine.Model.CommandSpec spec;

    public int getCatalog(int defaultValue) {
      if (catalog == null)
        return defaultValue;
      else
        return catalog;
    }

    public int getSections(int defaultValue) {
      if (sections == null)
        return defaultValue;
      else
        return sections;
    }
  }

  public static final class CourseRegistration {
    @Option(names = "--courses") Map<Integer, String> courses;

    @Spec private CommandLine.Model.CommandSpec spec;

    public Map<Integer, String> getCourses() {
      if (courses == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(), "Must provide key value pairs");
      }
      return courses;
    }

    public List<RegistrationCourse> convertCourses() {
      List<RegistrationCourse> regCourses = new ArrayList<>();

      for (Map.Entry<Integer, String> entry : courses.entrySet()) {
        String value = entry.getValue();
        if (value.contains(",")) {
          int[] values = Arrays.stream(value.split(","))
                             .mapToInt(Integer::parseInt)
                             .toArray();
          List<Integer> sectionsRelated =
              Arrays.stream(values).boxed().collect(Collectors.toList());
          regCourses.add(
              new RegistrationCourse(entry.getKey(), sectionsRelated));
        } else if (value.trim().equals("")) {
          regCourses.add(
              new RegistrationCourse(entry.getKey(), new ArrayList<>()));
        } else {
          regCourses.add(new RegistrationCourse(
              entry.getKey(), listOf(Integer.parseInt(value))));
        }
      }
      return regCourses;
    }
  }

  public static final class InputFile {
    @Option(names = "--input-file",
            description =
                "intput file to read from. If none provided, read from stdout")
    private String inputFile;

    public String getInput() { return Utils.readFromFileOrStdin(inputFile); }
  }

  public static final class Login {
    @Option(names = "--username", description = "Be correct of capitalization")
    private String username;
    @Option(names = "--pwd", description = "Be correct of capitalization")
    private String password;

    @Spec private CommandLine.Model.CommandSpec spec;

    public User getUserNotNull() {
      if (username == null || password == null) {
        return null;
      }
      return new User(username, password);
    }
    public User getUser() {
      User user = getUserNotNull();
      if (user == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(), "Must provide --username AND --pwd");
      }
      return user;
    }
  }

  public static final class OutputFile {
    @Option(names = "--pretty", defaultValue = "true",
            description = "Either true or false")
    private boolean pretty;

    @Option(names = "--output-file",
            description =
                "output file to read from. If none provided, read from stdout")
    private String outputFile;

    public void writeOutput(Object output) {
      Utils.writeToFileOrStdout(outputFile, JsonMapper.toJson(output, pretty));
    }
  }

  public static final class RegistrationNumber {
    @Option(names = "--school", description = "school code: UA, UT, UY, etc")
    private String school;

    @Option(names = "--subject",
            description = "subject code: CSCI-UA, MATH-UA, etc")
    private String subject;

    @Option(
        names = "--registration-number",
        description =
            "A registration number, such as 9667. This can be either a number for Lecture, section, etc..")
    private Integer registrationNumber;

    @Option(names = "--registration-numbers", split = ",",
            description = "Multiple registration numbers: 1134,441,134,...")
    private List<Integer> registrationNumbers;

    @Spec private CommandLine.Model.CommandSpec spec;

    public List<nyu.SubjectCode> getSubjectCodes() {
      if (school == null && subject == null && registrationNumber == null &&
          registrationNumbers == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
            "Must provide at least one of --school, --subject, or --registration-number");
      }
      if (school == null) {
        if (subject != null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "--subject doesn't make sense if school is null");
        }
        return nyu.SubjectCode.allSubjects();
      } else if (subject == null) {
        if (registrationNumber == null)
          return nyu.SubjectCode.allSchools().get(school).subjects;
        else
          return null;
      } else {
        nyu.SubjectCode s = nyu.SubjectCode.fromCode(subject);
        return Arrays.asList(s);
      }
    }

    public Integer getRegistrationNumber() { return registrationNumber; }

    public List<Integer> getRegistrationNumbers() {
      return registrationNumbers;
    }
  }

  public static final class SubjectCode {
    @Option(names = "--school", description = "school code: UA, UT, UY, etc")
    private String school;

    @Option(names = "--subject",
            description = "subject code: CSCI-UA, MATH-UA, etc")
    private String subject;

    @Spec private CommandLine.Model.CommandSpec spec;

    public List<nyu.SubjectCode> getSubjectCodes() {
      if (school == null) {
        if (subject != null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "--subject doesn't make sense if school is null");
        }
        return nyu.SubjectCode.allSubjects();
      } else if (subject == null) {
        return nyu.SubjectCode.allSchools().get(school).subjects;
      } else {
        nyu.SubjectCode s = nyu.SubjectCode.fromCode(subject);
        return Arrays.asList(s);
      }
    }
  }

  public static final class Term {
    @Spec private CommandLine.Model.CommandSpec spec;

    @Option(names = "--term",
            description = "Term is the shortcut for year and semester. "
                          + "To get term value, take year - 1900 then append \n"
                          + "ja = 2, sp = 4, su = 6 or fa = 8.\n Eg: "
                          + "Fall 2020 = (2020 - 1900) + 4 = 120 + 4 = 1204")
    private Integer termId;

    @Option(names = "--semester", description = "semester: ja, sp, su, or fa")
    private String semester;

    @Option(names = "--year", description = "year to scrape from")
    private Integer year;

    @Option(names = {"-h", "--help"}, usageHelp = true,
            description = "display a help message")
    boolean displayHelp;

    public nyu.Term getTermAllowNull() {
      if (termId != null && (semester != null || year != null)) {
        throw new CommandLine.MutuallyExclusiveArgsException(
            spec.commandLine(),
            "--term and --semester/--year are mutually exclusive");
      } else if (termId == null && semester == null && year == null) {
        return null;
      } else if (termId == null) {
        if (semester == null || year == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(), "Must provide both --semester AND --year");
        }
        return new nyu.Term(semester, year);
      } else {
        return nyu.Term.fromId(termId);
      }
    }

    public nyu.Term getTerm() {
      nyu.Term t = getTermAllowNull();
      if (t == null) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
            "Must provide at least one: --term   OR   --semester AND --year");
      } else {
        return t;
      }
    }
  }
}
