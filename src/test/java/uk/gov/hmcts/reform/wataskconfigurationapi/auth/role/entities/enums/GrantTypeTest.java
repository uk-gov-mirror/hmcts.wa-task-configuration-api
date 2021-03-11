package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.enums.GrantType;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class GrantTypeTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = GrantType.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .areWellImplemented();
    }

}
