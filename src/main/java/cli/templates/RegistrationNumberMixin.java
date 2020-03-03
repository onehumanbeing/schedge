package cli.templates;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nyu.SubjectCode;
import nyu.Term;
import picocli.CommandLine;

public final class RegistrationNumberMixin {
  private RegistrationNumberMixin() {}


  @CommandLine.
  Option(names = "--school", description = "school code: UA, UT, UY, etc")
  private String school;
  @CommandLine.
  Option(names = "--subject", description = "subject code: CSCI, MATH, etc")
  private String subject;
  @CommandLine.
  Option(names = "--registration-number", description = "A registration number")
  private Integer registrationNumber;

  public List<SubjectCode> getSubjectCodes() {
    if (school == null && subject == null && registrationNumber == null) {
      throw new IllegalArgumentException(
          "Must provide at least one of --school, --subject, or --registration-number");
    }
    if (school == null) {
      if (subject != null) {
        throw new IllegalArgumentException(
            "--subject doesn't make sense if school is null");
      }
      return SubjectCode.allSubjects();
    } else if (subject == null) {
      if (registrationNumber == null)
        return SubjectCode.allSubjectsForSchool(school);
      else
        return null;
    } else {
      SubjectCode s = new SubjectCode(subject, school);
      s.checkValid();
      return Arrays.asList(s);
    }
  }
  public Integer getRegistrationNumber() { return registrationNumber; }
}
