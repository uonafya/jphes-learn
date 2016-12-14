package org.hisp.dhis.partner.action;

import com.opensymphony.xwork2.Action;
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

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------


    @Override public String execute() throws Exception
    {


        return null;
    }
}
