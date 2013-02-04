/**
 * Copyright (C) 2012 Schneider Electric
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Julien TOUS
 * Contributors:
 */

package org.ow2.mind.adl.annotations;

import java.io.*;
import java.util.Map;
import java.util.TreeSet;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.PathHelper;
import org.ow2.mind.adl.annotation.ADLLoaderPhase;
import org.ow2.mind.adl.annotation.AbstractADLLoaderAnnotationProcessor;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Binding;
import org.ow2.mind.adl.ast.BindingContainer;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.MindInterface;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.annotation.Annotation;
import org.ow2.mind.io.BasicOutputFileLocator;
import org.ow2.mind.adl.annotations.DotWriter;
import org.ow2.mind.compilation.CompilerContextHelper;

/**
 * @author Julien TOUS
 */
public class DumpDotAnnotationProcessor extends
AbstractADLLoaderAnnotationProcessor {

	private Map<Object,Object> context;
	private String buildDir;
	private GraphvizImageConverter gic;
	private boolean generateForDefinitions = false;
	private boolean mindocCompatibility = false;

	private void showComposite(final Definition definition, String instanceName, DotWriter currentInstanceDot, DotWriter currentDefinitionDot) {
		final Component[] subComponents = ((ComponentContainer) definition)
				.getComponents();
		for (int i = 0; i < subComponents.length; i++) {
			currentInstanceDot.addSubComponent(subComponents[i]);
			if (generateForDefinitions)
				currentDefinitionDot.addSubComponentWithDefinitionMode(subComponents[i], mindocCompatibility);
		}

		TreeSet<Binding> bindings = new TreeSet<Binding>( new BindingComparator() );
		for ( Binding binding: ((BindingContainer) definition).getBindings() ) {
			bindings.add(binding);
		}
		for (Binding binding : bindings) {
			currentInstanceDot.addBinding(binding);
			if (generateForDefinitions)
				currentDefinitionDot.addBinding(binding);
		}

		for (int i = 0; i < subComponents.length; i++) {
			final Component subComponent = subComponents[i];
			showComponents(subComponent, instanceName);
		}

	}

	private void showPrimitive(final Definition definition, String instanceName, DotWriter currentInstanceDot, DotWriter currentDefinitionDot) {
		final Source[] sources = ((ImplementationContainer) definition).getSources();

		for (int i = 0; i < sources.length; i++) {
			currentInstanceDot.addSource(sources[i], false);
			if (generateForDefinitions)
				currentDefinitionDot.addSource(sources[i], mindocCompatibility);
		}

	}	

	private void showComponents(final Component component, String instanceName) {

		try {
			final Definition definition = ASTHelper.getResolvedDefinition(component
					.getDefinitionReference(), null, null);
			instanceName = instanceName + "." + component.getName();

			DotWriter currentInstanceDot = new DotWriter(buildDir, instanceName, context);
			DotWriter currentDefinitionDot = null;
			if (generateForDefinitions)
				currentDefinitionDot = new DotWriter(buildDir, definition.getName(), context);

			TreeSet<MindInterface> interfaces = new TreeSet<MindInterface>(new MindInterfaceComparator());
			for (Interface itf : ((InterfaceContainer) definition).getInterfaces())
				interfaces.add((MindInterface) itf); 

			for (MindInterface itf : interfaces) {
				if (itf.getRole()==TypeInterface.SERVER_ROLE) {
					currentInstanceDot.addServer(itf.getName());
					if (generateForDefinitions)
						currentDefinitionDot.addServer(itf.getName());
				}
				if (itf.getRole()==TypeInterface.CLIENT_ROLE) {
					currentInstanceDot.addClient(itf.getName());
					if (generateForDefinitions)
						currentDefinitionDot.addClient(itf.getName());
				}
			}

			if (ASTHelper.isComposite(definition)) {
				showComposite(definition, instanceName, currentInstanceDot, currentDefinitionDot);
			} else if (ASTHelper.isPrimitive(definition)) {
				showPrimitive(definition, instanceName, currentInstanceDot, currentDefinitionDot);
			}
			currentInstanceDot.close();
			if (generateForDefinitions)
				currentDefinitionDot.close();

			gic.convertDotToImage(buildDir, instanceName);

			if (generateForDefinitions) {
				// the mindoc @figure tag uses the package name for folders and subfolder "doc-files"
				if(mindocCompatibility) {
					String packageDirName = PathHelper.fullyQualifiedNameToDirName(definition.getName());
					// here the return dirName will start with "/" : careful !
					// and add the mindoc "doc-files" folder as a convention
					String targetDocFilesDirName = buildDir + packageDirName.substring(1) + File.separator + "doc-files" + File.separator;
					File currentDocFilesDir = new File(targetDocFilesDirName);
					currentDocFilesDir.mkdirs();

					// compute definition short name (removing package)
					String shortDefName = null;
					int i = definition.getName().lastIndexOf('.');
					if (i == -1) shortDefName = definition.getName();
					else shortDefName = definition.getName().substring(i + 1);

					gic.convertDotToImage(buildDir, definition.getName(), targetDocFilesDirName, shortDefName);
				} else		
					gic.convertDotToImage(buildDir, definition.getName());
			}

		} catch (final ADLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ow2.mind.adl.annotation.ADLLoaderAnnotationProcessor#processAnnotation
	 * (org.ow2.mind.annotation.Annotation, org.objectweb.fractal.adl.Node,
	 * org.objectweb.fractal.adl.Definition,
	 * org.ow2.mind.adl.annotation.ADLLoaderPhase, java.util.Map)
	 */
	public Definition processAnnotation(final Annotation annotation,
			final Node node, final Definition definition,
			final ADLLoaderPhase phase, final Map<Object, Object> cont)
					throws ADLException {
		assert annotation instanceof DumpDot;

		// Init
		context = cont;
		DumpDot dotAnno = (DumpDot) annotation;
		generateForDefinitions = dotAnno.generateForDefinitions;
		mindocCompatibility = dotAnno.mindocCompatibility;
		String topLevelName = CompilerContextHelper.getExecutableName(cont);

		if (topLevelName == null)
			// default value
			topLevelName="TopLevel";

		gic = new GraphvizImageConverter(dotAnno.generateImages);

		buildDir = ((File) context.get(BasicOutputFileLocator.OUTPUT_DIR_CONTEXT_KEY)).getPath() + File.separator;

		// Create files
		DotWriter topInstanceDot = new DotWriter(buildDir, topLevelName, cont);

		DotWriter topDefinitionDot = null;
		if (generateForDefinitions) {
			topDefinitionDot = new DotWriter(buildDir, definition.getName(), cont);
		}

		// Start recursion
		if (ASTHelper.isComposite(definition)) {
			showComposite(definition, topLevelName, topInstanceDot, topDefinitionDot);
		} else if (ASTHelper.isPrimitive(definition)) {
			showPrimitive(definition, topLevelName, topInstanceDot, topDefinitionDot);
		}
		topInstanceDot.close();
		if (dotAnno.generateForDefinitions)
			topDefinitionDot.close();

		gic.convertDotToImage(buildDir, topLevelName);
		if (generateForDefinitions) {
			// the mindoc @figure tag uses the package name for folders and subfolder "doc-files"
			if(mindocCompatibility) {
				String packageDirName = PathHelper.fullyQualifiedNameToDirName(definition.getName());
				// here the return dirName will start with "/" : careful !
				// and add the mindoc "doc-files" folder as a convention
				String targetDocFilesDirName = buildDir + packageDirName.substring(1) + File.separator + "doc-files" + File.separator;
				File currentDocFilesDir = new File(targetDocFilesDirName);
				currentDocFilesDir.mkdirs();

				// compute definition short name (removing package)
				String shortDefName = null;
				int i = definition.getName().lastIndexOf('.');
				if (i == -1) shortDefName = definition.getName();
				else shortDefName = definition.getName().substring(i + 1);

				gic.convertDotToImage(buildDir, definition.getName(), targetDocFilesDirName, shortDefName);
			} else		
				gic.convertDotToImage(buildDir, definition.getName());
		}


		return null;
	}

}
