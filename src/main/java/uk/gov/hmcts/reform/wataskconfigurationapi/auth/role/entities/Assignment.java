package uk.gov.hmcts.reform.wataskconfigurationapi.auth.role.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class Assignment {
    protected final ActorIdType actorIdType;
    protected final String actorId;
    protected final RoleType roleType;
    protected final RoleName roleName;
    protected final RoleCategory roleCategory;
    protected final Classification classification;
    protected final GrantType grantType;
    protected final Boolean readOnly;
    protected final Map<Attributes, String> attributes;


    public Assignment(ActorIdType actorIdType,
                      String actorId,
                      RoleType roleType,
                      RoleName roleName,
                      RoleCategory roleCategory,
                      Classification classification,
                      GrantType grantType,
                      Boolean readOnly,
                      Map<Attributes, String> attributes) {
        this.actorIdType = actorIdType;
        this.actorId = actorId;
        this.roleType = roleType;
        this.roleName = roleName;
        this.roleCategory = roleCategory;
        this.classification = classification;
        this.grantType = grantType;
        this.readOnly = readOnly;
        this.attributes = attributes;
    }

    public ActorIdType getActorIdType() {
        return actorIdType;
    }

    public String getActorId() {
        return actorId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public RoleCategory getRoleCategory() {
        return roleCategory;
    }

    public Classification getClassification() {
        return classification;
    }

    public GrantType getGrantType() {
        return grantType;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public Map<Attributes, String> getAttributes() {
        return attributes;
    }
}
