<%@ page import="it.torkin.optimus.portlet.finder.helpers.Constants" %>
<%@ page import="it.torkin.optimus.portlet.finder.portlet.views.PortletView" %>
<%@ page import="com.liferay.portal.kernel.model.Portlet" %>
<%@ page import="it.torkin.optimus.portlet.finder.helpers.PortletFinderUtil" %>
<%@ page import="javax.portlet.*" %>
<%@ page import="com.liferay.portal.kernel.service.PortletLocalServiceUtil" %>
<%@ page import="it.torkin.optimus.portlet.finder.portlet.configuration.PortletFinderCompanyConfiguration" %>
<%@ page import="com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil" %>
<%@ include file="./init.jsp" %>

<%
PortletPreferences prefs = renderRequest.getPreferences();
PortletFinderCompanyConfiguration companyConfiguration = ConfigurationProviderUtil.getCompanyConfiguration(
        PortletFinderCompanyConfiguration.class, company.getCompanyId());
boolean selectedPrivate = Boolean.parseBoolean( renderRequest.getParameter("selectedPrivate"));

%>
<liferay-portlet:actionURL name="showPortletLayouts" var="showPortletLayoutsURL"/>

<div id="<portlet:namespace/>portletsFormContainer">
	<aui:form method="post" action="<%=showPortletLayoutsURL.toString()%>" name="portletsForm">
		<aui:fieldset>
			<label><liferay-ui:message key="portlets.description"/></label>
		</aui:fieldset>
		
		<aui:fieldset column="true" style="padding-right:30px;">
			<aui:select name="selectedPrivate" label="show-private-pages" id="pageScope" onChange="submitPortletsForm();">
				<aui:option name="Public" value="false" label="Public" selected="${selectedPrivate == false}"/>
				<aui:option name="Private" value="true" label="Private" selected="${selectedPrivate == true}"/>
			</aui:select>
		</aui:fieldset>
		
		<aui:fieldset column="true" >
			<aui:select name="selectedPortlet" label="site-portlets-portlet" onChange="submitPortletsForm();" id="selectedPortlet">
				<c:forEach items="${portletNames}" var="portletName">
					<aui:option selected="${selectedPortlet == portletName.key}" value="${portletName.key}" label="${portletName.value}"/>
				</c:forEach>
			</aui:select>
		</aui:fieldset>
		
		<aui:fieldset column="true">
			<div id="<portlet:namespace/>preloader" style="width:16px;float:left;display:none;"><img src="<%=request.getContextPath()%>/images/preloader.gif"/></div>
		</aui:fieldset>
		
		<div style="clear:both;"></div>
	</aui:form>
</div>

<aui:script>

function submitPortletsForm() {
	enableWait();
	<portlet:namespace/>portletsForm.submit();
}

function enableWait() {
	AUI().use('node', function (A) {
		A.one("#<portlet:namespace/>preloader").attr('display', 'block');
		A.one("#<portlet:namespace/>pageScope").attr('readonly', 'readonly');
		A.one("#<portlet:namespace/>selectedPortlet").attr('readonly', 'readonly');
	});
}

function disableWait() {
	AUI().use('node', function (A) {
		A.one("#<portlet:namespace/>preloader").attr('display', 'none');
		A.one("#<portlet:namespace/>pageScope").attr('readonly', '');
		A.one("#<portlet:namespace/>selectedPortlet").attr('readonly', '');
	});
}


