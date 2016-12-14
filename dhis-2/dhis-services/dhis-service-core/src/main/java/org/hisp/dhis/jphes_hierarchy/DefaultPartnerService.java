package org.hisp.dhis.jphes_hierarchy;

import java.util.List;

/**
 * Created by bangadennis on 24/11/16.
 */
public class DefaultPartnerService implements PartnerService
{

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PartnerStore partnerStore;

    public void setPartnerStore(PartnerStore partnerStore){ this.partnerStore=partnerStore; }


    // -------------------------------------------------------------------------
    // PartnerService implementation
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    @Override public int savePartner( Partner partner )
    {
        return partnerStore.save( partner );
    }

    @Override public Partner getPartner( String uid )
    {
        return partnerStore.getByUid( uid );
    }

    @Override public Partner getPartnerByName( String name )
    {
        return partnerStore.getPartnerByName( name );
    }

    @Override public List<Partner> getPartners( String name )
    {
        return partnerStore.getAllLikeName( name );
    }

    @Override public List<Partner> getAllPartners()
    {
        return partnerStore.getAllPartners();
    }

    @Override public List<Partner> getPartnersBetween( int first, int max )
    {
        return partnerStore.getAllOrderedName( first, max );
    }

    @Override public List<Partner> getPartnersBetweenByName( String name, int first, int max )
    {
        return partnerStore.getAllLikeName( name, first, max );
    }

    @Override public int getPartnerCount()
    {
        return partnerStore.getCount();
    }

    @Override public int getPartnerCountByName( String name )
    {
        return partnerStore.getCountLikeName( name );
    }

    @Override public void updatePartner( Partner partner )
    {
        partnerStore.update( partner );
    }

    @Override public void deletePartner( Partner partner )
    {
        partnerStore.delete( partner );
    }
}
