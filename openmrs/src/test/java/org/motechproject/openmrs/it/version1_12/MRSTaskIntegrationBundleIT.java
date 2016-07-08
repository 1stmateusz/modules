package org.motechproject.openmrs.it.version1_12;

import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.config.domain.SettingsRecord;
import org.motechproject.config.mds.SettingsDataService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.openmrs.domain.*;
import org.motechproject.openmrs.exception.ConceptNameAlreadyInUseException;
import org.motechproject.openmrs.service.*;
import org.motechproject.openmrs.tasks.OpenMRSTasksNotifier;
import org.motechproject.openmrs.tasks.constants.Keys;
import org.motechproject.tasks.domain.mds.channel.Channel;
import org.motechproject.tasks.domain.mds.channel.EventParameter;
import org.motechproject.tasks.domain.mds.channel.TriggerEvent;
import org.motechproject.tasks.domain.mds.task.DataSource;
import org.motechproject.tasks.domain.mds.task.Lookup;
import org.motechproject.tasks.domain.mds.task.Task;
import org.motechproject.tasks.domain.mds.task.TaskActionInformation;
import org.motechproject.tasks.domain.mds.task.TaskConfig;
import org.motechproject.tasks.domain.mds.task.TaskConfigStep;
import org.motechproject.tasks.domain.mds.task.TaskTriggerInformation;
import org.motechproject.tasks.osgi.test.AbstractTaskBundleIT;
import org.motechproject.tasks.service.ChannelService;
import org.motechproject.tasks.service.TaskActivityService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private SettingsDataService settingsDataService;

    @Inject
    private OpenMRSPersonService personService;

    @Inject
    private OpenMRSObservationService observationService;

    @Inject
    private OpenMRSConceptService conceptService;

    private Patient createdPatient;
    private ProgramEnrollment createdProgramEnrollment;
    private Encounter createdEncounter;
    private Provider createdProvider;

    private static final String OPENMRS_CHANNEL_NAME = "org.motechproject.openmrs";
    private static final String OPENMRS_MODULE_NAME = "openMRS";
    private static final String MDS_CHANNEL_NAME = "org.motechproject.motech-platform-dataservices-entities";
    private static final String VERSION = "0.29.0.SNAPSHOT";
    private static final String TEST_INTERFACE = "org.motechproject.openmrs.tasks.OpenMRSActionProxyService";
    private static final String TRIGGER_SUBJECT = "mds.crud.serverconfig.SettingsRecord.CREATE";
    private static final String MOTECH_ID = "602";

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

        waitForChannel(OPENMRS_CHANNEL_NAME);
        Channel channel = findChannel(OPENMRS_CHANNEL_NAME);
        waitForChannel(MDS_CHANNEL_NAME);
        Channel mdsChannel = findChannel(MDS_CHANNEL_NAME);

        createdPatient = patientService.createPatient(DEFAULT_CONFIG_NAME, preparePatient());
        createdProvider = providerService.createProvider(DEFAULT_CONFIG_NAME, prepareProvider());
    }

    @Test
    public void testOpenMRSProgramEnrollmentDataSource() throws InterruptedException, IOException {
        createProgramEnrollmentTestData();
        Long taskID = createProgramEnrollmentTestTask();

        activateTrigger();

        // Give Tasks some time to process
        waitForTaskExecution();

        deleteTask(taskID);

        assertTrue(checkIfProgramEnrollmentWasCreatedProperly());
    }

    @Test
    public void testOpenMRSEncounterDataSource() throws InterruptedException, IOException, ParseException, ConceptNameAlreadyInUseException {
        createEncounterTestData();
        Long taskID = createEncounterTestTask();

        activateTrigger();

        // Give Tasks some time to process
        waitForTaskExecution();

        deleteTask(taskID);

        assertTrue(checkIfEncounterWasCreatedProperly());
    }

    @Test
    public void testOpenMRSPatientDataSourceAndCreatePatientAction() throws InterruptedException, IOException, ParseException, ConceptNameAlreadyInUseException {
        Long taskID = createPatientTestTask();

        activateTrigger();

        // Give Tasks some time to process
        waitForTaskExecution();

        deleteTask(taskID);

        assertTrue(checkIfPatientWasCreatedProperly());
    }

    @Test
    public void testOpenMRSProviderDataSourceAndCreateEncounterAction() throws InterruptedException, IOException, ParseException, ConceptNameAlreadyInUseException {
        Long taskID = createProviderTestTask();

        activateTrigger();

        // Give Tasks some time to process
        waitForTaskExecution();

        deleteTask(taskID);

        assertTrue(checkIfProviderWasCreatedProperly());
    }

    private Long createProgramEnrollmentTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("CREATE SettingsRecord", "data-services", MDS_CHANNEL_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Program Enrollment [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_CHANNEL_NAME, VERSION, TEST_INTERFACE, "createProgramEnrollment");

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

        Task task = new Task("OpenTestTask1", triggerInformation, Arrays.asList(actionInformation), taskConfig, true, true);
        getTaskService().save(task);

        getTriggerHandler().registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());

        return task.getId();
    }

    private Long createEncounterTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("CREATE SettingsRecord", "data-services", MDS_CHANNEL_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Encounter [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_CHANNEL_NAME, VERSION, TEST_INTERFACE, "createEncounter");

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

    private Long createPatientTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("CREATE SettingsRecord", "data-services", MDS_CHANNEL_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Patient [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_CHANNEL_NAME, VERSION, TEST_INTERFACE, "createPatient");

        actionInformation.setSubject("validate");

        SortedSet<TaskConfigStep> taskConfigStepSortedSet = new TreeSet<>();
        taskConfigStepSortedSet.add(createPatientDataSource());
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addAll(taskConfigStepSortedSet);

        Map<String, String> values = new HashMap<>();
        values.put(Keys.BIRTH_DATE, "{{ad.openMRS.Patient-" + DEFAULT_CONFIG_NAME + "#0.person.birthdate}}");
        values.put(Keys.FAMILY_NAME, "{{ad.openMRS.Patient-" + DEFAULT_CONFIG_NAME + "#0.person.display}}");
        values.put(Keys.GENDER, "{{ad.openMRS.Patient-" + DEFAULT_CONFIG_NAME + "#0.person.gender}}");
        values.put(Keys.GIVEN_NAME, "{{ad.openMRS.Patient-" + DEFAULT_CONFIG_NAME + "#0.person.display}}");
        values.put(Keys.MOTECH_ID, "{{ad.openMRS.Patient-" + DEFAULT_CONFIG_NAME + "#0.motechId}}");
        values.put(Keys.CONFIG_NAME, DEFAULT_CONFIG_NAME);
        actionInformation.setValues(values);

        Task task = new Task("OpenMRSPatientTestTask", triggerInformation, Arrays.asList(actionInformation), taskConfig, true, true);
        getTaskService().save(task);

        getTriggerHandler().registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());

        return task.getId();
    }

    private Long createProviderTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("CREATE SettingsRecord", "data-services", MDS_CHANNEL_NAME,
                VERSION, TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Encounter [" + DEFAULT_CONFIG_NAME + "]", OPENMRS_CHANNEL_NAME,
                OPENMRS_CHANNEL_NAME, VERSION, TEST_INTERFACE, "createEncounter");

        actionInformation.setSubject("validate");

        SortedSet<TaskConfigStep> taskConfigStepSortedSet = new TreeSet<>();
        taskConfigStepSortedSet.add(createProviderDataSource());
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addAll(taskConfigStepSortedSet);

        Map<String, String> values = new HashMap<>();
        values.put(Keys.PROVIDER_UUID, "{{ad.openMRS.Provider-TestOpenMRSServer#0.uuid}}");
        values.put(Keys.ENCOUNTER_DATE, createdEncounter.getEncounterDatetime().toString());
        values.put(Keys.ENCOUNTER_TYPE, createdEncounter.getEncounterType().toString());
        values.put(Keys.PATIENT_UUID, createdEncounter.getPatient().getUuid());
        values.put(Keys.CONFIG_NAME, DEFAULT_CONFIG_NAME);
        actionInformation.setValues(values);

        Task task = new Task("OpenMRSPatientTestTask", triggerInformation, Arrays.asList(actionInformation), taskConfig, true, true);
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

    private void createEncounterTestData() throws ParseException, ConceptNameAlreadyInUseException {
        Observation observation = prepareObservations();

        Location location = locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0);
        Encounter encounter = new Encounter(location, prepareEncounterType(), prepareProviderDate(), createdPatient, Collections.singletonList(createdProvider.getPerson()),
                Collections.singletonList(observation));

        createdEncounter = encounterService.createEncounter(DEFAULT_CONFIG_NAME, encounter);
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

    private Provider prepareProvider() {

        Person person = new Person();

        Person.Name name = new Person.Name();
        name.setGivenName("John");
        name.setFamilyName("Snow");
        person.setNames(Collections.singletonList(name));

        person.setGender("M");

        person = personService.createPerson(DEFAULT_CONFIG_NAME, person);

        Provider provider = new Provider();
        provider.setIdentifier("KingOfTheNorth");
        provider.setPerson(person);

        return provider;
    }

    private Observation prepareObservations() throws ParseException, ConceptNameAlreadyInUseException {
        Observation tempObservation = new Observation();

        tempObservation.setObsDatetime(prepareProviderDate());
        tempObservation.setConcept(prepareConcept());
        tempObservation.setValue(new Observation.ObservationValue("true"));
        tempObservation.setPerson(createdPatient.getPerson());

        String observationUuid = observationService.createObservation(DEFAULT_CONFIG_NAME, tempObservation).getUuid();
        return observationService.getObservationByUuid(DEFAULT_CONFIG_NAME, observationUuid);
    }

    private EncounterType prepareEncounterType() {
        EncounterType tempEncounterType = new EncounterType("FooType");
        tempEncounterType.setDescription("FooDescription");

        String encounterUuid = encounterService.createEncounterType(DEFAULT_CONFIG_NAME, tempEncounterType).getUuid();
        return encounterService.getEncounterTypeByUuid(DEFAULT_CONFIG_NAME, encounterUuid);
    }

    private Concept prepareConcept() throws ConceptNameAlreadyInUseException {
        Concept tempConcept = new Concept();
        tempConcept.setNames(Arrays.asList(new ConceptName("FooConcept")));
        tempConcept.setDatatype(new Concept.DataType("Boolean"));
        tempConcept.setConceptClass(new Concept.ConceptClass("Test"));
        return conceptService.createConcept(DEFAULT_CONFIG_NAME, tempConcept);
    }

    private Date prepareProviderDate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse("2011-01-16T00:00:00Z");
    }

    private void waitForTaskExecution() throws InterruptedException {
        getLogger().info("testOpenMRSTasksIntegration starts waiting for task to execute");
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
        DataSource dataSource = new DataSource(OPENMRS_MODULE_NAME, new Long(4), new Long(0), "ProgramEnrollment-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.motechIdAndProgramName", lookupList, false);
        dataSource.setOrder(0);
        return dataSource;
    }

    private DataSource createEncounterDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        lookupList.add(new Lookup("openMRS.UUID", createdEncounter.getUuid()));
        DataSource dataSource = new DataSource(OPENMRS_MODULE_NAME, new Long(4), new Long(0), "Encounter-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.uuid", lookupList, false);
        dataSource.setOrder(0);
        return dataSource;
    }

    private DataSource createPatientDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        lookupList.add(new Lookup("openMRS.UUID", createdPatient.getUuid()));
        DataSource dataSource = new DataSource(OPENMRS_MODULE_NAME, new Long(4), new Long(0), "Patient-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.uuid", lookupList, false);
        dataSource.setOrder(0);
        return dataSource;
    }

    private DataSource createProviderDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        lookupList.add(new Lookup("openMRS.UUID", createdProvider.getUuid()));
        DataSource dataSource = new DataSource(OPENMRS_MODULE_NAME, new Long(4), new Long(0), "Provider-" + DEFAULT_CONFIG_NAME, "openMRS.lookup.uuid", lookupList, false);
        dataSource.setOrder(0);
        return dataSource;
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
        List<Encounter> encounterList = encounterService.getEncountersByEncounterType(DEFAULT_CONFIG_NAME, MOTECH_ID, createdEncounter.getEncounterType().toString());
        for (Encounter encounter : encounterList) {

        }
        return encounterCount == 2 ? true : false;
    }

    private boolean checkIfPatientWasCreatedProperly() {
        int patientCount = 0;
        List<Patient> patientList = patientService.search(DEFAULT_CONFIG_NAME, createdPatient.getDisplay(), MOTECH_ID);
        for (Patient patient : patientList) {
            if (patient.getPerson().getBirthdate().equals(createdPatient.getPerson().getBirthdate()) &&
                    patient.getMotechId().equals(createdPatient.getMotechId()) &&
                    patient.getPerson().getGender().equals(createdPatient.getPerson().getGender())) {
                patientCount++;
            }
        }
        return patientCount == 2 ? true : false;
    }

    private boolean checkIfProviderWasCreatedProperly() {
        int patientCount = 0;
        List<Patient> patientList = patientService.search(DEFAULT_CONFIG_NAME, createdPatient.getDisplay(), MOTECH_ID);
        for (Patient patient : patientList) {
            if (patient.getPerson().getBirthdate().equals(createdPatient.getPerson().getBirthdate()) &&
                    patient.getMotechId().equals(createdPatient.getMotechId()) &&
                    patient.getPerson().getGender().equals(createdPatient.getPerson().getGender())) {
                patientCount++;
            }
        }
        return patientCount == 2 ? true : false;
    }

    private void activateTrigger() {
        settingsDataService.create(new SettingsRecord());
    }

    private void deleteTask(Long taskID) {
        getTaskService().deleteTask(taskID);
    }
}

