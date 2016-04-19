/**
 * Copyright (c) 2015-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.component.objectdetails;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.evolveum.midpoint.gui.api.component.tabs.PanelTab;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.web.component.assignment.AssignmentEditorDto;
import com.evolveum.midpoint.web.component.prism.ContainerStatus;
import com.evolveum.midpoint.web.component.prism.ObjectWrapper;
import com.evolveum.midpoint.web.page.admin.PageAdminFocus;
import com.evolveum.midpoint.web.page.admin.PageAdminObjectDetails;
import com.evolveum.midpoint.web.page.admin.roles.RoleMemberPanel;
import com.evolveum.midpoint.web.page.admin.roles.RolePolicyPanel;
import com.evolveum.midpoint.web.page.admin.users.dto.FocusSubwrapperDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RoleType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;

/**
 * @author semancik
 *
 */
public class RoleMainPanel extends AbstractRoleMainPanel<RoleType> {
	private static final long serialVersionUID = 1L;
	
	public RoleMainPanel(String id, LoadableModel<ObjectWrapper<RoleType>> objectModel, 
			LoadableModel<List<AssignmentEditorDto>> assignmentsModel, 
			LoadableModel<List<FocusSubwrapperDto<ShadowType>>> projectionModel, 
			LoadableModel<List<AssignmentEditorDto>> inducementsModel, PageAdminFocus<RoleType> parentPage) {
		super(id, objectModel, assignmentsModel, projectionModel, inducementsModel, parentPage);
	}

	@Override
	protected List<ITab> createTabs(final PageAdminObjectDetails<RoleType> parentPage) {
		List<ITab> tabs = super.createTabs(parentPage);
		
		tabs.add(new PanelTab(parentPage.createStringResource("AbstractRoleType.policyConstraints")) {
			private static final long serialVersionUID = 1L;

			@Override
			public WebMarkupContainer createPanel(String panelId) {
				return new RolePolicyPanel(panelId, getObject());
			}
		});

		tabs.add(new PanelTab(parentPage.createStringResource("pageRole.members")) {
			private static final long serialVersionUID = 1L;
			@Override
			public WebMarkupContainer createPanel(String panelId) {
				return new RoleMemberPanel<UserType>(panelId, getObject().getOid(), getDetailsPage());
			}
			
			@Override
			public boolean isVisible() {
				return getObjectWrapper().getStatus() != ContainerStatus.ADDING;
			}
		});
		
		return tabs;
	}

	
	
}
