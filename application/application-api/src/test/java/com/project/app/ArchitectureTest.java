package com.project.app;

import com.project.common.test.arch.PlatformArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Verifies the architectural decisions of the platform against all compiled modules.
 */
@AnalyzeClasses(packages = "com.project", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule coreIsFrameworkFree = PlatformArchitectureRules.CORE_IS_FRAMEWORK_FREE;

    @ArchTest
    static final ArchRule entitiesHaveNoJpaRelations = PlatformArchitectureRules.ENTITIES_HAVE_NO_JPA_RELATIONS;

    @ArchTest
    static final ArchRule persianCalendarOnlyInDatetime = PlatformArchitectureRules.PERSIAN_CALENDAR_ONLY_IN_DATETIME;

    @ArchTest
    static final ArchRule controllersDoNotTouchPersistence = PlatformArchitectureRules.CONTROLLERS_DO_NOT_TOUCH_PERSISTENCE;

    @ArchTest
    static final ArchRule servicesDoNotDependOnDaoImplementations =
            PlatformArchitectureRules.SERVICES_DO_NOT_DEPEND_ON_DAO_IMPLEMENTATIONS;

    @ArchTest
    static final ArchRule projectPackagesAreFreeOfCycles = PlatformArchitectureRules.PROJECT_PACKAGES_ARE_FREE_OF_CYCLES;
}
