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
            deflt = "true"
    )
    boolean ignoreScopeGroupIdFlag();

    @Meta.AD(
            required = false,
            deflt = "600"
    )
    int popupWidth();

    @Meta.AD(
            required = false,
            deflt = "400"
    )
    int popupHeight();

    @Meta.AD(
            required = false,
            deflt = "false"
    )
    boolean enableFilters();

    @Meta.AD(
            required = false,
            deflt = "false"
    )
    boolean showBaseUrl();




}
