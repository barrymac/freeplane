/*
 * Freeplane - A Program for creating and viewing MindmapsCopyright (C) 2000-2006
 * Joerg Mueller, Daniel Polansky, Christian Foltin and others.See COPYING for
 * DetailsThis program is free software; you can redistribute it and/ormodify it
 * under the terms of the GNU General Public Licenseas published by the Free
 * Software Foundation; either version 2of the License, or (at your option) any
 * later version.This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty ofMERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See theGNU General Public License for
 * more details.You should have received a copy of the GNU General Public
 * Licensealong with this program; if not, write to the Free SoftwareFoundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. Created on
 * 02.09.2006
 */
/*
 * $Id: ScriptingEngine.java,v 1.1.2.20 2008/04/18 21:18:26 christianfoltin Exp
 * $
 */
package org.freeplane.plugin.script;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OptionalDontShowMeAgainDialog;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.attribute.NodeAttributeTableModel;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.attribute.MAttributeController;
import org.freeplane.features.mindmapmode.text.MTextController;
import org.freeplane.main.application.FreeplaneSecurityManager;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * @author foltin
 */
public class ScriptingEngine {
	public interface IErrorHandler {
		void gotoLine(int pLineNumber);
	}

	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING = "execute_scripts_without_asking";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION = "execute_scripts_without_exec_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION = "execute_scripts_without_file_restriction";
	public static final String RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION = "execute_scripts_without_network_restriction";
	public static final String RESOURCES_SCRIPT_USER_KEY_NAME_FOR_SIGNING = "script_user_key_name_for_signing";
	public static final String RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED = "signed_script_are_trusted";
	public static final String RESOURCES_SCRIPT_DIRECTORIES = "script_directories";
	public static final String SCRIPT_PREFIX = "script";
	private static final long serialVersionUID = 1L;
	private static final HashMap<String, Object> sScriptCookies = new HashMap<String, Object>();
	private static Boolean noUserPermissionRequired = false;
	private static Pattern attributeNamePattern = Pattern.compile("^([a-zA-Z0-9_]*)=");

