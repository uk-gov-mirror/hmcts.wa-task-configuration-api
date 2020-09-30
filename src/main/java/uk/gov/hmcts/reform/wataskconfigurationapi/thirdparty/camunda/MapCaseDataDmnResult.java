package uk.gov.hmcts.reform.wataskconfigurationapi.thirdparty.camunda;

import java.util.Objects;

public class MapCaseDataDmnResult {
    private CamundaValue<String> name;
    private CamundaValue<String> value;

    private MapCaseDataDmnResult() {
    }

    public MapCaseDataDmnResult(CamundaValue<String> name, CamundaValue<String> value) {
        this.name = name;
        this.value = value;
    }

    public CamundaValue<String> getName() {
        return name;
    }

    public CamundaValue<String> getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MapCaseDataDmnResult that = (MapCaseDataDmnResult) object;
        return Objects.equals(name, that.name)
               && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return "MapCaseDataDmnResult{"
               + "name=" + name
               + ", value=" + value
               + '}';
    }
}
