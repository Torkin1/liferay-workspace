package it.torkin.optimus.portlet.finder.application.list;

import java.util.Locale;

import org.osgi.service.component.annotations.Component;

import com.liferay.application.list.BasePanelCategory;
import com.liferay.application.list.PanelCategory;
import com.liferay.application.list.constants.PanelCategoryKeys;

import it.torkin.optimus.portlet.finder.constants.PortletFinderPanelCategoryKeys;

/**
 * @author daniele
 */
@Component(
	property = {
		"panel.category.key=" + PanelCategoryKeys.SITE_ADMINISTRATION,
		"panel.category.order:Integer=100"
	},
	service = PanelCategory.class
)
public class PortletFinderPanelCategory extends BasePanelCategory {

	@Override
	public String getKey() {
		return PortletFinderPanelCategoryKeys.CONTROL_PANEL_CATEGORY;
	}

	@Override
	public String getLabel(Locale locale) {
		return PortletFinderPanelCategoryKeys.CONTROL_PANEL_CATEGORY;
	}

}