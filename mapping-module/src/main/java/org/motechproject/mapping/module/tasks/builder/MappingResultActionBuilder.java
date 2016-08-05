package org.motechproject.mapping.module.tasks.builder;

import org.motechproject.commcare.config.Config;
import org.motechproject.commcare.service.CommcareConfigService;
import org.motechproject.mapping.module.events.constants.DisplayNames;
import org.motechproject.mapping.module.events.constants.EventDataKeys;
import org.motechproject.mapping.module.events.constants.EventSubjects;
import org.motechproject.openmrs.service.OpenMRSConfigService;
import org.motechproject.tasks.contract.ActionEventRequest;
import org.motechproject.tasks.contract.ActionParameterRequest;
import org.motechproject.tasks.contract.builder.ActionEventRequestBuilder;
import org.motechproject.tasks.contract.builder.ActionParameterRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.motechproject.tasks.domain.mds.ParameterType.UNICODE;

public class MappingResultActionBuilder implements ActionBuilder {

    private CommcareConfigService commcareConfigService;
    private OpenMRSConfigService openMRSConfigService;

    public MappingResultActionBuilder(CommcareConfigService commcareConfigService, OpenMRSConfigService openMRSConfigService) {
        this.commcareConfigService = commcareConfigService;
        this.openMRSConfigService = openMRSConfigService;
    }

    @Override
    public List<ActionEventRequest> buildActions() {

        List<ActionEventRequest> actions = new ArrayList<>();

        for (Config commcareConfig: commcareConfigService.getConfigs().getConfigs()) {
            for (org.motechproject.openmrs.config.Config openmrsConfig: openMRSConfigService.getConfigs().getConfigs()) {
                actions.add(buildMappingResultActionWithLookupByCommcareCaseId(commcareConfig.getName(), openmrsConfig.getName()));
                actions.add(buildMappingResultActionWithLookupByCommcareAdditionalId(commcareConfig.getName(), openmrsConfig.getName()));
                actions.add(buildMappingResultActionWithLookupByOpemnmrsPatientUuid(commcareConfig.getName(), openmrsConfig.getName()));
                actions.add(buildMappingResultActionWithLookupByOpenmrsPersonUuid(commcareConfig.getName(), openmrsConfig.getName()));
                actions.add(buildMappingResultActionWithLookupByOpenmrsAdditionalId(commcareConfig.getName(), openmrsConfig.getName()));
            }
        }

        return actions;
    }




    private ActionEventRequest buildMappingResultActionWithLookupByCommcareCaseId(String commcareConfigName, String openmrsConfigName) {

        Integer order = 0;

        SortedSet<ActionParameterRequest> parameters = createMappingActionAdditionalParameters(order);
        parameters.add(createMappingActionParameter(DisplayNames.MAPPING_RESULT_COMMCARE_CASE_ID_PARAMTER,
                EventDataKeys.MAPPING_RESULT_ACTION_COMMCARE_CASE_ID,
                UNICODE.getValue(),
                true,
                order));

        return buildMappingResultAction(commcareConfigName,
                openmrsConfigName,
                DisplayNames.MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_COMMCARE_CASE_ID,
                EventSubjects.MAPPING_RESULT_WITH_LOOKUP_BY_COMMCARE_CASE_ID,
                parameters);
    }

    private ActionEventRequest buildMappingResultActionWithLookupByCommcareAdditionalId(String commcareConfigName, String openmrsConfigName) {

        Integer order = 0;

        SortedSet<ActionParameterRequest> parameters = createMappingActionAdditionalParameters(order);
        parameters.add(createMappingActionParameter(DisplayNames.MAPPING_RESULT_COMMCARE_ADDITIONAL_ID_PARAMETER,
                EventDataKeys.MAPPING_RESULT_ACTION_COMMCARE_ADDITIONAL_ID,
                UNICODE.getValue(),
                true,
                order));

        return buildMappingResultAction(commcareConfigName,
                openmrsConfigName,
                DisplayNames.MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_COMMCARE_ADDITIONAL_ID,
                EventSubjects.MAPPING_RESULT_WITH_LOOKUP_BY_COMMCARE_ADDITIONAL_ID,
                parameters);
    }

