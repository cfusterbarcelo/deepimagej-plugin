/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 *
 * Conditions of use: You are free to use this software for research or educational purposes. 
 * In addition, we expect you to include adequate citations and acknowledgments whenever you 
 * present or publish results that are based on it.
 * 
 * Reference: DeepImageJ: A user-friendly plugin to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, L. Donati, M. Unser, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2019.
 *
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 *
 * Corresponding authors: mamunozb@ing.uc3m.es, daniel.sage@epfl.ch
 *
 */

/*
 * Copyright 2019. Universidad Carlos III, Madrid, Spain and EPFL, Lausanne, Switzerland.
 * 
 * This file is part of DeepImageJ.
 * 
 * DeepImageJ is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * DeepImageJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeepImageJ. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package deepimagej.stamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import deepimagej.BuildDialog;
import deepimagej.Constants;
import deepimagej.Parameters;
import deepimagej.components.GridPanel;
import deepimagej.components.HTMLPane;
import deepimagej.tools.DijTensor;
import deepimagej.tools.Index;
import ij.IJ;

public class OutputDimensionStamp extends AbstractStamp implements ActionListener {

	private static List<JTextField> firstRowList	= new ArrayList<JTextField>();
	private static List<JTextField> secondRowList	= new ArrayList<JTextField>();
	private static List<JTextField> thirdRowList	= new ArrayList<JTextField>();

	private static List<JComboBox<String>>  cmbRowList		= new ArrayList<JComboBox<String>>();
	
	private static JTextField		txtExportDir	= new JTextField(IJ.getDirectory("imagej") + File.separator +
																	 "models" + File.separator + "results" + File.separator);
	
	private static GridPanel		pnOutputInfo	= new GridPanel(true);
	private static GridPanel		firstRow		= new GridPanel(true);
	private static GridPanel		secondRow		= new GridPanel(true);
	private static GridPanel		thirdRow		= new GridPanel(true);
	private static GridPanel		pnRange			= new GridPanel(true);

	private static JComboBox<String>referenceImage 	= new JComboBox<String>(new String[] {"aux"});
	private static JLabel			lblName			= new JLabel("Name");
	

	private static JButton 			bnNextOutput 	= new JButton("Next Output");
	private static JButton 			bnPrevOutput 	= new JButton("Previous Output");
	
	private static int				outputCounter	= 0;
	private String					model		  = "";
	
	private static double[] 		rangeOptions = {Double.NEGATIVE_INFINITY, (double) -1, (double) 0, (double) 1, Double.POSITIVE_INFINITY};
	
	private static JComboBox<String>	cmbRangeLow = new JComboBox<String>(new String [] {"-inf", "-1", "0", "1", "inf"});
	private static JComboBox<String>	cmbRangeHigh = new JComboBox<String>(new String [] {"-inf", "-1", "0", "1", "inf"});
	
	

	public OutputDimensionStamp(BuildDialog parent) {
		super(parent);
		buildPanel();
		cmbRangeHigh.setSelectedIndex(4);
	}

	@Override
	public void buildPanel() {
		
		HTMLPane info = new HTMLPane(Constants.width, 150);
		info.append("h2", "Input size constraints");
		info.append("p", "<b>Patch size (Q) </b>: If the network has not a predetermined input size, patch decomposition of default size <i>Q</i> is allowed.");
		info.append("p", "<b>Padding (P) </b>: To preserve the input size at the output, convolutions are calculated using zero padding boundary conditions of size <i>P</i>.");
		info.append("p", "<b>Multiple factor (m) </b>: If the network has an auto-encoder architecture, the size of each dimension of the input image, has to be multiple of a minimum size m.");
		
		pnOutputInfo.setBorder(BorderFactory.createEtchedBorder());
		lblName.setText("aux");
		pnOutputInfo.place(0, 0, lblName);
		//pnOutputInfo.place(0, 1, checkIsImage);
		pnOutputInfo.place(1, 0, referenceImage);
		referenceImage.setEditable(false);
		JLabel dimLetterAux = new JLabel("aux");
		JTextField txtAux = new JTextField("aux");
		firstRow.place(0, 0, dimLetterAux); firstRow.place(1, 0, txtAux);
		secondRow.place(0, 0, dimLetterAux); secondRow.place(1, 0, txtAux);
		thirdRow.place(0, 0, dimLetterAux); thirdRow.place(1, 0, txtAux);
		pnOutputInfo.place(2, 0, 1, 2, firstRow);
		pnOutputInfo.place(3,  0, 1, 2, secondRow);
		pnOutputInfo.place(4,  0, 1, 2, thirdRow);
		
		GridPanel pnRange1 = new GridPanel(true);
		JLabel lblRange1 = new JLabel("Data Range lower bound");
		pnRange1.place(0, 0, lblRange1);
		pnRange1.place(0, 1, cmbRangeLow);
		
		GridPanel pnRange2 = new GridPanel(true);
		JLabel lblRange2 = new JLabel("Data Range lower bound");
		pnRange2.place(0, 0, lblRange2);
		pnRange2.place(0, 1, cmbRangeHigh);

		//pnRange = new GridPanel(true);
		pnRange.place(0, 0, pnRange1);
		pnRange.place(0, 1, pnRange2);
		pnOutputInfo.place(5, 0, 2, 1, pnRange);
		
		lblRange1.setVisible(true);
		lblRange2.setVisible(true);
		pnRange.setVisible(true);
		
		cmbRangeLow.setVisible(true);
		cmbRangeHigh.setVisible(true);
		
		cmbRangeLow.setEditable(false);
		cmbRangeHigh.setEditable(false);

		GridPanel buttons = new GridPanel(true);
		buttons.setBorder(BorderFactory.createEtchedBorder());
		buttons.place(0, 0, bnPrevOutput);
		buttons.place(0, 1, bnNextOutput);
		
		JPanel pn = new JPanel();
		pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
		pn.add(info.getPane());
		pn.add(pnOutputInfo);
		pn.add(buttons, BorderLayout.SOUTH);
		
		panel.removeAll();
		panel.add(pn);
		
		bnNextOutput.addActionListener(this);
		bnPrevOutput.addActionListener(this);
		
	}
	
	@Override
	public void init() {
		Parameters params = parent.getDeepPlugin().params;
		// Set the screen at the first input if the model changes
		String modelOfInterest = params.path2Model;
		if (!modelOfInterest.equals(model)) {
			model = modelOfInterest;
			outputCounter = 0;
		}
		bnNextOutput.setEnabled(true);
		bnPrevOutput.setEnabled(true);
		referenceImage.removeAllItems();
		for (DijTensor in : params.inputList) {
			if (in.tensorType.contains("image"))
				referenceImage.addItem(in.name);
		}
		updateInterface(params);
	
	}

	@Override
	public boolean finish() {
		Parameters params = parent.getDeepPlugin().params;
		saveOutputData(params);
		for (DijTensor tensor : params.outputList) {
			if (!tensor.finished){
				IJ.error("You need to fill information for every input tensor");
				return false;
			}
		}
		
		return true;
	}
	
	public static void updateInterface(Parameters params) {
		
		// Check how many outputs there are to enable or not
		// the "next" and "back" buttons
		if (outputCounter == 0) {
			bnPrevOutput.setEnabled(false);
		} else {
			bnPrevOutput.setEnabled(true);
		}
		if (outputCounter < (params.outputList.size() - 1)) {
			bnNextOutput.setEnabled(true);
		} else {
			bnNextOutput.setEnabled(false);
		}

		lblName.setText(params.outputList.get(outputCounter).name);
		// Reinitialise all the params
		pnOutputInfo.removeAll();
		firstRow.removeAll();
		secondRow.removeAll();
		thirdRow.removeAll();
		firstRowList = new ArrayList<JTextField>();
		secondRowList = new ArrayList<JTextField>();
		thirdRowList = new ArrayList<JTextField>();
		if (params.outputList.get(outputCounter).tensorType.contains("image") && !params.pyramidalNetwork) {
			// Build the panel
			getPanelForImage(params);
		} else if (params.outputList.get(outputCounter).tensorType.contains("image") && params.pyramidalNetwork) {
			// Build the panel
			getPanelForImagePyramidalNet(params);
		}else if (params.outputList.get(outputCounter).tensorType.contains("list")) {
			getPanelForList(params);
		} else {
			outputCounter ++;
		}
		pnOutputInfo.revalidate();
		pnOutputInfo.repaint();
	
	}
	
	/*
	 * Method to retrieve from the UI the information necessary to build a 
	 * list from the tensor outputed by the model
	 */
	public static boolean saveOutputDataForList(Parameters params) {
		// There is no offset or halo in the case of the outùt being a list.
		// There is also no scale, but for convenience we will set it to 1.
		DijTensor tensor = params.outputList.get(outputCounter);
		tensor.scale = new float[tensor.tensor_shape.length];
		tensor.halo = new int[tensor.tensor_shape.length];
		tensor.offset = new int[tensor.tensor_shape.length];
		// Set the scale equal to 1 for every dimension
		for (int i = 0; i < tensor.scale.length; i ++)
			tensor.scale[i] = 1;
		// Now do the important thing in this step. Change the dimension letters
		// by C if it correspond to the column, or R if it corresponds to row
		int batchInd = DijTensor.getBatchInd(tensor.form);
		// Form containing rows and cols
		String newForm = "";
		
		int cmbCount = 0;
		for (int i = 0; i < params.outputList.get(outputCounter).scale.length; i++) {
			if (i == batchInd) {
				newForm = newForm + "N";
			} else {
				String selectedItem = String.valueOf(cmbRowList.get(cmbCount).getSelectedItem());
				String letter = selectedItem.split("")[0];
				cmbCount ++;
				if (newForm.indexOf(letter) == -1) {
					newForm = newForm + letter;
				} else {
					IJ.error("You cannot select the same field in both combo boxes.");
					return false;
				}
			}
		}
		tensor.form = newForm;
		return true;
	}
	
	/*
	 * Method to retrieve from the UI the information necessary to build an 
	 * image from the tensor outputed by the model
	 */
	public static boolean saveOutputDataForImage(Parameters params) {
		// Save all the information for the output given by the variable 'outputInd'
		String ref = (String) referenceImage.getSelectedItem();
		params.outputList.get(outputCounter).referenceImage = ref;
		// Get the reference tensor
		DijTensor refInput = DijTensor.retrieveByName(ref, params.inputList);
		
		params.outputList.get(outputCounter).scale = new float[params.outputList.get(outputCounter).tensor_shape.length];
		params.outputList.get(outputCounter).halo = new int[params.outputList.get(outputCounter).tensor_shape.length];
		params.outputList.get(outputCounter).offset = new int[params.outputList.get(outputCounter).tensor_shape.length];
		int batchInd = DijTensor.getBatchInd(params.outputList.get(outputCounter).form);
		
		int[] outDimVals = params.outputList.get(outputCounter).tensor_shape;
		int textFieldInd = 0;
		for (int i = 0; i < params.outputList.get(outputCounter).scale.length; i++) {
			try {
				float scaleValue =  1; int haloValue = 0; int offsetValue = 0;
				if (i == batchInd) {
					params.outputList.get(outputCounter).scale[i] = 1;
					params.outputList.get(outputCounter).halo[i] = 0;
					params.outputList.get(outputCounter).offset[i] = 0;
				} else {
					scaleValue = Float.valueOf(firstRowList.get(textFieldInd).getText());
					params.outputList.get(outputCounter).scale[i] = scaleValue;
					haloValue = Integer.valueOf(secondRowList.get(textFieldInd).getText());
					params.outputList.get(outputCounter).halo[i] = haloValue;
					offsetValue = Integer.parseInt(thirdRowList.get(textFieldInd++).getText());
					params.outputList.get(outputCounter).offset[i] = offsetValue;
				}
				// Input Patch Size * Scale factor - 2 * offset = Output Patch Size
				// Output Patch Size - 2 * halo > 0
				int refInd = Index.indexOf(refInput.form.split(""), params.outputList.get(outputCounter).form.split("")[i]);
				if (refInd != -1) {
					float yieldOutputPatch = (float) refInput.recommended_patch[refInd] * scaleValue - 2 * (float)offsetValue;
					if (refInd != -1 && outDimVals[i] != -1 && yieldOutputPatch != (float)outDimVals[i]) {
						IJ.error("The parameters introduces yield an output patch\nshape of " + yieldOutputPatch
								 + " and the model specifies that it should be " + outDimVals[i]);
						return false;
					}
					float relevantPixels = yieldOutputPatch - (float) (2 * haloValue);
					if (relevantPixels <= 0) {
						IJ.error("The halo chosen is too big for the output patch size.\n"
								+ "2 * halo should be smaller than Input patch size * scale - 2 *offset");
						return false;
					}
				}
			} catch( NumberFormatException ex) {
				IJ.error("Make sure that no text field is empty and\n"
						+ "that they correspond to real numbers.");
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Method to retrieve from the UI the information necessary to build an 
	 * image from the tensor outputed by the model in the case the model has
	 * a Pyramidal structure
	 */
	public static boolean saveOutputDataForImagePyramidalNet(Parameters params) {
		// Save all the information for the output given by the variable 'outputInd'
		// Get the reference tensor
		
		params.outputList.get(outputCounter).sizeOutputPyramid = new int[params.outputList.get(outputCounter).tensor_shape.length];
		int batchInd = DijTensor.getBatchInd(params.outputList.get(outputCounter).form);
		
		int textFieldInd = 0;
		for (int i = 0; i < params.outputList.get(outputCounter).sizeOutputPyramid.length; i++) {
			try {
				int sizeOutputPyramid =  1;
				if (i == batchInd) {
					params.outputList.get(outputCounter).sizeOutputPyramid[i] = 1;
				} else {
					sizeOutputPyramid = Integer.valueOf(firstRowList.get(textFieldInd ++).getText());
					params.outputList.get(outputCounter).sizeOutputPyramid[i] = sizeOutputPyramid;
				}
			} catch( NumberFormatException ex) {
				IJ.error("Make sure that no text field is empty and\n"
						+ "that they correspond to real numbers.");
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Method to retrieve from the UI the information necessary to build  
	 * whatever object is needed for the output tensor
	 */
	public static boolean saveOutputData(Parameters params) {
		// If the methods saving the info were successful, wasSaved=true
		boolean wasSaved = false;
		if (params.outputList.get(outputCounter).tensorType.contains("image") && !params.pyramidalNetwork) {
			wasSaved = saveOutputDataForImage(params);
		} else if (params.outputList.get(outputCounter).tensorType.contains("image") && params.pyramidalNetwork) {
			wasSaved = saveOutputDataForImagePyramidalNet(params);
		} else {
			wasSaved = saveOutputDataForList(params);
		}

		int lowInd = cmbRangeLow.getSelectedIndex();
		int highInd = cmbRangeHigh.getSelectedIndex();
		if (lowInd >= highInd) {
			IJ.error("The Data Range has to go from a value to a higher one.");
			return false;
		}
		
		params.outputList.get(outputCounter).dataRange[0] = rangeOptions[lowInd];
		params.outputList.get(outputCounter).dataRange[1] = rangeOptions[highInd];
		params.outputList.get(outputCounter).finished = wasSaved;
		
		//completeInfo[outputCounter] = wasSaved;
		return wasSaved;
	}

	private static void getPanelForImagePyramidalNet(Parameters params) {
		
		//pnOutputInfo.setBorder(BorderFactory.createEtchedBorder());
		int[] dimValues = DijTensor.getWorkingDimValues(params.outputList.get(outputCounter).form, params.outputList.get(outputCounter).tensor_shape); 
		String[] dims = DijTensor.getWorkingDims(params.outputList.get(outputCounter).form);

		for (int i = 0; i < dimValues.length; i ++) {
			JLabel dimLetter1 = new JLabel(dims[i]);
			JTextField txt1;
			
			int auxInd = params.outputList.get(outputCounter).form.indexOf(dims[i]);

			txt1 = new JTextField(params.outputList.get(outputCounter).finished ? "" + params.outputList.get(outputCounter).sizeOutputPyramid[auxInd] : "1", 5);
			txt1.setEditable(true);
			
			if (dimValues[i] != -1) {
				txt1.setText("" + dimValues[i]);
				txt1.setEditable(false);
			} else if (dimValues[i] == -1) {
				txt1.setText("" + 0);
				txt1.setEditable(true);
			}

			firstRow.place(0, i + 1, dimLetter1);
			firstRow.place(1, i + 1, txt1);
			
			firstRowList.add(txt1);
		}
		
		JLabel lblFirst	= new JLabel("Output size");
		firstRow.place(0, 0, lblFirst);
		
		pnOutputInfo.setBorder(BorderFactory.createEtchedBorder());
		pnOutputInfo.place(0, 0, lblName);
		//pnOutputInfo.place(1, 0, referenceImage);
		pnOutputInfo.place(2, 0, 2, 1, firstRow);
		pnOutputInfo.place(5, 0, 2, 1,  pnRange);
		pnOutputInfo.setPreferredSize(new Dimension(pnOutputInfo.getWidth(), pnOutputInfo.getHeight()));
	}
	
	private static void getPanelForImage(Parameters params) {
		
		//pnOutputInfo.setBorder(BorderFactory.createEtchedBorder());
		int[] dimValues = DijTensor.getWorkingDimValues(params.outputList.get(outputCounter).form, params.outputList.get(outputCounter).tensor_shape); 
		String[] dims = DijTensor.getWorkingDims(params.outputList.get(outputCounter).form);

		for (int i = 0; i < dimValues.length; i ++) {
			JLabel dimLetter1 = new JLabel(dims[i]);
			JLabel dimLetter2 = new JLabel(dims[i]);
			JLabel dimLetter3 = new JLabel(dims[i]);
			JTextField txt1;
			JTextField txt2;
			JTextField txt3;
			
			int auxInd = params.outputList.get(outputCounter).form.indexOf(dims[i]);

			txt1 = new JTextField(params.outputList.get(outputCounter).finished ? "" + params.outputList.get(outputCounter).scale[auxInd] : "1", 5);
			txt2 = new JTextField(params.outputList.get(outputCounter).finished ? "" + params.outputList.get(outputCounter).halo[auxInd] : "0", 5);
			txt3 = new JTextField(params.outputList.get(outputCounter).finished ? "" + params.outputList.get(outputCounter).offset[auxInd] : "0", 5);
			txt1.setEditable(true);
			
			int inputFixedSize = findFixedInput((String) referenceImage.getSelectedItem(), dims[i], params.inputList);
			
			if (dimValues[i] != -1 && inputFixedSize != -1) {
				float scale = ((float) dimValues[i]) / ((float) inputFixedSize);
				txt1.setText("" + scale);
				txt1.setEditable(true);
			}

			firstRow.place(0, i + 1, dimLetter1);
			firstRow.place(1, i + 1, txt1);
			secondRow.place(0, i + 1, dimLetter2);
			secondRow.place(1, i + 1, txt2);
			thirdRow.place(0, i + 1, dimLetter3);
			thirdRow.place(1, i + 1, txt3);
			
			firstRowList.add(txt1);
			secondRowList.add(txt2);
			thirdRowList.add(txt3);
		}

		JLabel lblFirst	= new JLabel("Scaling factor");
		JLabel lblSecond = new JLabel("Halo factor");
		JLabel lblThird	= new JLabel("Offset factor");
		firstRow.place(0, 0, lblFirst);
		secondRow.place(0, 0, lblSecond);
		thirdRow.place(0, 0, lblThird);
		
		pnOutputInfo.setBorder(BorderFactory.createEtchedBorder());
		pnOutputInfo.place(0, 0, lblName);
		pnOutputInfo.place(1, 0, referenceImage);
		pnOutputInfo.place(2, 0, 2, 1, firstRow);
		pnOutputInfo.place(3, 0, 2, 1, secondRow);
		pnOutputInfo.place(4, 0, 2, 1, thirdRow);
		pnOutputInfo.place(5, 0, 2, 1,  pnRange);
		pnOutputInfo.setPreferredSize(new Dimension(pnOutputInfo.getWidth(), pnOutputInfo.getHeight()));
	}
	
	/*
	 * Create Jpanel corresponding to list output
	 */
	private static void getPanelForList(Parameters params) {
		
		Dimension dim = pnOutputInfo.getSize();
		pnOutputInfo.removeAll();
		cmbRowList = new ArrayList<JComboBox<String>>();
		
		txtExportDir.setFont(new Font("Arial", Font.BOLD, 14));
		txtExportDir.setForeground(Color.red);
		txtExportDir.setPreferredSize(new Dimension(Constants.width, 25));
		
		int[] dimValues = DijTensor.getWorkingDimValues(params.outputList.get(outputCounter).form, params.outputList.get(outputCounter).tensor_shape); 
		String[] dims = DijTensor.getWorkingDims(params.outputList.get(outputCounter).form);

		lblName.setText(params.outputList.get(outputCounter).name);
		for (int i = 0; i < dimValues.length; i ++) {
			JLabel dimLetter1 = new JLabel(""+ dims[i] + " (size=" + dimValues[i] + ")");
			JComboBox<String> txt1;

			txt1 = new JComboBox<String>(new String[] {"Rows", "Columns"});
			txt1.setEditable(false);
			firstRow.place(0, i + 1, dimLetter1);
			firstRow.place(1, i + 1, txt1);
			cmbRowList.add(txt1);
		}
		pnOutputInfo.place(0, 0, lblName);
		pnOutputInfo.place(1, 0, 2, 1, new JLabel("Type: list"));
		/*
		pnOutputInfo.place(1, 0, 2, 1, new JLabel("Set the directory where the list created will be saved."));
		pnOutputInfo.place(2, 0, 2, 1, txtExportDir);
		*/
		pnOutputInfo.place(2, 0, 2, 1, firstRow);
		pnOutputInfo.setPreferredSize(dim);
	}
	
	private static int findFixedInput(String referenceInput, String dim, List<DijTensor> inputTensors) {
		DijTensor referenceTensor = null;
		int fixed = -1;
		for (DijTensor inp : inputTensors) {
			if (referenceInput.equals(inp.name)) {
				referenceTensor = inp;
				break;
			}
		}
		if (referenceTensor != null) {
			int ind = Index.indexOf(referenceTensor.form.split(""), dim);
			if (ind != -1 && referenceTensor.step[ind] == 0) {
				fixed = referenceTensor.minimum_size[ind];
			}
		}
		return fixed;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Parameters params = parent.getDeepPlugin().params;
		if (e.getSource() == bnNextOutput) {
			if (saveOutputData(params)) {
				outputCounter ++;
			}
		} else if (e.getSource() == bnPrevOutput) {
			outputCounter --;
		}
		updateInterface(params);
	}

}
