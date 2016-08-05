package org.motechproject.mapping.module.tasks.builder;

import org.motechproject.commcare.config.Config;
import org.motechproject.commcare.service.CommcareConfigService;
import org.motechproject.mapping.module.events.constants.DisplayNames;
import org.motechproject.mapping.module.events.constants.EventDataKeys;
import org.motechproject.mapping.module.events.constants.EventSubjects;
import org.motechproject.openmrs.service.OpenMRSConfigService;
import org.motechproject.tasks.contract.EventParameterRequest;
import org.motechproject.tasks.contract.TriggerEventRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>MapResultTriggerBuilder</code> class builds Commcare related task triggers, that do not depend on the current
 * schema or database state.
 */
public class MapResultTriggerBuilder implements TriggerBuilder {

    private CommcareConfigService commcareConfigService;

    private OpenMRSConfigService openMRSConfigService;

    /**
     * Creates an instance of the {@link MapResultTriggerBuilder} class, that is used for creating common triggers. It will
     * use the given {@code configService} for building common triggers.
     *
     * @param commcareConfigService  the configuration service
     * @param openMRSConfigService  the configuration service
     */
    public MapResultTriggerBuilder(CommcareConfigService commcareConfigService, OpenMRSConfigService openMRSConfigService) {
        this.commcareConfigService = commcareConfigService;
        this.openMRSConfigService = openMRSConfigService;
    }

    @Override
    public List<TriggerEventRequest> buildTriggers() {
        List<TriggerEventRequest> triggers = new ArrayList<>();
        triggers.addAll(buildMapResultTriggers());

        return triggers;
    }

    private List<TriggerEventRequest> buildMapResultTriggers() {

        List<TriggerEventRequest> triggers = new ArrayList<>();

        for (Config commcareConfig: commcareConfigService.getConfigs().getConfigs()) {
            for (org.motechproject.openmrs.config.Config openmrsConfig: openMRSConfigService.getConfigs().getConfigs()) {

                List<EventParameterRequest> parameterRequests = new ArrayList<>();

                parameterRequests.add(new EventParameterRequest(DisplayNames.COMMCARE_CONFIG_NAME, EventDataKeys.COMMCATE_CONFIG));
                parameterRequests.add(new EventParameterRequest(DisplayNames.OPENMRS_CONFIG_NAME, EventDataKeys.OPENMRS_CONFIG));

                parameterRequests.add(new EventParameterRequest(DisplayNames.COMMCARE_CASE_ID, EventDataKeys.COMMCARE_CASID));
                parameterRequests.add(new EventParameterRequest(DisplayNames.COMMCARE_ADDITIONAL_ID, EventDataKeys.COMMCARE_ADDITIONAL_ID));
                parameterRequests.add(new EventParameterRequest(DisplayNames.OPENMRS_PATIENT_UUID, EventDataKeys.OPENMRS_PATIENT_UUID));
                parameterRequests.add(new EventParameterRequest(DisplayNames.OPENMRS_PERSON_UUID, EventDataKeys.OPENMRS_PERSON_UUID));
                parameterRequests.add(new EventParameterRequest(DisplayNames.OPENMRS_PATIENT_ID_MOTECH_ID, EventDataKeys.OPENMRS_PATIENT_ID_MOTECH_ID));
                parameterRequests.add(new EventParameterRequest(DisplayNames.OPENMRS_ADDITIONAL_ID, EventDataKeys.OPENMRS_ADDDTIONAL_ID));

                String displayName = DisplayNameHelper.buildDisplayName(DisplayNames.MAPPING_RESULT_TRIGGER, commcareConfig.getName(), openmrsConfig.getName());

                triggers.add(new TriggerEventRequest(displayName, EventSubjects.MAPPING_RESULT + "." + commcareConfig.getName() + "." + openmrsConfig.getName(), null,
                parameterRequests));
            }
        }

        return triggers;
    }
}
