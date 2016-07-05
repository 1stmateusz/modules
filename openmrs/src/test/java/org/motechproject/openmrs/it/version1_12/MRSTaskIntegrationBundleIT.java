package org.motechproject.openmrs.it.version1_12;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.openmrs.domain.Encounter;
import org.motechproject.openmrs.domain.EncounterType;
import org.motechproject.openmrs.domain.Location;
import org.motechproject.openmrs.domain.Patient;
import org.motechproject.openmrs.domain.Person;
import org.motechproject.openmrs.domain.Program;
import org.motechproject.openmrs.domain.ProgramEnrollment;
import org.motechproject.openmrs.domain.Provider;
import org.motechproject.openmrs.service.OpenMRSEncounterService;
import org.motechproject.openmrs.service.OpenMRSLocationService;
import org.motechproject.openmrs.service.OpenMRSPatientService;
import org.motechproject.openmrs.service.OpenMRSProgramEnrollmentService;
import org.motechproject.openmrs.service.OpenMRSProviderService;
import org.motechproject.openmrs.tasks.OpenMRSTasksNotifier;
import org.motechproject.openmrs.tasks.constants.Keys;
import org.motechproject.tasks.domain.mds.channel.Channel;
import org.motechproject.tasks.domain.mds.task.DataSource;
import org.motechproject.tasks.domain.mds.task.Lookup;
import org.motechproject.tasks.domain.mds.task.Task;
import org.motechproject.tasks.domain.mds.task.TaskActionInformation;
import org.motechproject.tasks.domain.mds.task.TaskConfig;
import org.motechproject.tasks.domain.mds.task.TaskConfigStep;
import org.motechproject.tasks.domain.mds.task.TaskTriggerInformation;
import org.motechproject.tasks.osgi.test.AbstractTaskBundleIT;
import org.motechproject.tasks.service.ChannelService;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.helper.ServiceRetriever;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.motechproject.openmrs.tasks.OpenMRSActionProxyService.DEFAULT_LOCATION_NAME;
import static org.motechproject.openmrs.util.TestConstants.DEFAULT_CONFIG_NAME;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MRSTaskIntegrationBundleIT extends AbstractTaskBundleIT {

    private static final int PORT = TestContext.getJettyPort();

    private OpenMRSTasksNotifier openMRSTasksNotifier;

    @Inject
    private BundleContext bundleContext;

    @Inject
    private OpenMRSProgramEnrollmentService programEnrollmentService;

    @Inject
    private OpenMRSPatientService patientService;

    @Inject
    private OpenMRSEncounterService encounterService;

    @Inject
    private OpenMRSLocationService locationService;

    @Inject
    private OpenMRSProviderService providerService;

    @Inject
    EventRelay eventRelay;

    private ChannelService channelService = getChannelService();

    private Patient createdPatient;
    private ProgramEnrollment createdProgramEnrollment;
    private Encounter createdEncounter;
    private Provider createdProvider;

    private static final String OPENMRS_CHANNEL_NAME = "openMRS";
    private static final String OPENMRS_TEST_TRIGGER_CHANNEL_NAME = "openMRSTestChannel";
    private static final String OPENMRS_MODULE_NAME = "org.motechproject.mrs";
    private static final String VERSION = "0.29";
    private static final String TEST_INTERFACE = "org.motechproject.openmrs.tasks.OpenMRSActionProxyService";
    private static final String TRIGGER_SUBJECT = "mds.crud.websecurity.MotechPermission.CREATE";
    private static final String TEST_TRIGGER_SUBJECT = "openMRSTaskTrigger";
    private static final String MOTECH_ID = "602";
    private static final String ENCOUNTER_TYPE = "ADULTINITIAL";

    private static final Integer MAX_RETRIES_BEFORE_FAIL = 20;
    private static final Integer WAIT_TIME = 2000;

    @Override
    protected Collection<String> getAdditionalTestDependencies() {
        return Arrays.asList(
                "org.motechproject:motech-tasks-test-utils",
                "org.motechproject:motech-tasks",
                "commons-beanutils:commons-beanutils",
                "commons-fileupload:commons-fileupload",
                "org.motechproject:motech-platform-web-security",
                "org.motechproject:motech-platform-server-bundle",
                "org.openid4java:com.springsource.org.openid4java",
                "net.sourceforge.nekohtml:com.springsource.org.cyberneko.html",
                "org.springframework.security:spring-security-openid"
        );
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        createAdminUser();
        login();
        openMRSTasksNotifier = (OpenMRSTasksNotifier) ServiceRetriever.getWebAppContext(bundleContext, OPENMRS_CHANNEL_NAME).getBean("openMrsTasksNotifier");
        setUpSecurityContext("motech", "motech", "manageTasks", "manageOpenMRS");

        registerTriggerChannel();
        waitForChannel(OPENMRS_CHANNEL_NAME);
        waitForChannel(OPENMRS_TEST_TRIGGER_CHANNEL_NAME);
        Channel channel = findChannel(OPENMRS_CHANNEL_NAME);

        createdPatient = patientService.createPatient(DEFAULT_CONFIG_NAME, preparePatient());
    }

    @Test
    public void testOpenMRSEncounterDataSource() throws InterruptedException, IOException {
        createEncounterTestData();
        Long taskID = createEncounterTestTask();

        sendMotechEvent(TEST_TRIGGER_SUBJECT);

        // Give Tasks some time to process
        waitForTaskExecution();
        // Ask our OSGi service, which acts as Task action, to verify that correct values were received
        assertTrue(checkIfEncounterWasCreatedProperly());

        deleteTask(taskID);
    }

    @Test
    public void testOpenMRSProgramEnrollmentDataSource() throws InterruptedException, IOException {
        createProgramEnrollmentTestData();
        Long taskID = createProgramEnrollmentTestTask();

        sendMotechEvent(TEST_TRIGGER_SUBJECT);

        // Give Tasks some time to process
        waitForTaskExecution();
        // Ask our OSGi service, which acts as Task action, to verify that correct values were received
        assertTrue(checkIfProgramEnrollmentWasCreatedProperly());

        deleteTask(taskID);
    }

    private Long createProgramEnrollmentTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("trigger", OPENMRS_TEST_TRIGGER_CHANNEL_NAME, OPENMRS_MODULE_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Program Enrollment [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_MODULE_NAME, VERSION, TEST_INTERFACE, "createProgramEnrollment");

        actionInformation.setSubject("validate");

        SortedSet<TaskConfigStep> taskConfigStepSortedSet = new TreeSet<>();
        taskConfigStepSortedSet.add(createProgramEnrollmentDataSource());
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addAll(taskConfigStepSortedSet);

        Map<String, String> values = new HashMap<>();
        values.put(Keys.PATIENT_UUID, "{{ad.openMRS.ProgramEnrollment-" + DEFAULT_CONFIG_NAME + "#0.patient.uuid}}");
        values.put(Keys.PROGRAM_UUID, "{{ad.openMRS.ProgramEnrollment-" + DEFAULT_CONFIG_NAME + "#0.program.uuid}}");
        values.put(Keys.DATE_ENROLLED, new DateTime("2010-01-16T00:00:00Z").toString());
        values.put(Keys.DATE_COMPLETED, new DateTime("2016-01-16T00:00:00Z").toString());
        values.put(Keys.LOCATION_NAME, locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0).toString());
        values.put(Keys.CONFIG_NAME, DEFAULT_CONFIG_NAME);
        actionInformation.setValues(values);

        Task task = new Task("OpenTestTask", triggerInformation, Arrays.asList(actionInformation), taskConfig, true, true);
        getTaskService().save(task);

        getTriggerHandler().registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());

        return task.getId();
    }

    private Long createEncounterTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("trigger", OPENMRS_TEST_TRIGGER_CHANNEL_NAME, OPENMRS_MODULE_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Encounter [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_MODULE_NAME, VERSION, TEST_INTERFACE, "createEncounter");

        actionInformation.setSubject("validate");

        SortedSet<TaskConfigStep> taskConfigStepSortedSet = new TreeSet<>();
        taskConfigStepSortedSet.add(createEncounterDataSource());
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addAll(taskConfigStepSortedSet);

        Map<String, String> values = new HashMap<>();
        values.put(Keys.PROVIDER_UUID, "{{ad.openMRS.Encounter-" + DEFAULT_CONFIG_NAME + "#0.provider.uuid}}");
        values.put(Keys.PATIENT_UUID, "{{ad.openMRS.Encounter-" + DEFAULT_CONFIG_NAME + "#0.patient.uuid}}");
        values.put(Keys.ENCOUNTER_TYPE, "{{ad.openMRS.Encounter-" + DEFAULT_CONFIG_NAME + "#0.encounterType.name}}");
        values.put(Keys.ENCOUNTER_DATE, "{{ad.openMRS.Encounter-" + DEFAULT_CONFIG_NAME + "#0.encounterDatetime}}");
        values.put(Keys.LOCATION_NAME, "{{ad.openMRS.Encounter-" + DEFAULT_CONFIG_NAME + "#0.location.display}}");
        values.put(Keys.CONFIG_NAME, DEFAULT_CONFIG_NAME);
        actionInformation.setValues(values);

        Task task = new Task("OpenMRSEncounterTestTask", triggerInformation, Arrays.asList(actionInformation), taskConfig, true, true);
        getTaskService().save(task);

        getTriggerHandler().registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());

        return task.getId();
    }

    private void createProgramEnrollmentTestData() {
        Program program = new Program();
        program.setUuid("187af646-373b-4459-8114-4724d7e07fd5");

        DateTime dateEnrolled = new DateTime("2010-01-16T00:00:00Z");
        DateTime dateCompleted = new DateTime("2016-01-16T00:00:00Z");
        DateTime stateStartDate = new DateTime("2011-01-16T00:00:00Z");

        Location location = locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0);

        Program.State state = new Program.State();
        state.setUuid("6ac1bb86-f7ef-438c-8ea6-33050caa350d");

        ProgramEnrollment.StateStatus stateStatus = new ProgramEnrollment.StateStatus();
        stateStatus.setState(state);
        stateStatus.setStartDate(stateStartDate.toDate());

        ProgramEnrollment programEnrollment = new ProgramEnrollment();
        programEnrollment.setProgram(program);
        programEnrollment.setPatient(createdPatient);
        programEnrollment.setDateEnrolled(dateEnrolled.toDate());
        programEnrollment.setDateCompleted(dateCompleted.toDate());
        programEnrollment.setLocation(location);
        programEnrollment.setStates(Collections.singletonList(stateStatus));

        createdProgramEnrollment = programEnrollmentService.createProgramEnrollment(DEFAULT_CONFIG_NAME, programEnrollment);
    }

    private void createEncounterTestData() {
        List<Person> providersList = new ArrayList<>();
        providersList.add(prepareProvider());
        Encounter encouter = new Encounter(locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0), new EncounterType(ENCOUNTER_TYPE),
                new Date(), createdPatient, providersList);

        createdEncounter = encounterService.createEncounter(DEFAULT_CONFIG_NAME, encouter);
    }

    private Patient preparePatient() {
        Person person = new Person();

        Person.Name name = new Person.Name();
        name.setGivenName("John");
        name.setFamilyName("Smith");
        person.setNames(Collections.singletonList(name));

        Person.Address address = new Person.Address();
        address.setAddress1("10 Fifth Avenue");
        person.setAddresses(Collections.singletonList(address));

        person.setBirthdateEstimated(false);
        person.setGender("M");

        Location location = locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0);

        assertNotNull(location);

        return new Patient(person, MOTECH_ID, location);
    }

    private Person prepareProvider() {
        Person person = new Person();

        Person.Name name = new Person.Name();
        name.setGivenName("John");
        name.setFamilyName("Snow");
        person.setNames(Collections.singletonList(name));

        Person.Address address = new Person.Address();
        address.setAddress1("1 Castle Black");
        person.setAddresses(Collections.singletonList(address));

        person.setBirthdateEstimated(false);
        person.setGender("M");

        Location location = locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0);

        assertNotNull(location);

        Provider provider = new Provider(person);

        createdProvider = providerService.createProvider(DEFAULT_CONFIG_NAME, provider);

        return person;
    }

    private void waitForTaskExecution() throws InterruptedException {
        getLogger().info("testCommcareTasksIntegration starts waiting for task to execute");
        int retries = 0;
        while (retries < MAX_RETRIES_BEFORE_FAIL) {
            retries++;
            Thread.sleep(WAIT_TIME);
        }
        getLogger().info("Task executed after " + retries + " retries, what took about "
                + (retries * WAIT_TIME) / 1000 + " seconds");
    }

    private DataSource createProgramEnrollmentDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        lookupList.add(new Lookup("openMRS.patient.motechId", MOTECH_ID));
        lookupList.add(new Lookup("openMRS.programName", createdProgramEnrollment.getProgram().getName()));
        return new DataSource(OPENMRS_CHANNEL_NAME, new Long(5), new Long(0), "ProgramEnrollment-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.motechIdAndProgramName", lookupList, false);
    }

    private DataSource createEncounterDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        lookupList.add(new Lookup("openMRS.UUID", createdEncounter.getUuid()));
        return new DataSource(OPENMRS_CHANNEL_NAME, new Long(5), new Long(0), "Encounter-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.uuid", lookupList, false);
    }

    private boolean checkIfProgramEnrollmentWasCreatedProperly() {
        int programEnrollmentCount = 0;
        List<ProgramEnrollment> programEnrollmentList = programEnrollmentService.getProgramEnrollmentByPatientUuid(DEFAULT_CONFIG_NAME, createdProgramEnrollment.getPatient().getUuid());
        for (ProgramEnrollment programEnrollment : programEnrollmentList) {
            if (programEnrollment.getPatient().getUuid().equals(createdProgramEnrollment.getPatient().getUuid()) && programEnrollment.getProgram().getUuid().equals(createdProgramEnrollment.getProgram().getUuid())) {
                programEnrollmentCount++;
            }
        }
        return programEnrollmentCount == 2 ? true : false;
    }

    private boolean checkIfEncounterWasCreatedProperly() {
        int encounterCount = 0;
        List<Encounter> encounterList = encounterService.getEncountersByEncounterType(DEFAULT_CONFIG_NAME, MOTECH_ID, ENCOUNTER_TYPE);
        for (Encounter encounter : encounterList) {

        }
        return encounterCount == 2 ? true : false;
    }

    private void registerTriggerChannel() {
        String triggerEvent = String.format("{ displayName: 'openMRSTestTrigger', subject: '%s', eventParameters: [{ displayName: 'test', eventKey: 'key' }] }", TEST_TRIGGER_SUBJECT);
        String channel = String.format("{displayName: %s, triggerTaskEvents: [%s]}", OPENMRS_TEST_TRIGGER_CHANNEL_NAME, triggerEvent);
        InputStream stream = new ByteArrayInputStream(channel.getBytes(Charset.forName("UTF-8")));

        channelService.registerChannel(stream, OPENMRS_TEST_TRIGGER_CHANNEL_NAME, VERSION);
    }

    private void sendMotechEvent(String subject) {
        MotechEvent motechEvent = new MotechEvent(subject);
        eventRelay.sendEventMessage(motechEvent);
    }

    private void deleteTask(Long taskID) {
        getTaskService().deleteTask(taskID);
    }
}

