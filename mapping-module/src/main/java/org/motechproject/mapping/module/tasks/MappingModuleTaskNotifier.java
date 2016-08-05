package org.motechproject.mapping.module.tasks;

import org.motechproject.commcare.service.CommcareConfigService;
import org.motechproject.mapping.module.tasks.builder.ChannelRequestBuilder;
import org.motechproject.openmrs.service.OpenMRSConfigService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * The <code>CommcareTasksNotifier</code> class is responsible for communication
 * with the tasks module. It sends {@link org.motechproject.tasks.contract.ChannelRequest}s
 * in order to create or update tasks channel for the Commcare module. If the tasks module
 * is not present, no updates will be sent.
 */
@Component("mappingModuleTasksNotifier")
public class MappingModuleTaskNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingModuleTaskNotifier.class);

    private BundleContext bundleContext;
    private CommcareConfigService commcareConfigService;
    private OpenMRSConfigService openMRSConfigService;

    @Autowired
    public MappingModuleTaskNotifier(BundleContext bundleContext, CommcareConfigService commcareConfigService, OpenMRSConfigService openMRSConfigService) {
        this.bundleContext = bundleContext;
        this.commcareConfigService = commcareConfigService;
        this.openMRSConfigService = openMRSConfigService;
    }

    @PostConstruct
    public void updateTasksInfo() {
        LOGGER.info("Updating tasks integration");

        try {
            updateChannel();
        } catch (RuntimeException e) {
            LOGGER.error("Channel generated was not accepted by tasks due to validation errors", e);
        }
    }

    private void updateChannel() {
        ServiceReference serviceReference = bundleContext.getServiceReference("org.motechproject.tasks.service.ChannelService");
        if (serviceReference != null) {
            Object service = bundleContext.getService(serviceReference);
            if (service != null) {
                LOGGER.info("Registering Commcare tasks channel with the channel service");

                ChannelRequestBuilder channelRequestBuilder = new ChannelRequestBuilder(commcareConfigService, openMRSConfigService, bundleContext);
                TasksChannelServiceInstance instance = new TasksChannelServiceInstance(service, channelRequestBuilder);
                instance.updateTaskChannel();
            } else {
                LOGGER.warn("No channel service present, channel not registered");
            }
        }
    }
}
