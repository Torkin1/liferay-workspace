
package it.torkin.optimus.portlet.finder.portlet;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import it.torkin.optimus.portlet.finder.constants.PortletFinderPortletKeys;
import it.torkin.optimus.portlet.finder.helpers.WhereIsMyPortletUtil;
import it.torkin.optimus.portlet.finder.portlet.views.LayoutView;
import org.osgi.service.component.annotations.Component;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.SortedMap;

import static it.torkin.optimus.portlet.finder.helpers.Constants.*;

/**
 * @author daniele
 */
@Component(
        property = {
                "com.liferay.portlet.add-default-resource=true",
                "com.liferay.portlet.display-category=category.hidden",
                "com.liferay.portlet.header-portlet-css=/css/main.css",
                "com.liferay.portlet.layout-cacheable=true",
                "com.liferay.portlet.private-request-attributes=false",
                "com.liferay.portlet.private-session-attributes=false",
                "com.liferay.portlet.render-weight=50",
                "com.liferay.portlet.use-default-template=true",
                "javax.portlet.display-name=PortletFinder",
                "javax.portlet.expiration-cache=0",
                "javax.portlet.init-param.template-path=/",
                "javax.portlet.init-param.view-template=/view.jsp",
                "javax.portlet.name=" + PortletFinderPortletKeys.PORTLETFINDER,
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.security-role-ref=power-user,user",

        },
        service = javax.portlet.Portlet.class
)
public final class PortletFinder extends MVCPortlet {

	public Log logger = LogFactoryUtil.getLog(PortletFinder.class);

	public void serveResource(ResourceRequest request, ResourceResponse resourceResponse)
		throws IOException, PortletException {
	
		boolean selectedPrivate = ParamUtil.getBoolean(request, "json" + ATTRIBUTE_SELECTED_PAGES_SCOPE, false);
		String tab = ParamUtil.getString(request, "json" + ATTRIBUTE_TAB, TAB_PORTLETS);
		int action = ParamUtil.getInteger(request, "json" + ATTRIBUTE_ACTION);
		boolean pageScope = ParamUtil.getBoolean(request, "json" + ATTRIBUTE_SELECTED_PAGES_SCOPE);
		if (logger.isDebugEnabled()) {
			logger.debug("JSON REQUEST:");
			logger.debug("  selectedPrivate:" + selectedPrivate);
			logger.debug("  tab:" + tab);
			logger.debug("  action:" + action);
			logger.debug("  pageScope:" + pageScope);
		}
		
		JSONArray results = JSONFactoryUtil.getJSONFactory().createJSONArray();

		try {
			switch(action) {
			case ACTION_PAGE_SCOPE_PORTLETS:
				
				SortedMap<String, String> portletNames = WhereIsMyPortletUtil.getSortedPortletNames(request);
				
				for (String portletName : portletNames.keySet()) {
					JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
					String portletDisplayName = portletNames.get(portletName);
					
					jsonObject.put(JSON_PORTLET_NAME, portletName);
					jsonObject.put(JSON_PORTLET_DISPLAY_NAME, portletDisplayName);
					
					results.put(jsonObject);
				}
				
				
				break;
			case ACTION_PAGE_SCOPE_LAYOUTS:
			default:
				break;
			}
		}
		catch (PortalException e) {
			logger.error(e.getMessage(), e);
		}
		catch (SystemException e) {
			logger.error(e.getMessage(), e);
		}
		
		resourceResponse.setContentType(ContentTypes.APPLICATION_JSON);
		resourceResponse.setCharacterEncoding(StringPool.UTF8);
		PrintWriter pw = null;
		try {
			pw = resourceResponse.getWriter();
			pw.write(results.toString());
		}
		catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
		}
		finally {
			if (Validator.isNotNull(pw)) {
				pw.flush();
				pw.close();
			}
		}
		
	}
	
	public void doView(RenderRequest request, RenderResponse renderResponse) throws IOException, PortletException {
		boolean selectedPrivate = ParamUtil.getBoolean(request, ATTRIBUTE_SELECTED_PAGES_SCOPE, false);
		String tab = ParamUtil.getString(request, ATTRIBUTE_TAB, TAB_PORTLETS);
		boolean searchDone = ParamUtil.getBoolean(request, ATTRIBUTE_SEARCH_DONE, false);
		
		if (logger.isDebugEnabled()) {
			logger.debug("RENDER PHASE: ");
			logger.debug("  selectedPrivate: " + selectedPrivate);
			logger.debug("  tab: " + tab);
			logger.debug("  searchDone: " + searchDone);
		}
		
		if (!searchDone && tab.equals(TAB_PORTLETS)) {
			List<LayoutView> layouts = WhereIsMyPortletUtil.findLayoutsFromSelectedPortlet(request, selectedPrivate, null);
			request.setAttribute(ATTRIBUTE_PORTLET_LAYOUTS, layouts);
			request.setAttribute(ATTRIBUTE_PORTLET_LAYOUTS_SIZE, layouts.size());
			
			if (logger.isDebugEnabled()) {
				logger.debug("RETURNING FROM DO VIEW:");
				logger.debug("  layouts:" + layouts.size());
			}
		}
		
		super.doView(request, renderResponse);
	}

	public void showPortletLayouts(ActionRequest actionRequest, ActionResponse actionResponse) throws IOException, PortletException {
		String selectedPortlet = ParamUtil.getString(actionRequest, "selectedPortlet");
		boolean selectedPrivate = ParamUtil.getBoolean(actionRequest, "selectedPrivate");

		if (logger.isDebugEnabled()) {
			logger.debug("selectedPortlet: " + selectedPortlet);
			logger.debug("selectedPrivate: " + selectedPrivate);
		}
		
		List<LayoutView> layouts = WhereIsMyPortletUtil.findLayoutsFromSelectedPortlet(actionRequest, selectedPrivate, selectedPortlet);
		
		actionRequest.setAttribute("selectedPrivate", selectedPrivate);
		actionRequest.setAttribute("selectedPortlet", selectedPortlet);
		actionRequest.setAttribute("portletLayouts", layouts);
		actionRequest.setAttribute("portletLayoutsSize", layouts.size());
		actionRequest.setAttribute("searchDone", true);
		
		actionResponse.setRenderParameter("selectedPrivate", String.valueOf(selectedPrivate));
		actionResponse.setRenderParameter("searchDone", String.valueOf(true));

		if (logger.isDebugEnabled()) {
			logger.debug("RETURNING:");
			logger.debug("  selectedPortletName:" + selectedPortlet);
			logger.debug("  portletLayouts:" + layouts);
			logger.debug("  portletLayoutsSize:" + layouts.size());
		}
	}

}
