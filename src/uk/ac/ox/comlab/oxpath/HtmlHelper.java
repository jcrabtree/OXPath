/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package uk.ac.ox.comlab.oxpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import uk.ac.ox.comlab.oxpath.TermHelper.TermTypes;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlResetInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Class with methods and enums for facilitating html page interactions by the OXPath framework
 * 
 * @author AndrewJSel
 * 
 */
public class HtmlHelper {

	/**
	 * Returns a FieldType based on the HTML data of the node.
	 * 
	 * @param n
	 *            Node object to return FieldType
	 * @return value from FieldType enum relating HTML form field type of <tt>n</tt>
	 */
	public FieldTypes getFieldType(Node n) {
		FieldTypes ft = null;
		String nodeName = n.getNodeName().toLowerCase();
		if (nodeName == null)
			throw new NullPointerException("Input parameter cannot be null!");
		// these are all mutually exclusive conditions, ft should be set exactly once
		if (nodeName.equals("input")) {
			NamedNodeMap nns = n.getAttributes();
			Node typeRaw = nns.getNamedItem("type");
			if (typeRaw == null)
				throw new NullPointerException("Input node must have a type attribute!");
			String type = typeRaw.getNodeValue().toLowerCase();
			if (type.equals("text")) {
				ft = FieldTypes.TEXT;
			} else if (type.equals("password")) {
				ft = FieldTypes.PASSWORD;
			} else if (type.equals("checkbox")) {
				ft = FieldTypes.CHECKBOX;
			} else if (type.equals("radio")) {
				ft = FieldTypes.RADIOBUTTON;
			} else if (type.equals("button")) {
				ft = FieldTypes.INPUTBUTTON;
			} else if (type.equals("file")) {
				ft = FieldTypes.INPUTFILE;
			} else if (type.equals("image")) {
				ft = FieldTypes.INPUTIMAGE;
			} else if (type.equals("submit")) {
				ft = FieldTypes.INPUTSUBMIT;
			} else if (type.equals("reset")) {
				ft = FieldTypes.INPUTRESET;
			}
			// no need for test here, as XHTML/HTML4 would raise error if tag didn't belong
		} else if (nodeName.equals("textarea")) {
			ft = FieldTypes.TEXTAREA;
		} else if (nodeName.equals("select")) {
			ft = FieldTypes.SELECT;
		} else if (nodeName.equals("button")) {
			ft = FieldTypes.BUTTON;
		} else if (nodeName.equals("a")) {
			ft = FieldTypes.HREF;
		} else {
			ft = FieldTypes.CLICKABLE;
		}
		return ft;
	}

