/*******************************************************************************
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.ide.publish;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.dirigible.ide.common.CommonIDEParameters;
import org.eclipse.dirigible.ide.common.ExtensionPointUtils;
import org.eclipse.dirigible.ide.workspace.dual.WorkspaceLocator;
import org.eclipse.dirigible.repository.ext.security.IRoles;
import org.eclipse.dirigible.repository.logging.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Creates and manages all the registered {@link IPublisher} objects.
 * Global 'Activate' and 'Publish' processes are performed by PublishManager
 */
public final class PublishManager {

	private static final Logger logger = Logger.getLogger(PublishManager.class);

	private static final String PUBLISH_ERROR = Messages.getString("PublishManager.PUBLISH_ERROR"); //$NON-NLS-1$

	private static final String THE_USER_S_DOES_NOT_HAVE_OPERATOR_ROLE_TO_PERFORM_PUBLISH_OPERATION = Messages
			.getString("PublishManager.THE_USER_S_DOES_NOT_HAVE_OPERATOR_ROLE_TO_PERFORM_PUBLISH_OPERATION"); //$NON-NLS-1$

	private static final String PUBLISHER_EXTENSION_HAS_AN_INVALID_IMPLEMENTING_CLASS_CONFIGURED = Messages
			.getString("PublishManager.PUBLISHER_EXTENSION_HAS_AN_INVALID_IMPLEMENTING_CLASS_CONFIGURED"); //$NON-NLS-1$

	private static final String COULD_NOT_CREATE_PUBLISHER_INSTANCE = Messages.getString("PublishManager.COULD_NOT_CREATE_PUBLISHER_INSTANCE"); //$NON-NLS-1$

	private static final String EXTENSION_POINT_0_COULD_NOT_BE_FOUND = Messages.getString("PublishManager.EXTENSION_POINT_0_COULD_NOT_BE_FOUND"); //$NON-NLS-1$

	private static final String PUBLISHER_EXTENSION_POINT_ID = "org.eclipse.dirigible.ide.publish.publisher"; //$NON-NLS-1$

	private static final String PUBLISHER_ELEMENT_NAME = "publisher"; //$NON-NLS-1$

	private static final String PUBLISHER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	private static final String UNKNOWN_SELECTION_TYPE = "Unknown Selection Type"; //$NON-NLS-1$

	static List<IPublisher> publishers = null;

	public static final int ACTION_PUBLISH = 1;

	public static final int ACTION_ACTIVATE = 2;

	public static final int ACTION_TEMPLATE = 3;

	/**
	 * Returns a list {@link IPublisher}
	 *
	 * @return a list of {@link IPublisher} or <code>null</code> such is not
	 *         found that can handle the specified project type.
	 */
	public static List<IPublisher> getPublishers() {

		synchronized (PublishManager.class) {
			if (publishers == null) {
				publishers = new ArrayList<IPublisher>();
				final IExtensionPoint extensionPoint = ExtensionPointUtils.getExtensionPoint(PUBLISHER_EXTENSION_POINT_ID);
				if (extensionPoint == null) {
					throw new PublishManagerException(format(EXTENSION_POINT_0_COULD_NOT_BE_FOUND, PUBLISHER_EXTENSION_POINT_ID));
				}
				final IConfigurationElement[] publisherElements = getPublisherElements(extensionPoint.getExtensions());

				String publisherName = null;
				try {
					for (IConfigurationElement publisherElement : publisherElements) {
						publisherName = publisherElement.getAttribute(PUBLISHER_CLASS_ATTRIBUTE);
						publishers.add(createPublisher(publisherElement));
					}
				} catch (CoreException ex) {
					throw new PublishManagerException(String.format(COULD_NOT_CREATE_PUBLISHER_INSTANCE, publisherName), ex);
				}
			}
		}
		return publishers;
	}

	public static IProject[] getProjects(ISelection selection) {
		if ((selection == null) || !(selection instanceof IStructuredSelection)) {
			logger.error(UNKNOWN_SELECTION_TYPE);
			return new IProject[0];
		}
		final Set<IProject> result = new HashSet<IProject>();
		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		IProject project = null;
		for (Object element : structuredSelection.toArray()) {
			project = getProject(element);
			if (project != null) {
				result.add(project);
			}
		}
		return result.toArray(new IProject[0]);
	}

