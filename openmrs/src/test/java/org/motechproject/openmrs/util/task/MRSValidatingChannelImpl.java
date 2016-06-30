package org.motechproject.openmrs.util.task;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("commcareValidatingChannelImpl")
public class MRSValidatingChannelImpl implements MRSValidatingChannel {

    private boolean executed;
    private String pregnant;
    private String dob;
    private String caseId;

    @PostConstruct
    public void setUp() {
        executed = false;
        pregnant = "";
        dob = "";
        caseId = "";
    }

    @Override
    public void execute(String pregnant, String dob, String caseId) {
        executed = true;
        this.pregnant = pregnant;
        this.dob = dob;
        this.caseId = caseId;
    }

    @Override
    public boolean hasExecuted() {
        return executed;
    }

    @Override
    public boolean verify() {
        return pregnant.equals("") && dob.equals("") && caseId.equals("");
    }
}