	/**
	 * This method takes an action on a page, including any relevant javascript events listening for said action
	 * 
	 * @param page
	 *            page to perform the action on
	 * @param context
	 *            the context node (upon which to take the action)
	 * @param ft
	 *            type of the context node
	 * @return the <tt>page</tt> input parameter after the action has occurred
	 * @throws BadDataException
	 *             if malformed action token
	 * @throws IOException
	 *             if IO error occurs
	 */
	public HtmlPage takeAction(HtmlPage page, DomNode context, FieldTypes ft, String actionToken) throws BadDataException, IOException {
		ActionTerm at = new ActionTerm(actionToken);
		if (at.hasAttribute()) {
			this.hasAttribute = true;
			this.attributeName = at.getAttributeName();
			if (at.getActionType() == TermTypes.POSITION) {
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getPositionAction(i).toString();
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			} else if (at.getActionType() == TermTypes.EXPLICIT) {
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getExplicitAction(i);
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			} else {// must be keyword
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getKeywordAction(i);
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			}
		} else {
			this.hasAttribute = false;
			this.attributeName = "";
			this.attributeValue = "";
		}
		// decide which action is appropriate
		switch (ft) {// because we checked the node properties in getFieldType(), each of the initial casts is a safe operation
		case TEXT:
			HtmlTextInput textInput = (HtmlTextInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Text Input!");
			textInput.setValueAttribute(at.getExplicitAction(0));
			break;
		case PASSWORD:
			HtmlPasswordInput passwordInput = (HtmlPasswordInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Password Input!");
			passwordInput.setValueAttribute(at.getExplicitAction(0));
			break;
		case CHECKBOX:
			HtmlCheckBoxInput checkBoxInput = (HtmlCheckBoxInput) context;
			// make sure keyword is used
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Checkbox Input");
			}
			checkBoxInput.click();// may raise IOException()
			break;
		case RADIOBUTTON:
			HtmlRadioButtonInput radioButtonInput = (HtmlRadioButtonInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Radio Button Input");
			}
			radioButtonInput.click();// may raise IOException() (even with overridden method)
			break;
		case INPUTBUTTON:
			HtmlButtonInput buttonInput = (HtmlButtonInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Button element");
			}
			buttonInput.click();// may raise IOException()
			break;
		case INPUTFILE:
			HtmlFileInput fileInput = (HtmlFileInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Text element!");
			fileInput.setValueAttribute(at.getExplicitAction(0));// for passing the path to the file that you wish to upload
			// this is our only use for input files here; use fileInput.setData(byte[] data) to assign in memory data instead of loading from a file
			break;
		case INPUTIMAGE:// image button
			HtmlImageInput imageInput = (HtmlImageInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Image element");
			}
			page = (HtmlPage) imageInput.click();// may raise IOException()
			break;
		case INPUTSUBMIT:
			HtmlSubmitInput submitInput = (HtmlSubmitInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD)) {
				throw new BadDataException("Improper Action for Input Submit element");
			}
			page = submitInput.click();// must equal to page, as this is the object the method returns
			break;
		case INPUTRESET:// shouldn't really be used, included so OXPath scripts can navigate around them
			HtmlResetInput resetInput = (HtmlResetInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Reset element");
			}
			resetInput.click();// may raise IOException()
			break;
		case TEXTAREA:
			HtmlTextArea textArea = (HtmlTextArea) context;
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Password Input!");
			textArea.setText(at.getExplicitAction(0));
			break;
		case SELECT:
			HtmlSelect select = (HtmlSelect) context;
			switch (at.getActionType()) {
			case KEYWORD:
				throw new BadDataException("Improper Action for Select Element!");
			case DATABASE:// if the API is used, this should NEVER happen
				throw new BadDataException("Improper Action for Select Element!");
			case EXPLICIT:// reference the option by the displayed text; can't use @value as this may be different
				// have to probe options instead; create a mapping that matches text of the option to the object
				List<HtmlOption> options = new ArrayList<HtmlOption>();
				options = select.getOptions();
				Map<String, HtmlOption> optionsMap = new HashMap<String, HtmlOption>();
				for (HtmlOption hop : options) {// populate the mapping
					optionsMap.put(hop.getText(), hop);// use getText() instead of asText() here because asText() returns only the selected option (per API documentation)
				}
				for (int i = 0; i < at.getSizeActions(); i++) {
					select.setSelectedAttribute(optionsMap.get(at.getExplicitAction(i)), true);// true because we are selecting this value, method does nothing if already selected
				}
				break;
			case POSITION:
				for (int i = 0; i < at.getSizeActions(); i++) {
					select.setSelectedAttribute(select.getOption(at.getPositionAction(i)), true);
				}
				break;
			}
			break;
		case BUTTON:// Should NEVER see this, but sometimes obsolete elements show up
			// these are hard because sometimes they have a scripted event listener and sometimes submit info to server
			// overridden doClickAction() lets us handle as shown below
			HtmlButton button = (HtmlButton) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for button element");
			}
			page = button.click();
			break;
		case HREF:
			HtmlAnchor anchor = (HtmlAnchor) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for anchor element");
			}
			page = anchor.click();// simulates clicking on the link, returning the referenced page, in case there is a javascript listener, this is better than just grabbing the link
			break;
		case CLICKABLE:// if the context element is something else, we are trying to trigger a JavaScript, onclick event listener
			HtmlElement element = (HtmlElement) context;// use HtmlElement, though there is an abstract ClickableElement class it has been deprecated by its developers
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for clickeable element");
			}
			page = element.click();
			break;
		}
		return page;
	}
	
	/**
	 * This method takes an action on a page, including any relevant javascript events listening for said action
	 * 
	 * @param page
	 *            page to perform the action on
	 * @param context
	 *            the context node (upon which to take the action)
	 * @param action String of the action passed from the parser
	 * @param ft
	 *            type of the context node
	 * @return the <tt>page</tt> input parameter after the action has occurred
	 * @throws BadDataException
	 *             if malformed action token
	 * @throws IOException
	 *             if IO error occurs
	 */
	public HtmlPage takeAction(HtmlPage page, DomNode context, String action, FieldTypes ft) throws BadDataException, IOException {
		
		ActionTerm at = new ActionTerm(action);
		if (at.hasAttribute()) {
			this.hasAttribute = true;
			this.attributeName = at.getAttributeName();
			if (at.getActionType() == TermTypes.POSITION) {
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getPositionAction(i).toString();
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			} else if (at.getActionType() == TermTypes.EXPLICIT) {
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getExplicitAction(i);
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			} else {// must be keyword
				for (int i = 0; i < at.getSizeActions(); i++) {
					this.attributeValue += at.getKeywordAction(i);
					// if we aren't finished yet, add a separator for readability
					if ((i + 1) < at.getSizeActions())
						this.attributeValue += "; ";
				}
			}
		} else {
			this.hasAttribute = false;
			this.attributeName = "";
			this.attributeValue = "";
		}
		// decide which action is appropriate
		switch (ft) {// because we checked the node properties in getFieldType(), each of the initial casts is a safe operation
		case TEXT:
			HtmlTextInput textInput = (HtmlTextInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Text Input!");
			textInput.setValueAttribute(at.getExplicitAction(0));
			break;
		case PASSWORD:
			HtmlPasswordInput passwordInput = (HtmlPasswordInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Password Input!");
			passwordInput.setValueAttribute(at.getExplicitAction(0));
			break;
		case CHECKBOX:
			HtmlCheckBoxInput checkBoxInput = (HtmlCheckBoxInput) context;
			// make sure keyword is used
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Checkbox Input");
			}
			checkBoxInput.click();// may raise IOException()
			break;
		case RADIOBUTTON:
			HtmlRadioButtonInput radioButtonInput = (HtmlRadioButtonInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Radio Button Input");
			}
			radioButtonInput.click();// may raise IOException() (even with overridden method)
			break;
		case INPUTBUTTON:
			HtmlButtonInput buttonInput = (HtmlButtonInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Button element");
			}
			buttonInput.click();// may raise IOException()
			break;
		case INPUTFILE:
			HtmlFileInput fileInput = (HtmlFileInput) context;
			// as there is no fixed domain, no need to check, we are using explicit data and only a single entry
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Text element!");
			fileInput.setValueAttribute(at.getExplicitAction(0));// for passing the path to the file that you wish to upload
			// this is our only use for input files here; use fileInput.setData(byte[] data) to assign in memory data instead of loading from a file
			break;
		case INPUTIMAGE:// image button
			HtmlImageInput imageInput = (HtmlImageInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Image element");
			}
			page = (HtmlPage) imageInput.click();// may raise IOException()
			break;
		case INPUTSUBMIT:
			HtmlSubmitInput submitInput = (HtmlSubmitInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD)) {
				throw new BadDataException("Improper Action for Input Submit element");
			}
			page = submitInput.click();// must equal to page, as this is the object the method returns
			break;
		case INPUTRESET:// shouldn't really be used, included so OXPath scripts can navigate around them
			HtmlResetInput resetInput = (HtmlResetInput) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for Input Reset element");
			}
			resetInput.click();// may raise IOException()
			break;
		case TEXTAREA:
			HtmlTextArea textArea = (HtmlTextArea) context;
			if (at.getExplicitAction(0) == null)
				throw new BadDataException("Inproper Action for Password Input!");
			textArea.setText(at.getExplicitAction(0));
			break;
		case SELECT:
			HtmlSelect select = (HtmlSelect) context;
			switch (at.getActionType()) {
			case KEYWORD:
				throw new BadDataException("Improper Action for Select Element!");
			case DATABASE:// if the API is used, this should NEVER happen
				throw new BadDataException("Improper Action for Select Element!");
			case EXPLICIT:// reference the option by the displayed text; can't use @value as this may be different
				// have to probe options instead; create a mapping that matches text of the option to the object
				List<HtmlOption> options = new ArrayList<HtmlOption>();
				options = select.getOptions();
				Map<String, HtmlOption> optionsMap = new HashMap<String, HtmlOption>();
				for (HtmlOption hop : options) {// populate the mapping
					optionsMap.put(hop.getText(), hop);// use getText() instead of asText() here because asText() returns only the selected option (per API documentation)
				}
				for (int i = 0; i < at.getSizeActions(); i++) {
					select.setSelectedAttribute(optionsMap.get(at.getExplicitAction(i)), true);// true because we are selecting this value, method does nothing if already selected
				}
				break;
			case POSITION:
				for (int i = 0; i < at.getSizeActions(); i++) {
					select.setSelectedAttribute(select.getOption(at.getPositionAction(i)), true);
				}
				break;
			}
			break;
		case BUTTON:// Should NEVER see this, but sometimes obsolete elements show up
			// these are hard because sometimes they have a scripted event listener and sometimes submit info to server
			// overridden doClickAction() lets us handle as shown below
			HtmlButton button = (HtmlButton) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for button element");
			}
			page = button.click();
			break;
		case HREF:
			HtmlAnchor anchor = (HtmlAnchor) context;
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for anchor element");
			}
			page = anchor.click();// simulates clicking on the link, returning the referenced page, in case there is a javascript listener, this is better than just grabbing the link
			break;
		case CLICKABLE:// if the context element is something else, we are trying to trigger a JavaScript, onclick event listener
			HtmlElement element = (HtmlElement) context;// use HtmlElement, though there is an abstract ClickableElement class it has been deprecated by its developers
			if (!(at.getActionType() == TermHelper.TermTypes.KEYWORD) || !(at.getExplicitAction(0).equalsIgnoreCase(TermHelper.CLICK))) {
				throw new BadDataException("Improper Action for clickeable element");
			}
			page = element.click();
			break;
		}
		return page;
	}

	/**
	 * Method for analyzing CSS presentation semantics encoded within a DOM node and determining whether it is currently visible to a user. Recursively checks if CSS is hiding the context node or any ancestor
	 * 
	 * @param n
	 *            Node for determining if hidden or not
	 * @return true if Node <tt>n</tt> is currently in the presentation view, false otherwise
	 */
	public static boolean isVisible(DomNode n) {
		boolean rv = true;
		NamedNodeMap nns = n.getAttributes();
		Node styleRaw = nns.getNamedItem("style");
		if (styleRaw == null)
			rv = true;// if no CSS information is there, node is visible in the absence of CSS input
		else {
			// use two scanner objects to retrieve all info and compare against CSS (semantically) hides XHTML elements
			String style = styleRaw.getNodeValue().toLowerCase();
			Scanner scan = new Scanner(style).useDelimiter("\\s*;\\s*");// returns each individual property and its corresponding values
			while (scan.hasNext()) {
				Scanner inScan = new Scanner(scan.next()).useDelimiter("\\s*:\\s*");// now divides properties and their values
				while (inScan.hasNext()) {
					String tempProp = inScan.next();
					if (inScan.hasNext()) {// only need to check if there is a value; a blank property has defaults that aren't hidden (should never see this except in REALLY BAD XHTML)
						String tempValue = inScan.next();
						for (CSSHiddens hidden : CSSHiddens.values()) {// compare to possible ways CSS hides values
							if (tempProp.equals(hidden.getProperty())) {// not super efficient, but doesn't require refinement as long as we are only checking for two ways CSS hides HTML elements
								if (tempValue.equals(hidden.getValue()))
									rv = false;// only time we know CSS will hide an element from user presentation
							}
						}
					}
				}
				inScan.close();
			}
			scan.close();
		}
		// return here so scanners close (if necessary)
		if (rv == false)
			return false;
		else {
			DomNode parent = n.getParentNode();
			if (parent == null) {// we've arrived at the root and all ancestors of the original node are visible
				return true;
			} else
				return isVisible(parent);
		}
	}

	/**
	 * static method that tests whether a node satisfies the conditions set by an additional axis (node name and attributes)
	 * 
	 * @param newContext
	 *            node to compare
	 * @param aan
	 *            Additional Axis Navigator
	 * @return <tt>true</tt> if newContent satisfies the conditions of aan, <tt>false</tt> otherwise
	 */
	public static boolean nodeSatisfiesAxis(DomNode newContext, AdditionalAxisNavigator aan) {
		if (aan.getNodeType().toLowerCase().equals(newContext.getNodeName())) {
			NamedNodeMap nns = newContext.getAttributes();
			for (String attribute : aan.getPredicateAttributeKeys()) {
				Node typeRaw = nns.getNamedItem(attribute.toLowerCase());
				if (typeRaw == null)
					return false;// if the attribute isn't there, false
				String type = typeRaw.getNodeValue().toLowerCase();
				if (!type.equals(aan.getPredicateAttributeValue(attribute))) {
					return false;
				}
			}
			return true;// only if all attribute predicates are satisfied
		} else
			return false;
	}

	/**
	 * method for determining if the last action this object executed has an attribute
	 * 
	 * @return <tt>true</tt> if this object has executed an action with an attribute, <tt>false</tt> otherwise
	 */
	public boolean hasAttribute() {
		return this.hasAttribute;
	}

	/**
	 * method for getting the attribute name of the last action this object executed
	 * 
	 * @return name of attribute of last action this object executed, empty String if action had no attribute
	 */
	public String getLastAttributeName() {
		return this.attributeName;
	}

	/**
	 * method for getting the attribute value of the last action this object executed
	 * 
	 * @return value of attribute of last action this object executed, empty String if action had no attribute
	 */
	public String getLastAttributeValue() {
		return this.attributeValue;
	}

	/**
	 * private String storing if the last action this object executed had an attribute
	 */
	private boolean hasAttribute = false;

	/**
	 * private String storing last attribute name
	 */
	private String attributeName = "";

	/**
	 * private String storing attribute value
	 */
	private String attributeValue = "";

	/**
	 * private enum used to compare to analyze presentation semantics encoded within CSS relating only to whether or not a field is visible; each value consists of a CSS property and its corresponding value
	 */
	public enum CSSHiddens {
		/**
		 * for when display is none in CSS presentation semantics
		 */
		DISPLAYNONE("display:none"),

		/**
		 * for when display is hidden (but room is still provisioned) in CSS presentation semantics
		 */
		VISIBILITYHIDDEN("visibility:hidden");

		/**
		 * constructor for CSSString enum values
		 * 
		 * @param s
		 *            CSS syntactic representation for specific enum value in CSS
		 */
		CSSHiddens(String s) {
			Scanner scan = new Scanner(s).useDelimiter("\\s*:\\s*");
			property = scan.next();
			value = scan.next();
			scan.close();
		}

		/**
		 * method for retrieving the property of a CSSHiddens enum value
		 * 
		 * @return CSS property of the enum value
		 */
		public String getProperty() {
			return this.property;
		}

		/**
		 * method for retrieving the value associated with a CSSHiddens enum value
		 * 
		 * @return CSS value for the property encoded by this enum value
		 */
		public String getValue() {
			return this.value;
		}

		private final String property;
		private final String value;
	}

	// Original (far less effective) data elements for needed CSS constants
	// private static final String DISPLAYNONE = "display:none";
	// private static final String VISIBILITYHIDDEN = "visibility:hidden";
	// private static final String[] CSSSTRINGS = { DISPLAYNONE, VISIBILITYHIDDEN };
//	private static final String[] keywordsRaw = {"click", "dblclick", "keydown", 
//												 "keypress", "keyup", "mousedown", "mousemove", 
//												 "mouseover", "mouseout", "mouseup", "rightclick", 
//												 "scroll", "check", "uncheck", "scroll"};
//	public static final ArrayList<String> KEYWORDS = new ArrayList<String>();
}