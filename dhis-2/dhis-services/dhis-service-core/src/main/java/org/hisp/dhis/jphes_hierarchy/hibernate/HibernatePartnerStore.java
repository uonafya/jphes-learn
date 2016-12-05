package org.hisp.dhis.jphes_hierarchy.hibernate;

import org.hibernate.Query;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.jphes_hierarchy.Partner;
import org.hisp.dhis.jphes_hierarchy.PartnerStore;

import java.util.List;

/**
 * Created by afya on 05/12/16.
 */
public class HibernatePartnerStore extends HibernateIdentifiableObjectStore<Partner> implements PartnerStore
{
    @SuppressWarnings( "unchecked" )
    @Override public List<Partner> getAllPartners()
    {
        String hql = "select partner from Partner";
        Query query = getQuery(hql);
        return query.list();
    }

    @Override public Partner getPartnerByName( String name )
    {
        String hql = "select partner from Partner where partner.name = :name";
        Query query = getQuery(hql);
        return (Partner) query.uniqueResult();
    }
}
