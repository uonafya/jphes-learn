package org.hisp.dhis.partner.action;

import org.hisp.dhis.jphes_hierarchy.Partner;
import org.hisp.dhis.jphes_hierarchy.PartnerService;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.user.UserService;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by afya on 06/12/16.
 */

public class GetPartnerListAction extends ActionPagingSupport<Partner>
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    private PartnerService partnerService;

    public void setPartnerService(PartnerService partnerService){this.partnerService = partnerService;}

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<Partner> partners;

    public List<Partner> getPartners(){return partners;}

    private String key;

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    @Override
    public String execute()
        throws Exception
    {
        // Filter on key only if set
        if ( isNotBlank( key ) )
        {
            this.paging = createPaging( partnerService.getPartnerCountByName(key));

            partners = partnerService.getPartnersBetweenByName( key, paging.getStartPos(), paging.getPageSize() );
        }
        else
        {
            this.paging = createPaging( partnerService.getPartnerCount() );

            partners = partnerService.getPartnersBetween( paging.getStartPos(), paging.getPageSize() );
        }

        return SUCCESS;
    }
}