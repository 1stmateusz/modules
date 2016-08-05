package org.motechproject.mapping.module.events.constants;

/**
 * Utility class for storing display names of the the task actions.
 */
public final class DisplayNames {

    //Configs
    public static final String COMMCARE_CONFIG_NAME = "commcare.field.configName";
    public static final String OPENMRS_CONFIG_NAME = "openmrs.field.configName";

    //Triggers
    public static final String MAPPING_RESULT_TRIGGER = "mapping.module.mapping.result";

    //Parameters
    public static final String COMMCARE_CASE_ID = "mapping.module.commcare.caseId";
    public static final String COMMCARE_ADDITIONAL_ID = "mapping.module.commcare.additionalId";
    public static final String OPENMRS_PATIENT_UUID = "mapping.module.openmrs.patientUuid";
    public static final String OPENMRS_PERSON_UUID = "mapping.module.openmrs.personUuid";
    public static final String OPENMRS_PATIENT_ID_MOTECH_ID = "mapping.module.openmrs.personId.motechId";
    public static final String OPENMRS_ADDITIONAL_ID = "mapping.module.openmrs.additionalId";

    //Actions
    public static final String MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_COMMCARE_CASE_ID = "mapping.module.mapping.result.commcare.case.id";
    public static final String MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_COMMCARE_ADDITIONAL_ID = "mapping.module.mapping.result.commcare.additional.id";
    public static final String MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_PATIENT_UUID = "mapping.module.mapping.result.openmrs.patient.uuid";
    public static final String MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_PERSON_UUID = "mapping.module.mapping.result.openmrs.person.uuid";
    public static final String MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_ADDITIONAL_ID = "mapping.module.mapping.result.commcare.case.id";

    //Mapping Result Action Parameters
    public static final String PARAMETER_1 = "mapping.module.mapping.result.paramter1";
    public static final String PARAMETER_2 = "mapping.module.mapping.result.paramter2";
    public static final String MAPPING_RESULT_COMMCARE_CASE_ID_PARAMTER = "mapping.module.mapping.result.commcare.case.id";
    public static final String MAPPING_RESULT_COMMCARE_ADDITIONAL_ID_PARAMETER = "mapping.module.mapping.result.commcare.additional.id";
    public static final String MAPPING_RESULT_OPENMRS_PATIENT_UUID_PARAMETER = "mapping.module.mapping.result.openmrs.patient.uuid";
    public static final String MAPPING_RESULT_OPENMRS_PERSON_UUID_PARAMETER = "mapping.module.mapping.result.openmrs.person.uuid";
    public static final String MAPPING_RESULT_OPENMRS_ADDITIONAL_ID_PARAMETER = "mapping.module.mapping.result.commcare.case.id";

    /**
     * Utility class, should not be initiated.
     */
    private DisplayNames() {
    }
}
