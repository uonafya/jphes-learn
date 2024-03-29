package org.hisp.dhis.interpretation;

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

import org.hisp.dhis.chart.Chart;
import org.hisp.dhis.mapping.Map;
import org.hisp.dhis.reporttable.ReportTable;
import org.hisp.dhis.system.deletion.DeletionHandler;
import org.hisp.dhis.user.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Lars Helge Overland
 */
public class InterpretationDeletionHandler
    extends DeletionHandler
{
    @Autowired
    private InterpretationService interpretationService;

    @Override
    protected String getClassName()
    {
        return Interpretation.class.getSimpleName();
    }

    @Override
    public void deleteUser( User user )
    {
        List<Interpretation> interpretations = interpretationService.getInterpretations();

        for ( Interpretation interpretation : interpretations )
        {
            if ( interpretation.getUser() != null && interpretation.getUser().equals( user ) )
            {
                interpretation.setUser( null );
                interpretationService.updateInterpretation( interpretation );
            }
        }
    }

    @Override
    public String allowDeleteMap( Map map )
    {
        return interpretationService.countMapInterpretations( map ) == 0 ? null : ERROR;
    }

    @Override
    public String allowDeleteChart( Chart chart )
    {
        return interpretationService.countChartInterpretations( chart ) == 0 ? null : ERROR;
    }

    @Override
    public String allowDeleteReportTable( ReportTable reportTable )
    {
        return interpretationService.countReportTableInterpretations( reportTable ) == 0 ? null : ERROR;
    }
}
