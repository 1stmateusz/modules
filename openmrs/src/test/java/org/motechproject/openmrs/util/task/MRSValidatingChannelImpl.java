package org.motechproject.openmrs.util.task;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("commcareValidatingChannelImpl")
public class MRSValidatingChannelImpl implements MRSValidatingChannel {

    private boolean executed;
    private String programUUID;
    private String patientUUID;

    @PostConstruct
    public void setUp() {
        executed = false;
        programUUID = "";
        patientUUID = "";
    }

    @Override
    public void execute(String patientUUID, String programUUID) {
        executed = true;
        this.programUUID = programUUID;
        this.patientUUID = patientUUID;
    }

    @Override
    public boolean hasExecuted() {
        return executed;
    }

    @Override
    public boolean verify(String patientUUID, String programUUID) {
        return patientUUID.equals(patientUUID) && programUUID.equals(programUUID);
    }
}