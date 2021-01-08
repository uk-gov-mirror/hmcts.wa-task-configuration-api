package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.roleassignment;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleCategory;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class RoleCategoryTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = RoleCategory.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .areWellImplemented();
    }


}
