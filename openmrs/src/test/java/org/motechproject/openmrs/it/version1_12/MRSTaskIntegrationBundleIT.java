package org.motechproject.openmrs.it.version1_12;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.openmrs.config.Configs;
import org.motechproject.openmrs.domain.Location;
import org.motechproject.openmrs.domain.Patient;
import org.motechproject.openmrs.domain.Person;
import org.motechproject.openmrs.domain.Program;
import org.motechproject.openmrs.domain.ProgramEnrollment;
import org.motechproject.openmrs.service.OpenMRSLocationService;
import org.motechproject.openmrs.service.OpenMRSPatientService;
import org.motechproject.openmrs.service.OpenMRSProgramEnrollmentService;
import org.motechproject.openmrs.tasks.OpenMRSTasksNotifier;
import org.motechproject.openmrs.tasks.constants.DisplayNames;
import org.motechproject.openmrs.tasks.constants.Keys;
import org.motechproject.openmrs.util.DummyConfigsData;
import org.motechproject.openmrs.util.tasks.OpenMRSValidatingChannel;
import org.motechproject.tasks.contract.ActionEventRequest;
import org.motechproject.tasks.contract.ActionParameterRequest;
import org.motechproject.tasks.contract.builder.ActionEventRequestBuilder;
import org.motechproject.tasks.contract.builder.ActionParameterRequestBuilder;
import org.motechproject.tasks.domain.mds.ParameterType;
import org.motechproject.tasks.domain.mds.channel.Channel;
import org.motechproject.tasks.domain.mds.channel.builder.ActionEventBuilder;
import org.motechproject.tasks.domain.mds.task.DataSource;
import org.motechproject.tasks.domain.mds.task.Lookup;
import org.motechproject.tasks.domain.mds.task.Task;
import org.motechproject.tasks.domain.mds.task.TaskActionInformation;
import org.motechproject.tasks.domain.mds.task.TaskTriggerInformation;
import org.motechproject.tasks.osgi.test.AbstractTaskBundleIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.helper.ServiceRetriever;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
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
    private OpenMRSLocationService locationService;

    @Inject
    private OpenMRSValidatingChannel validatingChannel;

    private Configs configs;
    private Patient createdPatient;

    private static final String OPENMRS_CHANNEL_NAME = "openMRS";
    private static final String OPENMRS_MODULE_NAME = "org.motechproject.mrs";
    private static final String VERSION = "0.29";
    private static final String TEST_INTERFACE = "org.motechproject.mrs.tasks.action.MRSValidatingChannel";
    private static final String CREATE_PROGRAM_ENROLLMENT = "";
    private static final String CONFIG = "one";
    private static final String TRIGGER_SUBJECT = "mds.crud.websecurity.MotechUser.CREATE";
    private static final String PROVIDER_ID = "5";
    private static final String OBJECT_ID = "0";
    private static final String DATA_SOURCE_TYPE = "Patient-" + DEFAULT_CONFIG_NAME;

    private static final Integer MAX_RETRIES_BEFORE_FAIL = 20;
    private static final Integer WAIT_TIME = 2000;

    private static final String FORM = "";
    private static final String XMLNS1 = "";

    private static final String

    @Before
    public void setUp() throws IOException, InterruptedException {
        createAdminUser();
        login();
        openMRSTasksNotifier = (OpenMRSTasksNotifier) ServiceRetriever.getWebAppContext(bundleContext, OPENMRS_CHANNEL_NAME).getBean("openMrsTasksNotifier");
        setUpSecurityContext("motech", "motech", "manageTasks", "manageOpenMRS");
    }

    @Test
    public void testOpenMRSTasksIntegration() throws InterruptedException, IOException {
        configs = DummyConfigsData.prepareConfigs();
        HttpResponse configurationsResponse = updateConfigurations(configs);
        assertEquals(HttpStatus.SC_OK, configurationsResponse.getStatusLine().getStatusCode());

        createTestData();

        waitForChannel(OPENMRS_CHANNEL_NAME);
        Channel channel = findChannel(OPENMRS_CHANNEL_NAME);

        createDummyActionChannel(channel);
        createTestTask();

        HttpResponse response = sendMockForm();

        // Make sure that OpenMRS controller returned 200 after handling the form
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        // Give Tasks some time to process
        waitForTaskExecution();
        // Ask our OSGi service, which acts as Task action, to verify that correct values were received
        assertTrue(validatingChannel.verify());
    }

    private void createDummyActionChannel(Channel channel) {
        SortedSet<ActionParameterRequest> parameters = new TreeSet<>();

        int order = 0;

        parameters.add(prepareParameter(Keys.CONFIG_NAME, DisplayNames.CONFIG_NAME, CONFIG, false, true, order++));
        parameters.add(prepareParameter(Keys.PATIENT_UUID, DisplayNames.PATIENT_UUID, true, order++));
        parameters.add(prepareParameter(Keys.PROGRAM_UUID, DisplayNames.PROGRAM_UUID, true, order++));
        parameters.add(prepareParameter(Keys.DATE_ENROLLED, DisplayNames.DATE_ENROLLED, ParameterType.DATE, true, order++));
        parameters.add(prepareParameter(Keys.DATE_COMPLETED, DisplayNames.DATE_COMPLETED, ParameterType.DATE, false, order++));
        parameters.add(prepareParameter(Keys.LOCATION_NAME, DisplayNames.LOCATION_NAME, false, order));

        ActionEventRequest actionEventRequest = new ActionEventRequestBuilder()
                .setDisplayName(getDisplayName(CREATE_PROGRAM_ENROLLMENT, CONFIG))
                .setSubject("validate")
                .setDescription(null)
                .setServiceInterface(TEST_INTERFACE)
                .setServiceMethod("createProgramEnrollment")
                .setActionParameters(parameters)
                .createActionEventRequest();

        channel.addActionTaskEvent(ActionEventBuilder.fromActionEventRequest(actionEventRequest).build());
        getChannelService().addOrUpdate(channel);
    }

    private ActionParameterRequest prepareParameter(String key, String displayName, boolean required,
                                                    int order) {
        return prepareParameterBuilder(key, displayName, required, order).createActionParameterRequest();
    }

    private ActionParameterRequest prepareParameter(String key, String displayName, String value, boolean required,
                                                    boolean hidden, int order) {
        return prepareParameterBuilder(key, displayName, required, order)
                .setValue(value)
                .setHidden(hidden)
                .createActionParameterRequest();
    }

    private ActionParameterRequest prepareParameter(String key, String displayName, ParameterType type,
                                                    boolean required, int order) {
        return prepareParameterBuilder(key, displayName, required, order)
                .setType(type.toString())
                .createActionParameterRequest();
    }

    private ActionParameterRequestBuilder prepareParameterBuilder(String key, String displayName, boolean required,
                                                                  int order) {
        return new ActionParameterRequestBuilder()
                .setKey(key)
                .setDisplayName(displayName)
                .setRequired(required)
                .setOrder(order);
    }

    private void createTestTask() {
        TaskTriggerInformation triggerInformation = new TaskTriggerInformation("trigger", OPENMRS_CHANNEL_NAME, OPENMRS_MODULE_NAME, VERSION,
                TRIGGER_SUBJECT, TRIGGER_SUBJECT);

        TaskActionInformation actionInformation = new TaskActionInformation("Create Program Enrollment Action", OPENMRS_CHANNEL_NAME, OPENMRS_CHANNEL_NAME, VERSION,
                TEST_INTERFACE, "createProgramEnrollment");
        actionInformation.setSubject("validate");

        Map<String, String> values = new HashMap<>();
        values.put(Keys.PATIENT_UUID, createdPatient.getUuid());
        values.put(Keys.PROGRAM_UUID, "187af646-373b-4459-8114-4724d7e07fd5");
        values.put(Keys.PATIENT_UUID, "187af646-373b-4459-8114-4724d7e07fd5");
        values.put(Keys.DATE_ENROLLED, new DateTime("2010-01-16T00:00:00Z").toString());
        values.put(Keys.DATE_COMPLETED, new DateTime("2016-01-16T00:00:00Z").toString());
        values.put(Keys.LOCATION_NAME, locationService.getLocations(DEFAULT_CONFIG_NAME, DEFAULT_LOCATION_NAME).get(0).toString());
        actionInformation.setValues(values);

        Task task = new Task("OpenTestTask", triggerInformation, Arrays.asList(actionInformation));
        getTaskService().save(task);

        getTriggerHandler().registerHandlerFor(task.getTrigger().getEffectiveListenerSubject());
    }

    private HttpResponse sendMockForm() throws IOException, InterruptedException {
        HttpPost httpPost = new HttpPost(String.format("http://localhost:%d/commcare/forms/%s", PORT, configs.getByName("one").getName()));
        HttpEntity body = new ByteArrayEntity(FORM.getBytes("UTF-8"));
        httpPost.setEntity(body);
        return getHttpClient().execute(httpPost);
    }

    private void createTestData() {
        createdPatient = patientService.createPatient(DEFAULT_CONFIG_NAME, preparePatient());

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

        return new Patient(person, "602", location);
    }

    private void waitForTaskExecution() throws InterruptedException {
        getLogger().info("testCommcareTasksIntegration starts waiting for task to execute");
        int retries = 0;
        while (retries < MAX_RETRIES_BEFORE_FAIL && !validatingChannel.hasExecuted()) {
            retries++;
            Thread.sleep(WAIT_TIME);
        }
        getLogger().info("Task executed after " + retries + " retries, what took about "
                + (retries * WAIT_TIME) / 1000 + " seconds");
    }

    private String getDisplayName(String actionName, String configName) {
        return String.format("%s [%s]", actionName, configName);
    }

    private DataSource createDataSource() {
        List<Lookup> lookupList = new ArrayList<>();
        DataSource patientDataSource = new DataSource(OPENMRS_CHANNEL_NAME, PROVIDER_ID, OBJECT_ID, DATA_SOURCE_TYPE, "openMRS.lookup.motechId", lookupList, );

    }
}

