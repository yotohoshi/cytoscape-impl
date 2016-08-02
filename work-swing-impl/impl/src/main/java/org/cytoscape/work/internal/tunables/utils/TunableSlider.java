package org.cytoscape.work.internal.tunables.utils;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
public class TunableSlider extends JPanel {
	
	private JSlider slider;
	private JFormattedTextField textField;
	
	private final Number min, max;
	private Number value;
	private final List<Object> listeners;
	private final DecimalFormat format;
	private boolean ignore;
	
	private int m_smin = 0;
	private int m_srange = 100;
	String newline = System.getProperty("line.separator");
	Boolean upper;
	Boolean lower;
	
	public TunableSlider(
			final String title,
			final Number min,
			final Number max,
			final Number value,
			final Boolean lowerstrict,
			final Boolean upperstrict,
			final DecimalFormat format
	) {
		this.min = min;
		this.max = max;
		this.value = value;
		upper = upperstrict;
		lower = lowerstrict;
		listeners = new ArrayList<>();
		this.format = format;

		initUI();
	}

	protected void initUI() {
		textField = new JFormattedTextField(format) {
			@Override
			public Dimension getPreferredSize() {
				final Dimension d = super.getPreferredSize();
				
				if (this.getGraphics() != null) {
					// Set the preferred text field size after it gets a Graphics
					int sw = 16 + this.getGraphics().getFontMetrics().stringWidth(format.format(max.doubleValue()));
					d.width = Math.max(sw, 48);
				}
				
				return d;
			}
		};
		
		textField.setHorizontalAlignment(JTextField.RIGHT);
		
		if (isAquaLAF())
			textField.putClientProperty("JComponent.sizeVariant", "small");
		else if (textField.getFont() != null)
			textField.setFont(textField.getFont().deriveFont(getSmallFontSize()));
		
		slider = new JSlider();
		
		final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		
		if (value instanceof Double || value instanceof Float) {
			final double range = max.doubleValue() - min.doubleValue();
			
			labelTable.put(m_smin, new JLabel(format.format(min.doubleValue())));
			
			if (range % 2 == 0)
				labelTable.put(m_srange / 2, new JLabel(format.format(range / 2)));
			
			labelTable.put(m_srange, new JLabel(format.format(max.doubleValue())));
		} else if (value instanceof Long || value instanceof Integer) {
			final long range = max.longValue() - min.longValue();
			
			labelTable.put(m_smin, new JLabel(format.format(min.longValue())));
			
			if (range % 2 == 0)
				labelTable.put(m_srange / 2, new JLabel(format.format(range / 2)));
			
			labelTable.put(m_srange, new JLabel(format.format(max.longValue())));
		}
		
		slider.setPreferredSize(new Dimension(280, slider.getPreferredSize().height));
		slider.setMajorTickSpacing(m_srange / 4);
		slider.setMinorTickSpacing(m_srange / 20);
		slider.setLabelTable(labelTable);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		setSliderValue();
		setFieldValue();
		
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (ignore) return;
				ignore = true;
				// update the value
				value = getSliderValue();
				// set text field value
				setFieldValue();
				// fire event
				fireChangeEvent();
				ignore = false;
			}
		});
		
		textField.addActionListener((ActionEvent e) -> {
			textFieldValueChanged();
		});
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textFieldValueChanged();
			}
		});
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(slider, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(textField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, false)
				.addComponent(slider)
				.addGroup(layout.createSequentialGroup()
						.addGap(isAquaLAF() ? 4 : 0)
						.addComponent(textField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)

		);
		
		if (isAquaLAF())
			setOpaque(false);
		
		// Change the slider's label sizes -- only works if it's done after the slider has been added to
		// its parent container and had its UI assigned
		final Font tickFont = slider.getFont().deriveFont(getSmallFontSize());
		
		for (Enumeration<Integer> enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
			int k = enumeration.nextElement();
			final JLabel label = labelTable.get(k);
			label.setFont(tickFont); // Updates the font size
			label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
		}
	}
	
	public Number getValue(){
		return getFieldValue();
	}

	public void setValue(final Number value) {
		ignore = true;
		this.value = value;
		setSliderValue();
		setFieldValue();
		ignore = false;
	}
	
	private Number getSliderValue() {
		if (value instanceof Integer) {
			int val = slider.getValue();
			int min = this.min.intValue();
			int max = this.max.intValue();
			
			if (upper) max--;
			if (lower) min++;
			
			if (val > max)
				val = max;
			else if (val < min)
				val = min;
			
			return new Integer(min + (val - m_smin) * (max - min) / m_srange);
		} else if (value instanceof Long) {
			long val = slider.getValue();
			long min = this.min.longValue();
			long max = this.max.longValue();
			
			if (upper) max--;
			if (lower) min++;
			
			if (val > max)
				val = max;
			else if (val < min)
				val = min;
			
			return new Long(min + (val - m_smin) * (max - min) / m_srange);
		} else {
			double f = (slider.getValue() - m_smin) / (double) m_srange;
			double min = this.min.doubleValue();
			double max = this.max.doubleValue();
			double val = min + f * (max - min);
			
			if (upper) max -= 0.000000001;
			if (lower) min += 0.000000001;
			
			if (val < min)
				val = min;
			else if (val > max)
				val = max;
			
			return (value instanceof Double ? (Number) new Double(val) : new Float((float) val));
		}
	}
  
	private void setSliderValue() {
		int val;
		
		if (value instanceof Double || value instanceof Float) {
			double value = this.value.doubleValue();
			double min = this.min.doubleValue();
			double max = this.max.doubleValue();
			val = m_smin + (int) Math.round(m_srange * ((value - min) / (max - min)));
			
			if (upper) max -= 0.0001;
			if (lower) min += 0.0001;
		} else {
			long value = this.value.longValue();
			long min = this.min.longValue();
			long max = this.max.longValue();
			val = m_smin + (int) ((m_srange * (value - min)) / (max - min));
			
			if (upper) max--;
			if (lower) min++;
		}
		
		slider.setValue(val);
	}
  
	private Number getFieldValue(){
		Double val = null;
		Number n = format.parse(textField.getText(), new ParsePosition(0));
		
		if (n == null) {
			try {
				val = Double.valueOf(textField.getText());
			} catch (NumberFormatException nfe) {
				textField.setBackground(Color.RED);
				JOptionPane.showMessageDialog(null, "Please enter a Value", "Alert", JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				textField.setBackground(UIManager.getColor("TextField.background"));

				try {
					val = value.doubleValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			val = n.doubleValue();
		}
		
		if (value instanceof Double || value instanceof Float) {
			if (val < min.doubleValue()) {
				textField.setBackground(Color.RED);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.doubleValue()+") is less than lower limit ("+format.format(min.doubleValue())+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				textField.setBackground(UIManager.getColor("TextField.background"));
				
				return value;
			}
			if (val > max.doubleValue()) {
				textField.setBackground(Color.RED);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.doubleValue()+") is more than upper limit ("+format.format(max.doubleValue())+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				textField.setBackground(UIManager.getColor("TextField.background"));
				
				return value;
			}
			
			return value instanceof Double ? (Number)val.doubleValue() : val.floatValue();
		} else {
			if (val < min.longValue()) {
				textField.setBackground(Color.RED);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.longValue()+") is less than lower limit ("+min.longValue()+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				textField.setBackground(UIManager.getColor("TextField.background"));
				
				return value;
			}
			if (val > max.longValue()) {
				textField.setBackground(Color.RED);
				JOptionPane.showMessageDialog(
						null,
						"Value ("+val.longValue()+") is much than upper limit ("+max.longValue()+")"+newline+"Value will be set to default : "+value,
						"Alert",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				textField.setBackground(UIManager.getColor("TextField.background"));
				
				return value;
			}
			
			return value instanceof Long ? (Number)val.longValue() : val.intValue();
		}
	}
	
	private void setFieldValue() {
		textField.setValue(value);
	}
	
	public void addChangeListener(ChangeListener cl) {
		if (!listeners.contains(cl))
			listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}
	
	protected void fireChangeEvent() {
		Iterator<Object> iter = listeners.iterator();
		ChangeEvent evt = new ChangeEvent(this);
		
		while (iter.hasNext()) {
			ChangeListener cl = (ChangeListener) iter.next();
			cl.stateChanged(evt);
		}
	}
	
	private void textFieldValueChanged() {
		if (ignore) return;
		
		ignore = true;
		Number v = getFieldValue();
		
		if (v != value) {
			// update the value
			value = v;
			// set slider value
			setSliderValue();
		}
		
		// fire event
		fireChangeEvent();
		ignore = false;
	}
}
