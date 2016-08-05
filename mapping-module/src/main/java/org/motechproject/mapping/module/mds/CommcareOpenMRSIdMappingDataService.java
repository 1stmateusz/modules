package org.motechproject.mapping.module.mds;

import org.motechproject.mapping.module.domain.CommcareOpenMRSIdMapping;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

import java.util.List;

public interface CommcareOpenMRSIdMappingDataService extends MotechDataService<CommcareOpenMRSIdMapping> {

    /**
     * Returns CommcareOpenMRSIdMapping list with the given {@code commcareCaseId}.
     *
     * @param commcareCaseId
     * @return the matching application, null if application with the given {@code applicationName} does not exist
     */
    @Lookup
    List<CommcareOpenMRSIdMapping> findByCommcareCaseId(@LookupField(name = "commcareCaseId", customOperator = "equalsIgnoreCase()") String commcareCaseId);

    /**
     * Returns CommcareOpenMRSIdMapping with the given {@code commcareAdditionalId}.
     *
     * @param commcareAdditionalId  the name of the application
     * @return the matching application, null if application with the given {@code applicationName} does not exist
     */
    @Lookup
    List<CommcareOpenMRSIdMapping> findByCommcareAdditionalId(@LookupField(name = "commcareAdditionalId", customOperator = "equalsIgnoreCase()") String commcareAdditionalId);

    /**
     * Returns application with the given {@code applicationName}.
     *s
     * @param applicationName  the name of the application
     * @return the matching application, null if application with the given {@code applicationName} does not exist
     */
    @Lookup
    List<CommcareOpenMRSIdMapping> findByOpenmrsPatientUuid(@LookupField(name = "openmrsPatientUuid", customOperator = "equalsIgnoreCase()") String openmrsPatientUuid);

    /**
     * Returns application with the given {@code applicationName}.
     *
     * @param applicationName  the name of the application
     * @return the matching application, null if application with the given {@code applicationName} does not exist
     */
    @Lookup
    List<CommcareOpenMRSIdMapping> findByOpenmrsPersonUuid(@LookupField(name = "openmrsPersonUuid", customOperator = "equalsIgnoreCase()") String openmrsPersonUuid);

    /**
     * Returns application with the given {@code applicationName}.
     *
     * @param applicationName  the name of the application
     * @return the matching application, null if application with the given {@code applicationName} does not exist
     */
    @Lookup
    List<CommcareOpenMRSIdMapping> findByOpenmrsAdditionalId(@LookupField(name = "openmrsAdditionalId", customOperator = "equalsIgnoreCase()") String openmrsAdditionalId);
}