AUI().ready('aui-io-request', function(A) {
	
	function <portlet:namespace/>populatePortlets() {
		enableWait();
		var action = '<portlet:namespace/>json<%=Constants.ATTRIBUTE_ACTION %>';
		var actionValue = '<%= Constants.ACTION_PAGE_SCOPE_PORTLETS %>';
		
		var selectedPortlet = '<portlet:namespace/>json<%= Constants.ATTRIBUTE_SELECTED_PORTLET %>';
		var selectedPortletValue = A.one("#<portlet:namespace/>selectedPortlet").val();
		
		var selectedPrivate = '<portlet:namespace/>json<%= Constants.ATTRIBUTE_SELECTED_PAGES_SCOPE %>';
		var selectedPrivateValue = A.one("#<portlet:namespace/>pageScope").val();
		
		var preSelectedPortlet = '<c:out value="${selectedPortlet}" />';
		
		var url = "<portlet:resourceURL />&" + action + "=" + actionValue +"&" + selectedPrivate + "=" + selectedPrivateValue + "&" + selectedPortlet + "=" + selectedPortletValue;
		console.log("URL: " + url);
		console.log("action:" + action);
		console.log("actionValue:" + actionValue);
		console.log("selectedPortlet:" + selectedPortlet);
		console.log("selectedPortletValue:" + selectedPortletValue);
		console.log("selectedPrivate:" + selectedPrivate);
		console.log("selectedPrivateValue:" + selectedPrivateValue);
		console.log("preSelectedPortlet:" + preSelectedPortlet);
		
		A.io.request(url, {
			method: 'GET',
			dataType: 'json',
			on: {
				failure: function() {},
				success: function(event, id, obj) {
					var portletNamesArray = this.get('responseData');
					A.one("#<portlet:namespace/>selectedPortlet").html("");
					//A.one("#<portlet:namespace/>selectedPortlet").append("<option value='0'>None</option>");
					for (var i = 0; i < portletNamesArray.length; i++) {
						var portletName = portletNamesArray[i];
						A.one("#<portlet:namespace/>selectedPortlet").append("<option " + (preSelectedPortlet == portletName.portletName?"selected='true'":"") + " value='" + portletName.portletName + "'>" + portletName.portletDisplayName + "</option>");
						
					}
					disableWait();
					submitPortletsForm();
				}
			}
		});
	}

	A.one("#<portlet:namespace/>pageScope").on('change', function() {
		<portlet:namespace/>populatePortlets();
	});
});

</aui:script>

	<div id="<portlet:namespace/>results">
		<liferay-ui:search-container  emptyResultsMessage="site-portlets-no-portlets-were-found" total="${portletLayoutsSize}">
			<liferay-ui:search-container-results results="${portletLayouts}"/>
			<liferay-ui:search-container-row
					className="it.torkin.optimus.portlet.finder.portlet.views.LayoutView"
					keyProperty="layoutId"
					modelVar="layoutView">
			
				<portlet:actionURL name="showLayoutPortlets" var="showLayoutPortletsFromTableURL">
					<portlet:param name="selectedLayout" value="<%=String.valueOf(layoutView.getLayout().getPlid())%>"/>
					<portlet:param name="selectedPrivate" value="${selectedPrivate}"/>
				</portlet:actionURL>
				
				<liferay-ui:search-container-column-text
					name="site-portlets-layout-id"
						value="<%=String.valueOf(layoutView.getLayout().getLayoutId())%>"
				/>
				
				<liferay-ui:search-container-column-text
					name="Group Key"
						value="<%=layoutView.getLayout().getGroup().getGroupKey()%>"
				/>

                <liferay-ui:search-container-column-text
                        name="Type"
                        value="<%=layoutView.getLayout().getType()%>"
                />
			
				<liferay-ui:search-container-column-text
						name="site-portlets-layout-friendlyUrl"
						value='<%=layoutView.getLayout().getFriendlyURL()%>'
				/>

                <%
                    Portlet layoutPortlet = PortletLocalServiceUtil.getPortletById(layoutView.getPortletName());
                    PortletView portletView = new PortletView(layoutPortlet, layoutView.getLayout());
                %>
				
				<liferay-ui:search-container-column-text name="actions">		
					<liferay-ui:icon-menu >
						<liferay-ui:icon image="view_templates" target="_blank" message="goToLayout" url="<%=PortletFinderUtil.getLayoutUrl(renderRequest, layoutView.getLayout(), layoutPortlet, selectedPrivate)%>" />
					</liferay-ui:icon-menu>

				</liferay-ui:search-container-column-text>
			
			</liferay-ui:search-container-row>
			
			<liferay-ui:search-iterator  paginate="<%=true%>" />
			
		</liferay-ui:search-container>
	</div>
