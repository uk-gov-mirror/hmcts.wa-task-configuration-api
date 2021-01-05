package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.roleassignment;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.RoleType;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class RoleTypeTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = RoleType.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .areWellImplemented();
    }


}
