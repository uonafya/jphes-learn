package org.hisp.dhis.trackedentity;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.common.GridHeader;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.Pager;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.system.grid.ListGrid;
import org.hisp.dhis.system.util.DateUtils;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.validation.ValidationCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hisp.dhis.common.OrganisationUnitSelectionMode.*;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.*;

/**
 * @author Abyot Asalefew Gizaw
 */
@Transactional
public class DefaultTrackedEntityInstanceService
    implements TrackedEntityInstanceService
{
    private static final Log log = LogFactory.getLog( DefaultTrackedEntityInstanceService.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    @Autowired
    private TrackedEntityInstanceStore trackedEntityInstanceStore;

    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;

    @Autowired
    private TrackedEntityAttributeService attributeService;

    @Autowired
    private TrackedEntityService trackedEntityService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private ProgramService programService;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    @Autowired
    private CurrentUserService currentUserService;

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public List<TrackedEntityInstance> getTrackedEntityInstances( TrackedEntityInstanceQueryParams params )
    {
        decideAccess( params );
        validate( params );

        // ---------------------------------------------------------------------
        // Verify params
        // ---------------------------------------------------------------------

        User user = currentUserService.getCurrentUser();

        if ( user != null && params.isOrganisationUnitMode( OrganisationUnitSelectionMode.ACCESSIBLE ) )
        {
            params.setOrganisationUnits( user.getDataViewOrganisationUnitsWithFallback() );
            params.setOrganisationUnitMode( OrganisationUnitSelectionMode.DESCENDANTS );
        }
        else if ( params.isOrganisationUnitMode( CHILDREN ) )
        {
            Set<OrganisationUnit> organisationUnits = new HashSet<>();
            organisationUnits.addAll( params.getOrganisationUnits() );

            for ( OrganisationUnit organisationUnit : params.getOrganisationUnits() )
            {
                organisationUnits.addAll( organisationUnit.getChildren() );
            }

            params.setOrganisationUnits( organisationUnits );
        }

        if ( !params.isPaging() && !params.isSkipPaging() )
        {
            params.setDefaultPaging();
        }

        return trackedEntityInstanceStore.getTrackedEntityInstances( params );
    }

    // TODO lower index on attribute value?
    @Override
    public Grid getTrackedEntityInstancesGrid( TrackedEntityInstanceQueryParams params )
    {
        decideAccess( params );
        validate( params );

        // ---------------------------------------------------------------------
        // Verify params
        // ---------------------------------------------------------------------

        User user = currentUserService.getCurrentUser();

        if ( user != null && params.isOrganisationUnitMode( OrganisationUnitSelectionMode.ACCESSIBLE ) )
        {
            params.setOrganisationUnits( user.getDataViewOrganisationUnitsWithFallback() );
            params.setOrganisationUnitMode( OrganisationUnitSelectionMode.DESCENDANTS );
        }

        if ( !params.isPaging() && !params.isSkipPaging() )
        {
            params.setDefaultPaging();
        }

        // ---------------------------------------------------------------------
        // If params of type query and no attributes or filters defined, use
        // attributes from program if program is defined, if not, use 
        // display-in-list attributes.
        // ---------------------------------------------------------------------

        if ( params.isOrQuery() || !params.hasAttributes() )
        {
            if ( params.hasProgram() )
            {
                params.addAttributesIfNotExist( QueryItem.getQueryItems( params.getProgram().getTrackedEntityAttributes() ) );
            }
            else
            {
                Collection<TrackedEntityAttribute> attributes = attributeService.getTrackedEntityAttributesDisplayInList();

                params.addAttributesIfNotExist( QueryItem.getQueryItems( attributes ) );
                params.addFiltersIfNotExist( QueryItem.getQueryItems( attributes ) );
            }
        }

        // ---------------------------------------------------------------------
        // Conform params
        // ---------------------------------------------------------------------

        params.conform();

        // ---------------------------------------------------------------------
        // Grid headers
        // ---------------------------------------------------------------------

        Grid grid = new ListGrid();

        grid.addHeader( new GridHeader( TRACKED_ENTITY_INSTANCE_ID, "Instance" ) );
        grid.addHeader( new GridHeader( CREATED_ID, "Created" ) );
        grid.addHeader( new GridHeader( LAST_UPDATED_ID, "Last updated" ) );
        grid.addHeader( new GridHeader( ORG_UNIT_ID, "Org unit" ) );
        grid.addHeader( new GridHeader( TRACKED_ENTITY_ID, "Tracked entity" ) );
        grid.addHeader( new GridHeader( INACTIVE_ID, "Inactive" ) );

        for ( QueryItem item : params.getAttributes() )
        {
            grid.addHeader( new GridHeader( item.getItem().getUid(), item.getItem().getName() ) );
        }

        List<Map<String, String>> entities = trackedEntityInstanceStore.getTrackedEntityInstancesGrid( params );

        // ---------------------------------------------------------------------
        // Grid rows
        // ---------------------------------------------------------------------

        Set<String> tes = new HashSet<>();

        for ( Map<String, String> entity : entities )
        {
            grid.addRow();
            grid.addValue( entity.get( TRACKED_ENTITY_INSTANCE_ID ) );
            grid.addValue( entity.get( CREATED_ID ) );
            grid.addValue( entity.get( LAST_UPDATED_ID ) );
            grid.addValue( entity.get( ORG_UNIT_ID ) );
            grid.addValue( entity.get( TRACKED_ENTITY_ID ) );
            grid.addValue( entity.get( INACTIVE_ID ) );

            tes.add( entity.get( TRACKED_ENTITY_ID ) );

            for ( QueryItem item : params.getAttributes() )
            {
                grid.addValue( entity.get( item.getItemId() ) );
            }
        }

        Map<Object, Object> metaData = new HashMap<>();

        if ( params.isPaging() )
        {
            int count = 0;

            if ( params.isTotalPages() )
            {
                count = trackedEntityInstanceStore.getTrackedEntityInstanceCount( params );
            }

            Pager pager = new Pager( params.getPageWithDefault(), count, params.getPageSizeWithDefault() );
            metaData.put( PAGER_META_KEY, pager );
        }

        if ( !params.isSkipMeta() )
        {
            Map<String, String> names = new HashMap<>();

            for ( String te : tes )
            {
                TrackedEntity entity = trackedEntityService.getTrackedEntity( te );
                names.put( te, entity != null ? entity.getDisplayName() : null );
            }

            metaData.put( META_DATA_NAMES_KEY, names );
        }

        grid.setMetaData( metaData );

        return grid;
    }

    @Override
    public void decideAccess( TrackedEntityInstanceQueryParams params )
    {
        if ( params.isOrganisationUnitMode( ALL ) &&
            !currentUserService.currenUserIsAuthorized( F_TRACKED_ENTITY_INSTANCE_SEARCH_IN_ALL_ORGUNITS ) )
        {
            throw new IllegalQueryException( "Current user is not authorized to query across all organisation units" );
        }
    }

    @Override
    public void validate( TrackedEntityInstanceQueryParams params )
        throws IllegalQueryException
    {
        String violation = null;

        if ( params == null )
        {
            throw new IllegalQueryException( "Params cannot be null" );
        }

        User user = currentUserService.getCurrentUser();

        if ( !params.hasOrganisationUnits() && !( params.isOrganisationUnitMode( ALL ) || params.isOrganisationUnitMode( ACCESSIBLE ) ) )
        {
            violation = "At least one organisation unit must be specified";
        }

        if ( params.isOrganisationUnitMode( ACCESSIBLE ) && (user == null || !user.hasDataViewOrganisationUnitWithFallback()) )
        {
            violation = "Current user must be associated with at least one organisation unit when selection mode is ACCESSIBLE";
        }

        if ( params.hasProgram() && params.hasTrackedEntity() )
        {
            violation = "Program and tracked entity cannot be specified simultaneously";
        }

        if ( params.hasProgramStatus() && !params.hasProgram() )
        {
            violation = "Program must be defined when program status is defined";
        }

        if ( params.hasFollowUp() && !params.hasProgram() )
        {
            violation = "Program must be defined when follow up status is defined";
        }

        if ( params.hasProgramStartDate() && !params.hasProgram() )
        {
            violation = "Program must be defined when program start date is specified";
        }

        if ( params.hasProgramEndDate() && !params.hasProgram() )
        {
            violation = "Program must be defined when program end date is specified";
        }

        if ( params.hasEventStatus() && (!params.hasEventStartDate() || !params.hasEventEndDate()) )
        {
            violation = "Event start and end date must be specified when event status is specified";
        }

        if ( params.isOrQuery() && params.hasFilters() )
        {
            violation = "Query cannot be specified together with filters";
        }

        if ( !params.getDuplicateAttributes().isEmpty() )
        {
            violation = "Attributes cannot be specified more than once: " + params.getDuplicateAttributes();
        }

        if ( !params.getDuplicateFilters().isEmpty() )
        {
            violation = "Filters cannot be specified more than once: " + params.getDuplicateFilters();
        }

        if ( violation != null )
        {
            log.warn( "Validation failed: " + violation );

            throw new IllegalQueryException( violation );
        }
    }

    @Override
    public TrackedEntityInstanceQueryParams getFromUrl( String query, Set<String> attribute, Set<String> filter,
        Set<String> ou, OrganisationUnitSelectionMode ouMode, String program, ProgramStatus programStatus,
        Boolean followUp, Date programStartDate, Date programEndDate, String trackedEntity, EventStatus eventStatus,
        Date eventStartDate, Date eventEndDate, boolean skipMeta, Integer page, Integer pageSize, boolean totalPages, boolean skipPaging )
    {
        TrackedEntityInstanceQueryParams params = new TrackedEntityInstanceQueryParams();

        QueryFilter queryFilter = getQueryFilter( query );

        if ( attribute != null )
        {
            for ( String attr : attribute )
            {
                QueryItem it = getQueryItem( attr );

                params.getAttributes().add( it );
            }
        }

        if ( filter != null )
        {
            for ( String filt : filter )
            {
                QueryItem it = getQueryItem( filt );

                params.getFilters().add( it );
            }
        }

        if ( ou != null )
        {
            for ( String orgUnit : ou )
            {
                OrganisationUnit organisationUnit = organisationUnitService.getOrganisationUnit( orgUnit );

                if ( organisationUnit == null )
                {
                    throw new IllegalQueryException( "Organisation unit does not exist: " + orgUnit );
                }

                params.getOrganisationUnits().add( organisationUnit );
            }
        }

        Program pr = program != null ? programService.getProgram( program ) : null;

        if ( program != null && pr == null )
        {
            throw new IllegalQueryException( "Program does not exist: " + program );
        }

        TrackedEntity te = trackedEntity != null ? trackedEntityService.getTrackedEntity( trackedEntity ) : null;

        if ( trackedEntity != null && te == null )
        {
            throw new IllegalQueryException( "Tracked entity does not exist: " + program );
        }

        params.setQuery( queryFilter );
        params.setProgram( pr );
        params.setProgramStatus( programStatus );
        params.setFollowUp( followUp );
        params.setProgramStartDate( programStartDate );
        params.setProgramEndDate( programEndDate );
        params.setTrackedEntity( te );
        params.setOrganisationUnitMode( ouMode );
        params.setEventStatus( eventStatus );
        params.setEventStartDate( eventStartDate );
        params.setEventEndDate( eventEndDate );
        params.setSkipMeta( skipMeta );
        params.setPage( page );
        params.setPageSize( pageSize );
        params.setTotalPages( totalPages );
        params.setSkipPaging( skipPaging );

        return params;
    }

    /**
     * Creates a QueryItem from the given item string. Item is on format
     * {attribute-id}:{operator}:{filter-value}[:{operator}:{filter-value}].
     * Only the attribute-id is mandatory.
     */
    private QueryItem getQueryItem( String item )
    {
        String[] split = item.split( DimensionalObject.DIMENSION_NAME_SEP );

        if ( split == null || (split.length % 2 != 1) )
        {
            throw new IllegalQueryException( "Query item or filter is invalid: " + item );
        }

        QueryItem queryItem = getItem( split[0] );

        if ( split.length > 1 ) // Filters specified
        {
            for ( int i = 1; i < split.length; i += 2 )
            {
                QueryOperator operator = QueryOperator.fromString( split[i] );
                queryItem.getFilters().add( new QueryFilter( operator, split[i + 1] ) );
            }
        }

        return queryItem;
    }

    private QueryItem getItem( String item )
    {
        TrackedEntityAttribute at = attributeService.getTrackedEntityAttribute( item );

        if ( at == null )
        {
            throw new IllegalQueryException( "Attribute does not exist: " + item );
        }

        return new QueryItem( at, null, at.getValueType(), at.getAggregationType(), at.getOptionSet() );
    }

    /**
     * Creates a QueryFilter from the given query string. Query is on format
     * {operator}:{filter-value}. Only the filter-value is mandatory. The EQ
     * QueryOperator is used as operator if not specified.
     */
    private QueryFilter getQueryFilter( String query )
    {
        if ( query == null || query.isEmpty() )
        {
            return null;
        }

        if ( !query.contains( DimensionalObject.DIMENSION_NAME_SEP ) )
        {
            return new QueryFilter( QueryOperator.EQ, query );
        }
        else
        {
            String[] split = query.split( DimensionalObject.DIMENSION_NAME_SEP );

            if ( split == null || split.length != 2 )
            {
                throw new IllegalQueryException( "Query has invalid format: " + query );
            }

            QueryOperator op = QueryOperator.fromString( split[0] );

            return new QueryFilter( op, split[1] );
        }
    }

    @Override
    public int addTrackedEntityInstance( TrackedEntityInstance instance )
    {
        return trackedEntityInstanceStore.save( instance );
    }

    @Override
    public int createTrackedEntityInstance( TrackedEntityInstance instance, String representativeId,
        Integer relationshipTypeId, Set<TrackedEntityAttributeValue> attributeValues )
    {
        int id = addTrackedEntityInstance( instance );

        for ( TrackedEntityAttributeValue pav : attributeValues )
        {
            attributeValueService.addTrackedEntityAttributeValue( pav );
            instance.getAttributeValues().add( pav );
        }

        // ---------------------------------------------------------------------
        // If under age, save representative information
        // ---------------------------------------------------------------------

        if ( representativeId != null )
        {
            TrackedEntityInstance representative = trackedEntityInstanceStore.getByUid( representativeId );

            if ( representative != null )
            {
                instance.setRepresentative( representative );

                Relationship rel = new Relationship();
                rel.setEntityInstanceA( representative );
                rel.setEntityInstanceB( instance );

                if ( relationshipTypeId != null )
                {
                    RelationshipType relType = relationshipTypeService.getRelationshipType( relationshipTypeId );

                    if ( relType != null )
                    {
                        rel.setRelationshipType( relType );
                        relationshipService.addRelationship( rel );
                    }
                }
            }
        }

        updateTrackedEntityInstance( instance ); // Update associations

        return id;
    }

    @Override
    public void updateTrackedEntityInstance( TrackedEntityInstance instance )
    {
        trackedEntityInstanceStore.update( instance );
    }

    @Override
    public void deleteTrackedEntityInstance( TrackedEntityInstance instance )
    {
        trackedEntityInstanceStore.delete( instance );
    }

    @Override
    public TrackedEntityInstance getTrackedEntityInstance( int id )
    {
        return trackedEntityInstanceStore.get( id );
    }

    @Override
    public TrackedEntityInstance getTrackedEntityInstance( String uid )
    {
        return trackedEntityInstanceStore.getByUid( uid );
    }

    @Override
    public boolean trackedEntityInstanceExists( String uid )
    {
        return trackedEntityInstanceStore.exists( uid );
    }

    @Override
    public void updateTrackedEntityInstance( TrackedEntityInstance instance, String representativeId,
        Integer relationshipTypeId, List<TrackedEntityAttributeValue> valuesForSave,
        List<TrackedEntityAttributeValue> valuesForUpdate, Collection<TrackedEntityAttributeValue> valuesForDelete )
    {
        trackedEntityInstanceStore.update( instance );

        valuesForSave.forEach( attributeValueService::addTrackedEntityAttributeValue );
        valuesForUpdate.forEach( attributeValueService::updateTrackedEntityAttributeValue );
        valuesForDelete.forEach( attributeValueService::deleteTrackedEntityAttributeValue );

        if ( shouldSaveRepresentativeInformation( instance, representativeId ) )
        {
            TrackedEntityInstance representative = trackedEntityInstanceStore.getByUid( representativeId );

            if ( representative != null )
            {
                instance.setRepresentative( representative );

                Relationship rel = new Relationship();
                rel.setEntityInstanceA( representative );
                rel.setEntityInstanceB( instance );

                if ( relationshipTypeId != null )
                {
                    RelationshipType relType = relationshipTypeService.getRelationshipType( relationshipTypeId );
                    if ( relType != null )
                    {
                        rel.setRelationshipType( relType );
                        relationshipService.addRelationship( rel );
                    }
                }
            }
        }
    }

    private boolean shouldSaveRepresentativeInformation( TrackedEntityInstance instance, String representativeId )
    {
        if ( representativeId == null || representativeId.isEmpty() )
        {
            return false;
        }

        return instance.getRepresentative() == null || !(instance.getRepresentative().getUid().equals( representativeId ));
    }

    @Override
    public String validateTrackedEntityInstance( TrackedEntityInstance instance, Program program )
    {
        if ( program != null )
        {
            ValidationCriteria validationCriteria = validateEnrollment( instance, program );

            if ( validationCriteria != null )
            {
                return TrackedEntityInstanceService.ERROR_ENROLLMENT + TrackedEntityInstanceService.SEPARATOR
                    + validationCriteria.getId();
            }
        }

        if ( instance.getAttributeValues() != null && instance.getAttributeValues().size() > 0 )
        {
            for ( TrackedEntityAttributeValue attributeValue : instance.getAttributeValues() )
            {
                String valid = trackedEntityInstanceStore.validate( instance, attributeValue, program );

                if ( valid != null )
                {
                    return valid;
                }
            }
        }

        return TrackedEntityInstanceService.ERROR_NONE + "";
    }

    @Override
    public ValidationCriteria validateEnrollment( TrackedEntityInstance instance, Program program )
    {
        for ( ValidationCriteria criteria : program.getValidationCriteria() )
        {
            for ( TrackedEntityAttributeValue attributeValue : instance.getAttributeValues() )
            {
                if ( attributeValue.getAttribute().getUid().equals( criteria.getProperty() ) )
                {
                    String value = attributeValue.getValue();
                    ValueType valueType = attributeValue.getAttribute().getValueType();

                    if ( valueType.isNumeric() )
                    {
                        int value1 = Integer.parseInt( value );
                        int value2 = Integer.parseInt( criteria.getValue() );

                        if ( (criteria.getOperator() == ValidationCriteria.OPERATOR_LESS_THAN && value1 >= value2)
                            || (criteria.getOperator() == ValidationCriteria.OPERATOR_EQUAL_TO && value1 != value2)
                            || (criteria.getOperator() == ValidationCriteria.OPERATOR_GREATER_THAN && value1 <= value2) )
                        {
                            return criteria;
                        }
                    }
                    else if ( valueType.isDate() )
                    {
                        Date value1 = DateUtils.parseDate( value );
                        Date value2 = DateUtils.parseDate( criteria.getValue() );

                        int i = value1.compareTo( value2 );

                        if ( i != criteria.getOperator() )
                        {
                            return criteria;
                        }
                    }
                    else
                    {
                        if ( criteria.getOperator() == ValidationCriteria.OPERATOR_EQUAL_TO && !value.equals( criteria.getValue() ) )
                        {
                            return criteria;
                        }

                    }
                }
            }

        }

        // Return null if all criteria are met

        return null;
    }
}
