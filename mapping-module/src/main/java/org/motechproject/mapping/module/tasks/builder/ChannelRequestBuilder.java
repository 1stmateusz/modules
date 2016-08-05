package org.motechproject.mapping.module.tasks.builder;

import org.motechproject.commcare.service.CommcareConfigService;
import org.motechproject.openmrs.service.OpenMRSConfigService;
import org.motechproject.tasks.contract.ActionEventRequest;
import org.motechproject.tasks.contract.ChannelRequest;
import org.motechproject.tasks.contract.TriggerEventRequest;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>ChannelRequestBuilder</code> class is responsible for building the
 * {@link org.motechproject.tasks.contract.ChannelRequest}. Such request is later on
 * used to register the channel for the Commcare module. To build the necessary triggers,
 * we use classes implementing {@link TriggerBuilder}.
 */
public class ChannelRequestBuilder {

    private static final String DISPLAY_NAME = "commcare-openmrs-id-map";

    private BundleContext bundleContext;
    private CommcareConfigService commcareConfigService;
    private OpenMRSConfigService openMRSConfigService;

    /**
     * Creates an instance of the {@link ChannelRequestBuilder} class, which is used for building {@link ChannelRequest}
     * instances. The given {@code configService}, {@code schemaService}, {@code bundleContext} will be use for
     * building new instances.
     *
     * @param commcareConfigService  the configuration service
     * @param openMRSConfigService the schema service
     * @param bundleContext  the bundle context
     */
    public ChannelRequestBuilder(CommcareConfigService commcareConfigService, OpenMRSConfigService openMRSConfigService,
                                 BundleContext bundleContext) {
        this.commcareConfigService = commcareConfigService;
        this.openMRSConfigService = openMRSConfigService;
        this.bundleContext = bundleContext;
    }

    /**
     * Builds an object of the {@link ChannelRequest} class.
     *
     * @return the created instance
     */
    public ChannelRequest buildChannelRequest() {

        TriggerBuilder mapResultTriggerBuilder = new MapResultTriggerBuilder(commcareConfigService, openMRSConfigService);

        ActionBuilder mappingResultActionBuilder = new MappingResultActionBuilder(commcareConfigService, openMRSConfigService);

        //Triggers
        List<TriggerEventRequest> triggers = new ArrayList<>();
        triggers.addAll(mapResultTriggerBuilder.buildTriggers());

        //Actions
        List<ActionEventRequest> actions = new ArrayList<>();
        actions.addAll(mappingResultActionBuilder.buildActions());

        return new ChannelRequest(DISPLAY_NAME, bundleContext.getBundle().getSymbolicName(),
                bundleContext.getBundle().getVersion().toString(), null, triggers, actions);
    }
}
