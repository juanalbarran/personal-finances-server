package com.jarsolutions.finances.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.jarsolutions.finances",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule applicationMustNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter..");

  @ArchTest
  static final ArchRule domainModelMustNotDependOnSpringOrJpa =
      noClasses()
          .that()
          .resideInAPackage("..application.domain.model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

  @ArchTest
  static final ArchRule incomingAdaptersMustNotDependOnOutgoingAdapters =
      noClasses()
          .that()
          .resideInAPackage("..adapter.in..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter.out..");

  @ArchTest
  static final ArchRule adaptersMustNotBypassThePortsToCallServices =
      noClasses()
          .that()
          .resideInAPackage("..adapter..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..application.domain.service..");

  @ArchTest
  static final ArchRule portsMustBeInterfaces =
      classes().that().resideInAPackage("..application.port..").should().beInterfaces();
}
