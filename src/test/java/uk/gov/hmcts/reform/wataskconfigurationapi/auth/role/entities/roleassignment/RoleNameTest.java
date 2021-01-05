package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.roleassignment;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleName;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class RoleNameTest {
    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = RoleName.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .areWellImplemented();
    }


}
