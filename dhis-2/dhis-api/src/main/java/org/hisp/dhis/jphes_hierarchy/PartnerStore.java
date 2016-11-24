package org.hisp.dhis.jphes_hierarchy;

import org.hisp.dhis.common.GenericIdentifiableObjectStore;

import java.util.List;

/**
 * Created by @author bangadennis on 24/11/16.
 */
public interface PartnerStore extends GenericIdentifiableObjectStore<Partner>
{
    List<Partner> getAllPartners();

    Partner getPartnerByName(String name);


}
