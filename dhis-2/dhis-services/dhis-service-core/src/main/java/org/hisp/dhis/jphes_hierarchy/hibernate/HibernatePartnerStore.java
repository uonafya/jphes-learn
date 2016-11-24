package org.hisp.dhis.jphes_hierarchy.hibernate;

import org.hibernate.Query;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.jphes_hierarchy.Partner;
import org.hisp.dhis.jphes_hierarchy.PartnerStore;

import java.util.List;

/**
 * Created by bangadennis on 24/11/16.
 */
public class HibernatePartnerStore extends HibernateIdentifiableObjectStore<Partner>
    implements PartnerStore
{
    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    @Override public List<Partner> getAllPartners()
    {
        String hql = "select partner from Partner";

        Query query = getQuery( hql );
        return query.list();
    }

    @Override public Partner getPartnerByName( String name )
    {
        String hql = "select partner from Partner as partners where name=:name";

        Query query = getQuery( hql );
        return (Partner) query.uniqueResult();
    }
}