    private ActionEventRequest buildMappingResultActionWithLookupByOpemnmrsPatientUuid(String commcareConfigName, String openmrsConfigName) {

        Integer order = 0;

        SortedSet<ActionParameterRequest> parameters = createMappingActionAdditionalParameters(order);
        parameters.add(createMappingActionParameter(DisplayNames.MAPPING_RESULT_OPENMRS_PATIENT_UUID_PARAMETER,
                EventDataKeys.MAPPING_RESULT_ACTION_OPENMRS_PATIENT_UUID,
                UNICODE.getValue(),
                true,
                order));

        return buildMappingResultAction(commcareConfigName,
                openmrsConfigName,
                DisplayNames.MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_PATIENT_UUID,
                EventSubjects.MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_PATIENT_UUID,
                parameters);
    }

    private ActionEventRequest buildMappingResultActionWithLookupByOpenmrsPersonUuid(String commcareConfigName, String openmrsConfigName) {

        Integer order = 0;

        SortedSet<ActionParameterRequest> parameters = createMappingActionAdditionalParameters(order);
        parameters.add(createMappingActionParameter(DisplayNames.MAPPING_RESULT_OPENMRS_PERSON_UUID_PARAMETER,
                EventDataKeys.MAPPING_RESULT_ACTION_OPENMRS_PERSON_UUID,
                UNICODE.getValue(),
                true,
                order));

        return buildMappingResultAction(commcareConfigName,
                openmrsConfigName,
                DisplayNames.MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_PERSON_UUID,
                EventSubjects.MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_PERSON_UUID,
                parameters);
    }

    private ActionEventRequest buildMappingResultActionWithLookupByOpenmrsAdditionalId(String commcareConfigName, String openmrsConfigName) {

        Integer order = 0;

        SortedSet<ActionParameterRequest> parameters = createMappingActionAdditionalParameters(order);
        parameters.add(createMappingActionParameter(DisplayNames.MAPPING_RESULT_OPENMRS_ADDITIONAL_ID_PARAMETER,
                EventDataKeys.MAPPING_RESULT_ACTION_OPENMRS_ADDITIONAL_ID,
                UNICODE.getValue(),
                true,
                order));

        return buildMappingResultAction(commcareConfigName,
                openmrsConfigName,
                DisplayNames.MAPPING_RESULT_ACTION_WITH_LOOKUP_BY_OPENMRS_ADDITIONAL_ID,
                EventSubjects.MAPPING_RESULT_WITH_LOOKUP_BY_OPENMRS_ADDITIONAL_ID,
                parameters);
    }

    private ActionEventRequest buildMappingResultAction(String commcareConfigName, String openmrsConfigName, String displayName, String eventSubject, SortedSet<ActionParameterRequest> parameters) {

        String actionDisplayName = DisplayNameHelper.buildDisplayName(displayName, commcareConfigName, openmrsConfigName);
        ActionEventRequestBuilder actionBuilder = new ActionEventRequestBuilder()
                .setDisplayName(actionDisplayName)
                .setSubject(eventSubject + "." + commcareConfigName + "." + openmrsConfigName)
                .setActionParameters(parameters);

        return actionBuilder.createActionEventRequest();
    }

    private ActionParameterRequest createMappingActionParameter(String displayName, String eventKey, String type, boolean required, Integer order) {

        ActionParameterRequestBuilder builder;

        builder = new ActionParameterRequestBuilder()
                .setDisplayName(displayName)
                .setKey(eventKey)
                .setType(type)
                .setRequired(required)
                .setOrder(order++);

        return builder.createActionParameterRequest();
    }

    private SortedSet<ActionParameterRequest> createMappingActionAdditionalParameters(Integer order) {

        ActionParameterRequestBuilder builder;
        SortedSet<ActionParameterRequest> parameters = new TreeSet<>();

        builder = new ActionParameterRequestBuilder()
                .setDisplayName(DisplayNames.PARAMETER_1)
                .setKey(EventDataKeys.MAPPING_RESULT_ACTION_PARAMETER_1)
                .setType(UNICODE.getValue())
                .setRequired(true)
                .setOrder(order++);
        parameters.add(builder.createActionParameterRequest());

        builder = new ActionParameterRequestBuilder()
                .setDisplayName(DisplayNames.PARAMETER_1)
                .setKey(EventDataKeys.MAPPING_RESULT_ACTION_PARAMETER_2)
                .setType(UNICODE.getValue())
                .setRequired(false)
                .setOrder(order++);
        parameters.add(builder.createActionParameterRequest());

        return parameters;
    }
}