	public static IFile[] getFiles(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			logger.error(UNKNOWN_SELECTION_TYPE);
			return new IFile[0];
		}
		final Set<IFile> result = new HashSet<IFile>();
		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Object element : structuredSelection.toArray()) {
			if (element instanceof IFile) {
				result.add((IFile) element);
			}
		}
		return result.toArray(new IFile[0]);
	}

	private static IProject getProject(Object element) {
		IProject project = null;
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IFile) {
			project = ((IFile) element).getProject();
		} else if (element instanceof IFolder) {
			project = ((IFolder) element).getProject();
		}
		return project;
	}

	public static void activateProject(IProject project, HttpServletRequest request) throws PublishException {
		publish(project, ACTION_ACTIVATE, request);
		logger.info(String.format("Project %s has been activated successfully", project.getName()));
	}

	public static void publishProject(IProject project, HttpServletRequest request) throws PublishException {
		if (!CommonIDEParameters.isUserInRole(IRoles.ROLE_OPERATOR, request)) {
			String message = String.format(THE_USER_S_DOES_NOT_HAVE_OPERATOR_ROLE_TO_PERFORM_PUBLISH_OPERATION,
					CommonIDEParameters.getUserName(request));
			try {
				MessageDialog.openError(null, PUBLISH_ERROR, message);
			} catch (Throwable t) {
				logger.error(message, t);
			}
			return;
		}
		publish(project, ACTION_PUBLISH, request);
		logger.info(String.format("Project %s has been published successfully", project.getName()));
	}

	public static void publishTemplate(IProject project, HttpServletRequest request) throws PublishException {
		if (!CommonIDEParameters.isUserInRole(IRoles.ROLE_OPERATOR, request)) {
			String message = String.format(THE_USER_S_DOES_NOT_HAVE_OPERATOR_ROLE_TO_PERFORM_PUBLISH_OPERATION,
					CommonIDEParameters.getUserName(request));
			try {
				MessageDialog.openError(null, PUBLISH_ERROR, message);
			} catch (Throwable t) {
				logger.error(message, t);
			}
			return;
		}
		publish(project, ACTION_TEMPLATE, request);
		logger.info(String.format("Project %s has been activated successfully as a template", project.getName()));
	}

	private static void publish(IProject project, int action, HttpServletRequest request) throws PublishException {
		final List<IPublisher> registeredPublishers = PublishManager.getPublishers();

		for (IPublisher iPublisher : registeredPublishers) {

			try {
				IPublisher publisher = iPublisher;
				switch (action) {
					case ACTION_ACTIVATE:
						publisher.activate(project, request);
						break;
					case ACTION_PUBLISH:
						publisher.publish(project, request);
						break;
					case ACTION_TEMPLATE:
						publisher.template(project, request);
						break;
					default:
						throw new PublishException("Unknown action for publish");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new PublishException(e.getMessage(), e);
			}
		}
	}

	private static IConfigurationElement[] getPublisherElements(IExtension[] extensions) {
		final List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension.getConfigurationElements()) {
				if (PUBLISHER_ELEMENT_NAME.equals(element.getName())) {
					result.add(element);
				}
			}
		}
		return result.toArray(new IConfigurationElement[0]);
	}

	private static IPublisher createPublisher(IConfigurationElement publisherElement) throws CoreException {
		final Object publisher = publisherElement.createExecutableExtension(PUBLISHER_CLASS_ATTRIBUTE);
		if (!(publisher instanceof IPublisher)) {
			throw new PublishManagerException(PUBLISHER_EXTENSION_HAS_AN_INVALID_IMPLEMENTING_CLASS_CONFIGURED);
		}
		return (IPublisher) publisher;
	}

	private PublishManager() {
		super();
	}

	public static void activateAll(HttpServletRequest request) throws PublishException {
		IProject[] projects = WorkspaceLocator.getWorkspace(request).getRoot().getProjects();
		for (IProject project : projects) {
			StringBuffer buff = new StringBuffer();
			try {
				activateProject(project, request);
			} catch (PublishException e) {
				buff.append(String.format("Project %s has thrown an error during activation: %s \n", project.getName(), e.getMessage()));
			}
			if (buff.length() > 0) {
				throw new PublishException(buff.toString());
			}
		}
	}

	public static void publishAll(HttpServletRequest request) throws PublishException {
		IProject[] projects = WorkspaceLocator.getWorkspace(request).getRoot().getProjects();
		for (IProject project : projects) {
			StringBuffer buff = new StringBuffer();
			try {
				publishProject(project, request);
			} catch (PublishException e) {
				buff.append(String.format("Project %s has thrown an error during publish: %s \n", project.getName(), e.getMessage()));
			}
			if (buff.length() > 0) {
				throw new PublishException(buff.toString());
			}
		}
	}
}