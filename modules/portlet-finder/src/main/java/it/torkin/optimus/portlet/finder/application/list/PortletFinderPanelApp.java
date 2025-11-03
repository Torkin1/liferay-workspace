package it.torkin.optimus.portlet.finder.application.list;

import it.torkin.optimus.portlet.finder.constants.PortletFinderPanelCategoryKeys;
import it.torkin.optimus.portlet.finder.constants.PortletFinderPortletKeys;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.portal.kernel.model.Portlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author daniele
 */
@Component(
	property = {
		"panel.app.order:Integer=100",
		"panel.category.key=" + PortletFinderPanelCategoryKeys.CONTROL_PANEL_CATEGORY
	},
	service = PanelApp.class
)
public class PortletFinderPanelApp extends BasePanelApp {

	@Override
	public String getPortletId() {
		return PortletFinderPortletKeys.PORTLETFINDER;
	}

	@Override
	public Portlet getPortlet() {
		return _portlet;
	}

	@Reference(target = "(javax.portlet.name=" + PortletFinderPortletKeys.PORTLETFINDER + ")")
	private Portlet _portlet;

}