package uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.configuration;

import org.junit.jupiter.api.Test;
import pl.pojo.tester.api.assertion.Method;
import uk.gov.hmcts.reform.wataskconfigurationapi.domain.entities.IdamUserDetails;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;

class IdamUserDetailsTest {

    @Test
    void isWellImplemented() {
        final Class<?> classUnderTest = IdamUserDetails.class;

        assertPojoMethodsFor(classUnderTest)
            .testing(Method.GETTER)
            .testing(Method.CONSTRUCTOR)
            .testing(Method.TO_STRING)
            .testing(Method.EQUALS)
            .testing(Method.HASH_CODE)
            .areWellImplemented();
    }

}
