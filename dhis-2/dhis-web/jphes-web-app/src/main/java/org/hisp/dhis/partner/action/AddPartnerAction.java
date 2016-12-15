package org.hisp.dhis.partner.action;

import com.ctc.wstx.util.StringUtil;
import com.opensymphony.xwork2.Action;
import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.jphes_hierarchy.Partner;
import org.hisp.dhis.jphes_hierarchy.PartnerService;
import org.hisp.dhis.user.UserService;

/**
 * Created by afya on 06/12/16.
 */
public class AddPartnerAction implements Action
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

    public void setPartnerService(PartnerService partnerService){ this.partnerService = partnerService; }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private String name;

    public void setName( String rolename )
    {
        this.name = rolename;
    }

    private String code;

    public void setCode(String code){ this.code = code; }

    private String displayName;

    public void setDisplayName(String displayName){ this.displayName = displayName; }


    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------


    @Override public String execute() throws Exception
    {
        Partner partner = new Partner();

        partner.setName( StringUtils.trimToNull( name ) );
        partner.setCode( StringUtils.trimToNull( code ) );
        partner.setDisplayName( StringUtils.trimToNull( displayName) );

        partnerService.savePartner( partner );

        return SUCCESS;
    }
}
