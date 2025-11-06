package it.torkin.optimus.portlet.finder.helpers;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalServiceUtil;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructure;
import com.liferay.layout.page.template.model.LayoutPageTemplateStructureRel;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureLocalServiceUtil;
import com.liferay.layout.page.template.service.LayoutPageTemplateStructureRelLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.*;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.*;
import com.liferay.portal.kernel.util.comparator.LayoutComparator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.segments.service.SegmentsExperienceLocalServiceUtil;
import it.torkin.optimus.portlet.finder.portlet.configuration.PortletFinderCompanyConfiguration;
import it.torkin.optimus.portlet.finder.portlet.views.LayoutView;

import javax.portlet.PortletRequest;
import java.util.*;
import java.util.stream.Collectors;

public class PortletFinderUtil implements Constants {

	private static final Log logger = LogFactoryUtil.getLog(PortletFinderUtil.class);

	public static String getLayoutUrl(PortletRequest request, Layout layout, Portlet layoutPortlet, boolean privatePage) {

		return getUrl(request, layout, layoutPortlet, true, privatePage);
	}

    private static String _getVirtualHostName(Layout layout) {
        LayoutSet layoutSet = layout.getLayoutSet();

        VirtualHost virtualHost = VirtualHostLocalServiceUtil.fetchVirtualHost(
                layoutSet.getCompanyId(), layoutSet.getLayoutSetId());

        return (virtualHost != null) ? virtualHost.getHostname() : null;

    }

	private static String _getControlPanelPortletUrl(PortletRequest request, Portlet layoutPortlet){
		return PortalUtil.getControlPanelPortletURL(request, layoutPortlet.getPortletId(), PortletRequest.RENDER_PHASE)
			.toString();
	}

	public static String getUrl(PortletRequest request, Layout layout, Portlet layoutPortlet, boolean appendLayoutUrl, boolean privatePage) {

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

		// handle panel apps separately
		if (StringUtil.equals(layout.getType(), LayoutConstants.TYPE_CONTROL_PANEL)) {
			return _getControlPanelPortletUrl(request, layoutPortlet);
		}

		StringBundler url = new StringBundler();

		try {
			Locale locale = themeDisplay.getLocale();

			String virtualHostname = _getVirtualHostName(layout);

			url.append(themeDisplay.getPortalURL());

			if (Validator.isNull(virtualHostname) || !virtualHostname.equals(themeDisplay.getServerName())) {
				String pathFriendlyUrl = themeDisplay.getPathFriendlyURLPublic();
				if (privatePage) {
					pathFriendlyUrl = themeDisplay.getPathFriendlyURLPrivateGroup();
				}
				url.append(pathFriendlyUrl).append(layout.getGroup().getFriendlyURL());
			}
			else {
				if (!LocaleUtil.getDefault().equals(locale)) {
					String i18nPath = buildI18NPath(locale);

					if (Validator.isNotNull(i18nPath)) {
						url.append(i18nPath);
					}
				}
			}

			if (appendLayoutUrl) {
				url.append(layout.getFriendlyURL());
			}
		}
		catch (Exception e) {
			logger.error("SystemException: " + e.getMessage(), e.fillInStackTrace());
		}

		return url.toString();
	}

	public static String buildI18NPath(Locale locale) {

		String languageId = LocaleUtil.toLanguageId(locale);

		if (Validator.isNull(languageId)) {
			return null;
		}

		if (LanguageUtil.isDuplicateLanguageCode(locale.getLanguage())) {
			Locale priorityLocale = LanguageUtil.getLocale(locale.getLanguage());

			if (locale.equals(priorityLocale)) {
				languageId = locale.getLanguage();
			}
		}
		else {
			languageId = locale.getLanguage();
		}

		return StringPool.SLASH.concat(languageId);
	}
	
