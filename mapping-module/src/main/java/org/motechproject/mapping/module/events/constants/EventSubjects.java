package org.motechproject.mapping.module.events.constants;

public class EventSubjects {

    private static final String BASE_SUBJECT = "org.motechproject.mapping.module.api.";

    //Mapping Result Event Subjects
    public static final String MAPPING_RESULT = BASE_SUBJECT + "mappingResult";

    public static final String MAPPING_RESULT_WITH_LOOKUP_BY_COMMCARE_CASE_ID = MAPPING_RESULT + "commcareCaseId";
    public static final String MAPPING_RESULT_WITH_LOOKUP_BY_COMMCARE_ADDITIONAL_ID = MAPPING_RESULT + "commcareAdditionalId";
    public static final String MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_PATIENT_UUID = MAPPING_RESULT + "openmrsPatientUuid";
    public static final String MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_PERSON_UUID = MAPPING_RESULT + "openmrsPersonUuid";
    public static final String MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_ADDITIONAL_ID = MAPPING_RESULT + "openmrsAdditionalId";

    public static String getConfigName(String subject) {
        return subject.substring(subject.lastIndexOf('.') + 1);
    }

    /**
     * Utility class, should not be initiated.
     */
    private EventSubjects() {
    }
}