	/**
	 * @return the result of the script, or null, if the user has cancelled.
	 * @throws ExecuteScriptException on errors
	 */
	static Object executeScript(final NodeModel node, String script, final MModeController pMindMapController,
	                            final IErrorHandler pErrorHandler, final PrintStream pOutStream)
	        throws ExecuteScriptException {
		if (!noUserPermissionRequired) {
			final int showResult = OptionalDontShowMeAgainDialog.show(pMindMapController.getController(),
			    "really_execute_script", "confirmation", RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
			    OptionalDontShowMeAgainDialog.ONLY_OK_SELECTION_IS_STORED);
			if (showResult != JOptionPane.OK_OPTION) {
				return null;
			}
		}
		noUserPermissionRequired = Boolean.TRUE;
		final Binding binding = new Binding();
		binding.setVariable("c", ProxyFactory.createController(pMindMapController));
		binding.setVariable("node", ProxyFactory.createNode(node, pMindMapController));
		binding.setVariable("cookies", ScriptingEngine.sScriptCookies);
		boolean assignResult = false;
		String assignTo = null;
		final Matcher matcher = attributeNamePattern.matcher(script);
		if (matcher.matches()) {
			assignResult = true;
			String attributeName = matcher.group(1);
			if (attributeName.length() == 0) {
				script = script.substring(1);
			}
			else {
				assignTo = attributeName;
				script = script.substring(matcher.end());
			}
		}
		/*
		 * get preferences (and store them again after the script execution,
		 * such that the scripts are not able to change them).
		 */
		final String executeWithoutAsking = ResourceController.getResourceController().getProperty(
		    RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING);
		final String executeWithoutFileRestriction = ResourceController.getResourceController().getProperty(
		    RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION);
		final String executeWithoutNetworkRestriction = ResourceController.getResourceController().getProperty(
		    RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION);
		final String executeWithoutExecRestriction = ResourceController.getResourceController().getProperty(
		    RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION);
		final String signedScriptsWithoutRestriction = ResourceController.getResourceController().getProperty(
		    RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED);
		/* *************** */
		/* Signature */
		/* *************** */
		final PrintStream oldOut = System.out;
		boolean filePerm = Boolean.parseBoolean(executeWithoutFileRestriction);
		boolean networkPerm = Boolean.parseBoolean(executeWithoutNetworkRestriction);
		boolean execPerm = Boolean.parseBoolean(executeWithoutExecRestriction);
		if (Boolean.parseBoolean(signedScriptsWithoutRestriction)) {
			final boolean isSigned = new SignedScriptHandler().isScriptSigned(script, pOutStream);
			if (isSigned) {
				filePerm = true;
				networkPerm = true;
				execPerm = true;
			}
		}
		final ScriptingSecurityManager scriptingSecurityManager = new ScriptingSecurityManager(filePerm, networkPerm,
		    execPerm);
		final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
		try {
			System.setOut(pOutStream);
			final GroovyShell shell = new GroovyShell(binding) {
				/**
				 * Evaluates some script against the current Binding and returns the result
				 *
				 * @param in       the stream reading the script
				 * @param fileName is the logical file name of the script (which is used to create the class name of the script)
				 */
				@Override
				public Object evaluate(final InputStream in, final String fileName) throws CompilationFailedException {
					Script script = null;
					try {
						script = parse(in, fileName);
						securityManager.setFinalSecurityManager(scriptingSecurityManager);
						return script.run();
					}
					finally {
						if (script != null) {
							InvokerHelper.removeClass(script.getClass());
							securityManager.removeFinalSecurityManager(scriptingSecurityManager);
						}
					}
				}
			};
			Object result = shell.evaluate(script);
			if (assignResult && result != null) {
				if (assignTo == null) {
					((MTextController) TextController.getController(pMindMapController)).setNodeText(node, result
					    .toString());
				}
				else {
					((MAttributeController) AttributeController.getController(pMindMapController)).editAttribute(node,
					    assignTo, result.toString());
				}
			}
			return result;
		}
		catch (final GroovyRuntimeException e1) {
			/*
			 * Cover exceptions in normal security context (ie. no problem with
			 * (log) file writing etc.)
			 */
			final String resultString = e1.getMessage();
			pOutStream.print("message: " + resultString);
			final ModuleNode module = e1.getModule();
			final ASTNode astNode = e1.getNode();
			int lineNumber = -1;
			if (module != null) {
				lineNumber = module.getLineNumber();
			}
			else if (astNode != null) {
				lineNumber = astNode.getLineNumber();
			}
			else {
				lineNumber = ScriptingEngine.findLineNumberInString(resultString, lineNumber);
			}
			pOutStream.print("Line number: " + lineNumber);
			pErrorHandler.gotoLine(lineNumber);
			throw new ExecuteScriptException(e1.getMessage(), e1);
		}
		catch (final Throwable e2) {
			pMindMapController.getMapController().select(node);
			LogUtils.warn(e2);
			pOutStream.print(e2.getMessage());
			final String cause = ((e2.getCause() != null) ? e2.getCause().getMessage() : "");
			final String message = ((e2.getMessage() != null) ? e2.getMessage() : "");
			UITools.errorMessage(e2.getClass().getName() + ": " + cause
			        + ((cause.length() != 0 && message.length() != 0) ? ", " : "") + message);
			throw new ExecuteScriptException(e2.getMessage(), e2);
		}
		finally {
			System.setOut(oldOut);
			/* restore preferences (and assure that the values are unchanged!). */
			ResourceController.getResourceController().setProperty(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING,
			    executeWithoutAsking);
			ResourceController.getResourceController().setProperty(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_FILE_RESTRICTION,
			    executeWithoutFileRestriction);
			ResourceController.getResourceController().setProperty(
			    RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION, executeWithoutNetworkRestriction);
			ResourceController.getResourceController().setProperty(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION,
			    executeWithoutExecRestriction);
			ResourceController.getResourceController().setProperty(RESOURCES_SIGNED_SCRIPT_ARE_TRUSTED,
			    signedScriptsWithoutRestriction);
		}
	}

	public static int findLineNumberInString(final String resultString, int lineNumber) {
		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*@ line ([0-9]+).*",
		    java.util.regex.Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(resultString);
		if (matcher.matches()) {
			lineNumber = Integer.parseInt(matcher.group(1));
		}
		return lineNumber;
	}

	public static Object executeScript(final MModeController modeController, final NodeModel node, final String script) {
		return ScriptingEngine.executeScript(node, script, modeController, new IErrorHandler() {
			public void gotoLine(final int pLineNumber) {
			}
		}, System.out);
	}

	static Object executeScriptRecursive(final MModeController modeController, final NodeModel node, final String script) {
		for (final Iterator<NodeModel> iter = modeController.getMapController().childrenUnfolded(node); iter.hasNext();) {
			executeScriptRecursive(modeController, iter.next(), script);
		}
		return executeScript(modeController, node, script);
	}

	static boolean performScriptOperationRecursive(final MModeController modeController, final NodeModel node) {
		for (final Iterator<NodeModel> iter = modeController.getMapController().childrenUnfolded(node); iter.hasNext();) {
			final NodeModel child = iter.next();
			if (!performScriptOperationRecursive(modeController, child)) {
				return false;
			}
		}
		return performScriptOperation(modeController, node);
	}

	static boolean performScriptOperation(final MModeController modeController, final NodeModel node) {
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		if (attributes == null) {
			return true;
		}
		for (int row = 0; row < attributes.getRowCount(); ++row) {
			final String attrKey = (String) attributes.getName(row);
			final String script = (String) attributes.getValue(row);
			if (attrKey.startsWith(ScriptingEngine.SCRIPT_PREFIX)) {
				executeScript(modeController, node, script);
			}
		}
		return true;
	}

	static void setNoUserPermissionRequired(final Boolean noUserPermissionRequired) {
		ScriptingEngine.noUserPermissionRequired = noUserPermissionRequired;
	}

	static Boolean getNoUserPermissionRequired() {
		return noUserPermissionRequired;
	}
}
