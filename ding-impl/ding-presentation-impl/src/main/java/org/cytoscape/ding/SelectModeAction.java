package org.cytoscape.ding;

import static org.cytoscape.ding.DVisualLexicon.NETWORK_ANNOTATION_SELECTION;
import static org.cytoscape.ding.DVisualLexicon.NETWORK_EDGE_SELECTION;
import static org.cytoscape.ding.DVisualLexicon.NETWORK_NODE_SELECTION;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

@SuppressWarnings("serial")
public class SelectModeAction extends AbstractCyAction {

	public static final String NODES = "Nodes Only";
	public static final String EDGES = "Edges Only";
	public static final String ANNOTATIONS = "Annotations Only";
	public static final String NODES_EDGES = "Nodes and Edges";
	public static final String ALL = "All";
	
	private final CyServiceRegistrar serviceRegistrar;

	public SelectModeAction(final String name, float gravity, final CyServiceRegistrar serviceRegistrar) {
		super(name);
		this.serviceRegistrar = serviceRegistrar;
		
		useCheckBoxMenuItem = true;
		setPreferredMenu("Select.Mouse Drag Selects");
		setMenuGravity(gravity);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView view = applicationManager.getCurrentNetworkView();

		if (view != null) {
			if (name.equalsIgnoreCase(NODES)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
				view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
			} else if (name.equalsIgnoreCase(EDGES)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
			} else if (name.equalsIgnoreCase(ANNOTATIONS)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.FALSE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.FALSE);
				view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.TRUE);
			} else if (name.equalsIgnoreCase(NODES_EDGES)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.FALSE);
			} else if (name.equalsIgnoreCase(ALL)) {
				view.setLockedValue(NETWORK_NODE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_EDGE_SELECTION, Boolean.TRUE);
				view.setLockedValue(NETWORK_ANNOTATION_SELECTION, Boolean.TRUE);
			}
		}
	}
	
	@Override
	public boolean isEnabled() {
		CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		
		if (view == null)
			return false;
		
		NetworkViewRenderer renderer = applicationManager.getNetworkViewRenderer(view.getRendererId());
		RenderingEngineFactory<CyNetwork> factory = renderer == null ? null
				: renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);
		VisualLexicon lexicon = factory == null ? null : factory.getVisualLexicon();
		
		if (lexicon == null)
			return false; // Should never happen!
		
		// At least the properties for node and edge selection must be supported
		VisualProperty<?> vp1 = lexicon.lookup(NETWORK_NODE_SELECTION.getTargetDataType(),
				NETWORK_NODE_SELECTION.getIdString());
		VisualProperty<?> vp2 = lexicon.lookup(NETWORK_EDGE_SELECTION.getTargetDataType(),
				NETWORK_EDGE_SELECTION.getIdString());
		
		return vp1 != null && lexicon.isSupported(vp1) && vp2 != null && lexicon.isSupported(vp2);
	}
	
	@Override
	public void menuSelected(MenuEvent e) {
		final JCheckBoxMenuItem item = getThisItem(); 

		if (item != null) {
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final CyNetworkView view = applicationManager.getCurrentNetworkView();
			
			if (view == null) {
				item.setSelected(false);
			} else {
				Boolean nodeSelection = view.getVisualProperty(NETWORK_NODE_SELECTION);
				Boolean edgeSelection = view.getVisualProperty(NETWORK_EDGE_SELECTION);
				Boolean annotationSelection = view.getVisualProperty(NETWORK_ANNOTATION_SELECTION);
				
				if (nodeSelection && edgeSelection && annotationSelection)
					item.setSelected(name.equalsIgnoreCase(ALL));
				else if (nodeSelection && edgeSelection)
					item.setSelected(name.equalsIgnoreCase(NODES_EDGES));
				else if (nodeSelection)
					item.setSelected(name.equalsIgnoreCase(NODES));
				else if (edgeSelection)
					item.setSelected(name.equalsIgnoreCase(EDGES));
				else if (annotationSelection)
					item.setSelected(name.equalsIgnoreCase(ANNOTATIONS));
				else
					item.setSelected(false);
			}
		}
		
		updateEnableState();
	}
	
	private JCheckBoxMenuItem getThisItem() {
		final CySwingApplication swingApplication = serviceRegistrar.getService(CySwingApplication.class);
		final JMenu menu = swingApplication.getJMenu(preferredMenu);
		
		for (int i = 0; i < menu.getItemCount(); i++) {
			final JMenuItem item = menu.getItem(i);
			
			if (item instanceof JCheckBoxMenuItem && item.getText().equals(getName()))
				return (JCheckBoxMenuItem) item;
		}
		
		return null;
	}
}