	public static String getPortletTitleBarName(ThemeDisplay themeDisplay, String portletId, String portletDisplayName, long plid) throws SystemException {
		if (logger.isDebugEnabled()) {
			logger.debug("GETTING PORTLET TITLE BAR NAME...");
			logger.debug("  portletId: " + portletId);
			logger.debug("  portletDisplayName: " + portletDisplayName);
		}
		
		List<PortletPreferences> preferencesList =
						PortletPreferencesLocalServiceUtil.getPortletPreferences(
							themeDisplay.getCompanyId(), 
							themeDisplay.getScopeGroupId(), 
							PortletKeys.PREFS_OWNER_ID_DEFAULT, 
							PortletKeys.PREFS_OWNER_TYPE_LAYOUT, 
							portletId, 
							false);

		for (PortletPreferences portletPreferences : preferencesList) {
			
			if (logger.isDebugEnabled()) { 
				logger.debug("pp:" + portletPreferences);
			}

			if (portletPreferences != null
							&& portletPreferences.getPlid() == plid) {
			
				try {
                    String xmlString = portletPreferences.toXmlString();
                    if (Validator.isNull(xmlString)) {
                        xmlString = "";
                    }
					Document document = SAXReaderUtil.read(xmlString);
					String propertyName = "portletSetupTitle_" + themeDisplay.getLocale();
					if (logger.isDebugEnabled()) logger.debug("propertyName: " + propertyName);
					Node altTitleTabNode = document.selectSingleNode("/portlet-preferences/preference[name='" + propertyName + "']/value");
					
					if (Validator.isNotNull(altTitleTabNode)) {
						portletDisplayName = altTitleTabNode.getText();
						if (logger.isDebugEnabled()) logger.debug("new name:" + portletDisplayName);
					}
				}
				catch (DocumentException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		
		return portletDisplayName;
	}
	
	public static String getIconUrl(Portlet portlet) {
		if (portlet==null || Validator.isNull(portlet.getIcon()))
			return null;
		else
			return portlet.getContextPath() + portlet.getIcon();
	}


    private static Map<String, String> _getWidgetLayoutPortletNames(Layout layout){
        LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();

        Map<String, String> portletNames = new HashMap<String, String>();
        List<Portlet> temp = layoutTypePortlet.getAllPortlets();
        for (Portlet portlet : temp) {
            if (!portletNames.containsKey(portlet.getPortletName()))
                portletNames.put(portlet.getPortletName(), portlet.getDisplayName());
        }

        return portletNames;
    }

    private static Map<String, String> _getContentLayoutPortletNames(Layout layout){
        Map<String, String> portletNames = new HashMap<String, String>();


        LayoutPageTemplateStructure layoutPageTemplateStructure = LayoutPageTemplateStructureLocalServiceUtil.fetchLayoutPageTemplateStructure(
                layout.getGroupId(), layout.getPlid());

        long layoutPageTemplateStructureId = layoutPageTemplateStructure.getLayoutPageTemplateStructureId();
        long segmentsExperienceId = SegmentsExperienceLocalServiceUtil.fetchDefaultSegmentsExperienceId(layout.getPlid());

        LayoutPageTemplateStructureRel layoutPageTemplateStructureRel = LayoutPageTemplateStructureRelLocalServiceUtil
                .fetchLayoutPageTemplateStructureRel(layoutPageTemplateStructureId, segmentsExperienceId);

        try {
            JSONObject root = JSONFactoryUtil.createJSONObject(layoutPageTemplateStructureRel.getData());
            JSONObject items = root.getJSONObject("items");
            for (String itemUUID : items.keySet()) {
                JSONObject item = items.getJSONObject(itemUUID);
                JSONObject config = item.getJSONObject("config");
                if (!config.keySet().contains("fragmentEntryLinkId"))
                    continue;
                FragmentEntryLink fragmentEntryLink = FragmentEntryLinkLocalServiceUtil
                        .getFragmentEntryLink(Long.parseLong(config.getString("fragmentEntryLinkId")));
                JSONObject editableValues = JSONFactoryUtil
                        .createJSONObject(fragmentEntryLink.getEditableValues());
                if (!editableValues.keySet().contains("portletId"))
                    continue;
                Portlet portlet = PortletLocalServiceUtil.getPortletById(editableValues.getString("portletId"));
                if (Validator.isNotNull(portlet)) {
                    portletNames.put(portlet.getPortletName(), portlet.getDisplayName());
                }

            }

            return portletNames;

        } catch (PortalException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

    }

	private static Map<String, String> _getControlPanelLayoutPortletNames(Layout layout){

		// all portlets which are also panel apps must have a control panel category
		return PortletLocalServiceUtil.getPortlets().stream()
			.filter(p -> Validator.isNotNull(p.getControlPanelEntryCategory()))
			.collect(Collectors.toMap(Portlet::getPortletName, Portlet::getDisplayName));

	}

		private static Map<String, String> _getLayoutPortletNames(Layout layout) {
        Map<String, String> portletNames;

        switch (layout.getType()) {
            case LayoutConstants.TYPE_PORTLET:
                portletNames = _getWidgetLayoutPortletNames(layout);
                break;
            case LayoutConstants.TYPE_CONTENT:
                portletNames = _getContentLayoutPortletNames(layout);
                break;
            case LayoutConstants.TYPE_CONTROL_PANEL:
				portletNames = _getControlPanelLayoutPortletNames(layout);
				break;
            default:
                logger.warn("Unsupported layout type: " + layout.getType());
                portletNames = new HashMap<>();
                break;
        }

            return portletNames;
    }

    public static SortedMap<String, String> getSortedPortletNames(PortletRequest renderRequest)
            throws PortalException, SystemException {

        Map<String, String> portletNames = new HashMap<>();

        portletNames = PortletLocalServiceUtil.getPortlets().stream()
                        .collect(Collectors.toMap(Portlet::getPortletName, Portlet::getDisplayName));


        SortedMap<String, String> sortedData = new TreeMap<String, String>(new ValueComparer(portletNames));
        sortedData.putAll(portletNames);
        renderRequest.setAttribute(ATTRIBUTE_PORTLET_NAMES, sortedData);
        return sortedData;
    }
	
	/**
	 * If selectedPortlet is null, the we will try to get the first portlet of the available pages. If no portletName available then
	 * the result will be an empty array
	 * @param request the portlet request
	 * @param selectedPrivate working with private or public pages
	 * @param selectedPortlet the portlet name of the selected portlet
	 * @return a list of Layout views that contain portlets with the given portlet name
	 */
	public static List<LayoutView> findLayoutsFromSelectedPortlet(
			PortletRequest request,
			boolean selectedPrivate,
			String selectedPortlet) {

		List<LayoutView> results = new ArrayList<LayoutView>();
		try {

            List<Layout> allLayouts = getLayouts(request, selectedPrivate);
            String portletName;

            SortedMap<String, String> sortedData = getSortedPortletNames(request);
            if (!Validator.isNotNull(selectedPortlet)  && !sortedData.isEmpty()) {
                portletName = (String)sortedData.keySet().toArray()[0];
            }
            else {
                portletName = ParamUtil.getString(request, ATTRIBUTE_SELECTED_PORTLET);
            }

            List<Layout> layouts = _getLayoutsContainingPortlet(allLayouts, portletName);
			results = wrapInView(layouts, portletName);
		}
		catch (SystemException e) {
			logger.error(e.getMessage(), e);
		}
		catch (PortalException e) {
			logger.error(e.getMessage(), e);
		}

		return results;
	}
	
	private static List<Layout> getLayouts(PortletRequest request, boolean selectedPrivate)
		throws SystemException {

		boolean ignoreScopeGroupIdFlag = getIgnoreScopeGroupIdFlag(request);

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		List<Layout> allLayouts = new LinkedList<Layout>();

		if (ignoreScopeGroupIdFlag) {
			allLayouts = LayoutLocalServiceUtil.getLayouts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		}
		else {
			allLayouts = LayoutLocalServiceUtil.getLayouts(themeDisplay.getScopeGroupId(), selectedPrivate);
		}

		List<Layout> sortedLayouts = new ArrayList<Layout>(allLayouts);
		Collections.sort(sortedLayouts, LayoutComparator.getInstance(true));
		request.setAttribute("allLayouts", sortedLayouts);
		request.setAttribute("ignoreScopeGroupIdFlag", ignoreScopeGroupIdFlag);
		return allLayouts;
	}
	
	/**
	 * Returns the preference that indicates if we want to ignore the scope group id 
	 * that is selected through the control panel in the content area
	 * @param request
	 * @return a boolean that indicates the value of the requested preference
	 */
	private static boolean getIgnoreScopeGroupIdFlag(PortletRequest request) {

        try {
            long companyId = ((ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY)).getCompanyId();
            PortletFinderCompanyConfiguration companyConfiguration = ConfigurationProviderUtil.getCompanyConfiguration(PortletFinderCompanyConfiguration.class, companyId);
            return companyConfiguration.ignoreScopeGroupIdFlag();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
	
	private static List<Layout> _getLayoutsContainingPortlet(List<Layout> layouts, String portletName) {
		List<Layout> results = new LinkedList<Layout>();
		
		if (logger.isDebugEnabled()) logger.debug("FILTERING LAYOUTS BY PORTLET NAME:" + portletName);
		
		for (Layout layout : layouts) {
			if (logger.isDebugEnabled()) logger.debug("  CHECKING LAYOUT " + layout.getFriendlyURL());

            Map<String, String> portletNames = _getLayoutPortletNames(layout);
            if (portletNames.containsKey(portletName)){
                if (logger.isDebugEnabled()) logger.debug("    ADDING PORTLET");
                results.add(layout);
            }

		}
		if (logger.isDebugEnabled()) logger.debug("RETURNING " + results.size() + " FILTERED LAYOUTS");
		return results;
	}
	
	/**
	 * Wraps a Layout object in a LayoutView. It needs the portlet name to work correctly
	 * @param allLayouts
	 * @param portletName 
	 * @return
	 */
	private static List<LayoutView> wrapInView(List<Layout> allLayouts, String portletName) {

		List<LayoutView> results = new LinkedList<LayoutView>();

        for (Layout layout : allLayouts) {
            results.add(new LayoutView(layout, portletName));
        }

		return results;
	}
}
