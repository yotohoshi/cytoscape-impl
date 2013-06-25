package org.cytoscape.work.internal;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.util.Properties;

import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableRecorder;
import org.cytoscape.work.internal.task.JDialogTaskManager;
import org.cytoscape.work.internal.task.TaskManagerImpl;
import org.cytoscape.work.internal.task.JPanelTaskManager;
import org.cytoscape.work.internal.tunables.BooleanHandler;
import org.cytoscape.work.internal.tunables.BoundedHandler;
import org.cytoscape.work.internal.tunables.DoubleHandler;
import org.cytoscape.work.internal.tunables.FileHandlerFactory;
import org.cytoscape.work.internal.tunables.FloatHandler;
import org.cytoscape.work.internal.tunables.IntegerHandler;
import org.cytoscape.work.internal.tunables.JDialogTunableMutator;
import org.cytoscape.work.internal.tunables.JPanelTunableMutator;
import org.cytoscape.work.internal.tunables.ListMultipleHandler;
import org.cytoscape.work.internal.tunables.ListSingleHandler;
import org.cytoscape.work.internal.tunables.LongHandler;
import org.cytoscape.work.internal.tunables.StringHandler;
import org.cytoscape.work.internal.tunables.URLHandlerFactory;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		CyProperty<Properties> cyPropertyServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		DataSourceManager dsManager = getService(bc, DataSourceManager.class);

		FileUtil fileUtilRef = getService(bc,FileUtil.class);
		UndoSupportImpl undoSupport = new UndoSupportImpl();
		
		JDialogTunableMutator jDialogTunableMutator = new JDialogTunableMutator();
		JPanelTunableMutator jPanelTunableMutator = new JPanelTunableMutator();


		JDialogTaskManager jDialogTaskManager = new JDialogTaskManager(jDialogTunableMutator, cyPropertyServiceRef);
		DialogTaskManager dialogTaskManager = new TaskManagerImpl(jDialogTunableMutator, cyPropertyServiceRef);

		PanelTaskManager jPanelTaskManager = new JPanelTaskManager(jPanelTunableMutator, jDialogTaskManager);

		SupportedFileTypesManager supportedFileTypesManager = new SupportedFileTypesManager();
		SimpleGUITunableHandlerFactory<BooleanHandler> booleanHandlerFactory = new SimpleGUITunableHandlerFactory<BooleanHandler>(
				BooleanHandler.class, Boolean.class, boolean.class);
		SimpleGUITunableHandlerFactory<IntegerHandler> integerHandlerFactory = new SimpleGUITunableHandlerFactory<IntegerHandler>(
				IntegerHandler.class, Integer.class, int.class);
		SimpleGUITunableHandlerFactory<FloatHandler> floatHandlerFactory = new SimpleGUITunableHandlerFactory<FloatHandler>(
				FloatHandler.class, Float.class, float.class);
		SimpleGUITunableHandlerFactory<DoubleHandler> doubleHandlerFactory = new SimpleGUITunableHandlerFactory<DoubleHandler>(
				DoubleHandler.class, Double.class, double.class);
		SimpleGUITunableHandlerFactory<LongHandler> longHandlerFactory = new SimpleGUITunableHandlerFactory<LongHandler>(
				LongHandler.class, Long.class, long.class);
		SimpleGUITunableHandlerFactory<StringHandler> stringHandlerFactory = new SimpleGUITunableHandlerFactory<StringHandler>(
				StringHandler.class, String.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedIntegerHandlerFactory = new SimpleGUITunableHandlerFactory<BoundedHandler>(
				BoundedHandler.class, BoundedInteger.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedFloatHandlerFactory = new SimpleGUITunableHandlerFactory<BoundedHandler>(
				BoundedHandler.class, BoundedFloat.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedDoubleHandlerFactory = new SimpleGUITunableHandlerFactory<BoundedHandler>(
				BoundedHandler.class, BoundedDouble.class);
		SimpleGUITunableHandlerFactory<BoundedHandler> boundedLongHandlerFactory = new SimpleGUITunableHandlerFactory<BoundedHandler>(
				BoundedHandler.class, BoundedLong.class);
		SimpleGUITunableHandlerFactory<ListSingleHandler> listSingleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory<ListSingleHandler>(
				ListSingleHandler.class, ListSingleSelection.class);
		SimpleGUITunableHandlerFactory<ListMultipleHandler> listMultipleSelectionHandlerFactory = new SimpleGUITunableHandlerFactory<ListMultipleHandler>(
				ListMultipleHandler.class, ListMultipleSelection.class);

		URLHandlerFactory urlHandlerFactory = new URLHandlerFactory(dsManager);
		
		FileHandlerFactory fileHandlerFactory = new FileHandlerFactory(fileUtilRef,supportedFileTypesManager);

		Properties undoSupportProps = new Properties();
		registerService(bc,undoSupport,UndoSupport.class, undoSupportProps);
		registerService(bc,undoSupport,SwingUndoSupport.class, undoSupportProps);

		//registerService(bc,jDialogTaskManager,DialogTaskManager.class, new Properties());
		//registerService(bc,jDialogTaskManager,TaskManager.class, new Properties());
		registerService(bc,dialogTaskManager,DialogTaskManager.class, new Properties());
		registerService(bc,dialogTaskManager,TaskManager.class, new Properties());

		registerService(bc,jPanelTaskManager,PanelTaskManager.class, new Properties());
		

		registerService(bc,integerHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,floatHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,doubleHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,longHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,booleanHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,stringHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedIntegerHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedFloatHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedDoubleHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,boundedLongHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,listSingleSelectionHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,listMultipleSelectionHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,fileHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		registerService(bc,urlHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		
		registerServiceListener(bc,supportedFileTypesManager,"addInputStreamTaskFactory","removeInputStreamTaskFactory",InputStreamTaskFactory.class);
		registerServiceListener(bc,supportedFileTypesManager,"addCyWriterTaskFactory","removeCyWriterTaskFactory",CyWriterFactory.class);

		registerServiceListener(bc,jDialogTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);
		registerServiceListener(bc,dialogTaskManager,"addTunableRecorder","removeTunableRecorder",TunableRecorder.class);

		registerServiceListener(bc,jPanelTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);
		registerServiceListener(bc,jDialogTunableMutator,"addTunableHandlerFactory","removeTunableHandlerFactory",GUITunableHandlerFactory.class, TunableHandlerFactory.class);

	}
}
