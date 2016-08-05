package org.motechproject.mapping.module.events.constants;


public class EventDataKeys {

    //Configs
    public static final String COMMCATE_CONFIG = "commcare_config";
    public static final String OPENMRS_CONFIG = "openmrs_config";

    //Mapping Result Trigger Parameter Keys
    public static final String COMMCARE_CASID = "commcare_case_id";
    public static final String COMMCARE_ADDITIONAL_ID = "commcare_additional_id";
    public static final String OPENMRS_PATIENT_UUID = "openmrs_patient_uuid";
    public static final String OPENMRS_PERSON_UUID = "openmrs_person_uuid";
    public static final String OPENMRS_PATIENT_ID_MOTECH_ID = "openmrs_patient_id_motech_id";
    public static final String OPENMRS_ADDDTIONAL_ID = "openmrs-additional_id";

    //Mapping Result Action Parameter Keys
    public static final String MAPPING_RESULT_ACTION_COMMCARE_CASE_ID = "commcareCaseId";
    public static final String MAPPING_RESULT_ACTION_COMMCARE_ADDITIONAL_ID = "commcareAdditionalId";
    public static final String MAPPING_RESULT_ACTION_OPENMRS_PATIENT_UUID = "openmrsPatientUuid";
    public static final String MAPPING_RESULT_ACTION_OPENMRS_PERSON_UUID = "openmrsPersonUuid";
    public static final String MAPPING_RESULT_ACTION_OPENMRS_ADDITIONAL_ID = "openmrsAdditionalId";
    public static final String MAPPING_RESULT_ACTION_PARAMETER_1 = "parameter1";
    public static final String MAPPING_RESULT_ACTION_PARAMETER_2 = "parameter2";

    /**
     * Utility class, should not be initiated.
     */
    private EventDataKeys() {
    }
}
