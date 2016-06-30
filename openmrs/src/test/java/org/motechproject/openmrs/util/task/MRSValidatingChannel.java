package org.motechproject.openmrs.util.task;

public interface MRSValidatingChannel {

    void execute(String pregnant, String dobKnown, String caseId);

    boolean hasExecuted();

    boolean verify(String patientUUID, String programUUID);
}
