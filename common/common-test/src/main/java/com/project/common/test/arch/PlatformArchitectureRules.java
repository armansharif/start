package com.project.common.test.arch;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Architecture rules of the platform, shared by every module that wants to verify them.
 *
 * <p>These are executable decisions, not documentation: violating one fails the build.
 * The rules are registered in {@code application-api}'s architecture test, which sees
 * all modules at once.</p>
 */
public final class PlatformArchitectureRules {

    private static final String ANY_JPA_RELATION = "jakarta.persistence.ManyToOne";

    private PlatformArchitectureRules() {
    }

    /**
     * {@code common-core} must stay usable without any framework: it holds the error
     * contract, the search model and paging, all of which are plain Java.
     */
    public static final ArchRule CORE_IS_FRAMEWORK_FREE =
            noClasses()
                    .that().resideInAPackage("com.project.common.core..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.servlet..",
                            "net.time4j..");

    /**
     * Entities reference other rows by identifier, never by object relation. This single
     * rule is what keeps lazy loading, N+1 queries and mixed loading strategies out.
     */
    public static final ArchRule ENTITIES_HAVE_NO_JPA_RELATIONS =
            noFields()
                    .that().areDeclaredInClassesThat().resideInAPackage("..entity..")
                    .should(notBeAJpaRelation());

    /** The calendar algorithm exists in exactly one module. */
    public static final ArchRule PERSIAN_CALENDAR_ONLY_IN_DATETIME =
            noClasses()
                    .that().resideInAPackage("com.project..")
                    .and().resideOutsideOfPackage("com.project.common.datetime..")
                    .should().dependOnClassesThat().resideInAPackage("net.time4j..");

    /**
     * Controllers talk to services and DTOs, never to persistence or entities.
     */
    public static final ArchRule CONTROLLERS_DO_NOT_TOUCH_PERSISTENCE =
            noClasses()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAnyPackage("..dao..", "..entity..");

    /** Services depend on DAO interfaces only; implementations are wiring details. */
    public static final ArchRule SERVICES_DO_NOT_DEPEND_ON_DAO_IMPLEMENTATIONS =
            noClasses()
                    .that().resideInAPackage("..service.impl..")
                    .should().dependOnClassesThat().resideInAPackage("..dao.impl..");

    /**
     * Top level packages (common modules and business modules) must not depend on each
     * other in a cycle. Business modules sit directly under {@code com.project}, so one
     * slice per module is enough to keep the dependency graph acyclic.
     */
    public static final ArchRule PROJECT_PACKAGES_ARE_FREE_OF_CYCLES =
            SlicesRuleDefinition.slices()
                    .matching("com.project.(*)..")
                    .should().beFreeOfCycles();

    /**
     * Matching by type name keeps this rule independent of the persistence API being on
     * the test classpath, and catches every relation flavour including future ones.
     */
    private static ArchCondition<JavaField> notBeAJpaRelation() {
        return new ArchCondition<>("not be a JPA relation") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                field.getAnnotations().stream()
                        .map(annotation -> annotation.getRawType().getName())
                        .filter(PlatformArchitectureRules::isJpaRelationAnnotation)
                        .forEach(name -> events.add(SimpleConditionEvent.violated(field,
                                "Entity field %s uses %s; keep the identifier only"
                                        .formatted(field.getFullName(), name))));
            }
        };
    }

    private static boolean isJpaRelationAnnotation(String annotationTypeName) {
        return annotationTypeName.equals(ANY_JPA_RELATION)
                || annotationTypeName.equals("jakarta.persistence.OneToMany")
                || annotationTypeName.equals("jakarta.persistence.OneToOne")
                || annotationTypeName.equals("jakarta.persistence.ManyToMany")
                || annotationTypeName.equals("jakarta.persistence.JoinColumn");
    }
}
