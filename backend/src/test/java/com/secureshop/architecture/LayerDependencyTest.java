package com.secureshop.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.secureshop")
class LayerDependencyTest {

    @ArchTest
    static final ArchRule domain_depends_on_nothing_except_java =
            classes()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("..domain..", "java..", "javax..");

    @ArchTest
    static final ArchRule application_depends_only_on_domain_and_java =
            classes()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage("..application..", "..domain..", "java..", "javax..");

    @ArchTest
    static final ArchRule infrastructure_does_not_depend_on_api =
            noClasses()
                    .that()
                    .resideInAPackage("..infrastructure..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule api_does_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..api..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");
}
