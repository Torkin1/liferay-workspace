package it.torkin.optimus.portlet.finder.portlet.configuration;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

@ExtendedObjectClassDefinition(
        category = PortletFinderCompanyConfiguration.ID,
        scope = ExtendedObjectClassDefinition.Scope.COMPANY // change this to change scope
)
@Meta.OCD(
    id = PortletFinderCompanyConfiguration.ID
)
public interface PortletFinderCompanyConfiguration {

    String ID = "it.torkin.optimus.portlet.finder.portlet.configuration.PortletFinderCompanyConfiguration";

    @Meta.AD(
            required = false,
            deflt = "true",
            description = "If true, PortletFinder will search for portlet occurrences in all Layouts. Set it to false to limit the scope to the current Group."
    )
    boolean ignoreScopeGroupIdFlag();
}
