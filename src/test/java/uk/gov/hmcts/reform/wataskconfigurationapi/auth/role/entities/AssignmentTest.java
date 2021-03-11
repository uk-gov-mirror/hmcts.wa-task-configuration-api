package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities.Assignment;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;


class AssignmentTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = Assignment.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .areWellImplemented();
    }

}
