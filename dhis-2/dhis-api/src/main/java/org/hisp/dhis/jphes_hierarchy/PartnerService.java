package org.hisp.dhis.jphes_hierarchy;

import java.util.List;

/**
 * Created by @author bangadennis on 24/11/16.
 */


public interface PartnerService
{
    String ID = PartnerService.class.getName();

    int savePartner(Partner partner);

    Partner getPartner(String uid);

    Partner getPartnerByName(String name);

    List<Partner> getPartners(String name);

    List<Partner> getAllPartners();

    void updatePartner(Partner partner);

    void deletePartner(Partner partner);

}
