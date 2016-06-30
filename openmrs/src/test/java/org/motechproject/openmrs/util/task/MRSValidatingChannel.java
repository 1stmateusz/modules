package org.motechproject.openmrs.util.task;

public interface MRSValidatingChannel {

    void execute(String patientUUID, String programUUID);

    boolean hasExecuted();

    boolean verify(String patientUUID, String programUUID);
}
