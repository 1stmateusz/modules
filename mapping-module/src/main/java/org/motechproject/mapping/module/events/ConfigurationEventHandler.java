package org.motechproject.mapping.module.events;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mapping.module.tasks.MappingModuleTaskNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationEventHandler {

    @Autowired
    private MappingModuleTaskNotifier mappingModuleTaskNotifier;

    /**
     * Responsible for handling {@code CONFIG_CREATED} event. This event is fired when user creates a new configuration.
     * Handling this event will result in adding new configuration and downloading applications related with it.
     *
     * @param event  the event to be handled
     */
    @MotechListener(subjects = CONFIG_CREATED, CONFIG_UPDATED, CONFIG_DELETED)
    public synchronized void configCreated(MotechEvent event) {
        String configName = (String) event.getParameters().get(EventDataKeys.CONFIG_NAME);
        configurationManager.configCreated(configName);
    }
}
