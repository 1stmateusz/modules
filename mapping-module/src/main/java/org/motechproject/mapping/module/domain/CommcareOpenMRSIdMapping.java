package org.motechproject.mapping.module.domain;

import org.motechproject.mds.annotations.Access;
import org.motechproject.mds.annotations.CrudEvents;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.event.CrudEventType;
import org.motechproject.mds.util.SecurityMode;

import java.util.Objects;

@Entity(name = "Commcare OpenMRS Mapping")
@CrudEvents(CrudEventType.NONE)
@Access(value = SecurityMode.PERMISSIONS, members = {"manageCommcare"})
public class CommcareOpenMRSIdMapping {

    @Field(displayName = "Commcare Case Id")
    private String commcareCaseId;

    @Field(displayName = "Commcare Additional Id")
    private String commcareAdditionalId;

    @Field(displayName = "OpenMRS Patient UUID")
    private String openmrsPatientUuid;

    @Field(displayName = "OpenMRS Person UUID")
    private String openmrsPersonUuid;

    @Field(displayName = "OpenMRS Additional Id")
    private String openmrsAdditionalId;

    CommcareOpenMRSIdMapping() {
        this(null, null, null, null, null);
    }

    CommcareOpenMRSIdMapping(String commcareCaseId, String commcareAdditionalId, String openmrsPatientUuid, String openmrsPersonUuid, String openmrsAdditionalId) {
        this.commcareCaseId = commcareCaseId;
        this.commcareAdditionalId = commcareAdditionalId;
        this.openmrsPatientUuid = openmrsPatientUuid;
        this.openmrsPersonUuid = openmrsPersonUuid;
        this.openmrsAdditionalId = openmrsAdditionalId;
    }

    public String getCommcareCaseId() {
        return commcareCaseId;
    }

    public String getCommcareAdditionalId() {
        return commcareAdditionalId;
    }

    public String getOpenmrsPatientUuid() {
        return openmrsPatientUuid;
    }

    public String getOpenmrsPersonUuid() {
        return openmrsPersonUuid;
    }

    public String getOpenmrsAdditionalId() {
        return openmrsAdditionalId;
    }

    public void setCommcareCaseId(String commcareCaseId) {
        this.commcareCaseId = commcareCaseId;
    }

    public void setCommcareAdditionalId(String commcareAdditionalId) {
        this.commcareAdditionalId = commcareAdditionalId;
    }

    public void setOpenmrsPatientUuid(String openmrsPatientUuid) {
        this.openmrsPatientUuid = openmrsPatientUuid;
    }

    public void setOpenmrsPersonUuid(String openmrsPersonUuid) {
        this.openmrsPersonUuid = openmrsPersonUuid;
    }

    public void setOpenmrsAdditionalId(String openmrsAdditionalId) {
        this.openmrsAdditionalId = openmrsAdditionalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommcareOpenMRSIdMapping that = (CommcareOpenMRSIdMapping) o;
        return Objects.equals(commcareCaseId, that.commcareCaseId) &&
                Objects.equals(commcareAdditionalId, that.commcareAdditionalId) &&
                Objects.equals(openmrsPatientUuid, that.openmrsPatientUuid) &&
                Objects.equals(openmrsPersonUuid, that.openmrsPersonUuid) &&
                Objects.equals(openmrsAdditionalId, that.openmrsAdditionalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commcareCaseId, commcareAdditionalId, openmrsPatientUuid, openmrsPersonUuid, openmrsAdditionalId);
    }

    @Override
    public String toString() {
        return "CommcareOpenMRSIdMapping{" +
                "commcareCaseId='" + commcareCaseId + '\'' +
                ", commcareAdditionalId='" + commcareAdditionalId + '\'' +
                ", openmrsPatientUuid='" + openmrsPatientUuid + '\'' +
                ", openmrsPersonUuid='" + openmrsPersonUuid + '\'' +
                ", openmrsAdditionalId='" + openmrsAdditionalId + '\'' +
                '}';
    }
}