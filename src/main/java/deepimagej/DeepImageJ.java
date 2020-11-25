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

package deepimagej;

import java.awt.TextArea;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.tensorflow.SavedModelBundle;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import deepimagej.tools.Log;
import deepimagej.tools.DijTensor;
import deepimagej.tools.FileTools;
import ij.IJ;
import ij.gui.GenericDialog;

public class DeepImageJ {

	private String					path;
	private Log 					log;
	public String					dirname;
	public Parameters				params;
	private boolean					valid 			= false;
	private boolean					developer		= true;
	public ArrayList<String>		msgChecks		= new ArrayList<String>();
	public ArrayList<String>		msgLoads		= new ArrayList<String>();
	public ArrayList<String[]>		msgArchis		= new ArrayList<String[]>();
	private SavedModelBundle		tfModel			= null;
	private ZooModel<NDList, NDList>torchModel		= null;
	
	public DeepImageJ(String pathModel, String dirname, Log log, boolean dev) {
		String p = pathModel + File.separator + dirname + File.separator;
		this.path = p.replace(File.separator + File.separator, File.separator);
		// Remove double File separators
		this.path = cleanPathStr(p);
		this.log = log;
		this.dirname = dirname;
		this.developer = dev;
		if (!dev && !(new File(path, "model.yaml").isFile())) {
			this.valid = false;
		} else if (dev || new File(path, "model.yaml").isFile()) {
			this.params = new Parameters(valid, path, dev);
			this.params.path2Model = this.path;
			this.valid = checkUser(p, false);
		}
		if (this.valid && dev && this.params.framework.equals("Tensorflow/Pytorch")) {
			askFrameworkGUI();
		}
		if (!dev && this.valid && !this.params.completeConfig) {
			this.valid = false;
		}
	}

	/*
	 * Method that substitutes double path separators ('\\' or '//') by single ones
	 */
	private String cleanPathStr(String p) {
		while (p.indexOf(File.separator + File.separator) != -1) {
			p = p.replace(File.separator + File.separator, File.separator);
		}
		return p;
	}

	public String getPath() {
		return path;
	}
	
	public String getName() {
		String name = params.name.equals("n.a.") ? dirname : params.name;
		return name.replace("\"", "");
	}
	
	public ZooModel<NDList, NDList> getTorchModel() {
		return torchModel;
	}

	public void setTorchModel(ZooModel<NDList, NDList> model) {
		this.torchModel = model;
	}
	
	public SavedModelBundle getTfModel() {
		return tfModel;
	}

	public void setTfModel(SavedModelBundle model) {
		this.tfModel = model;
	}

	public boolean getValid() {
		return this.valid;
	}
	
	static public HashMap<String, DeepImageJ> list(String pathModels, Log log, boolean isDeveloper) {
		HashMap<String, DeepImageJ> list = new HashMap<String, DeepImageJ>();
		File models = new File(pathModels);
		File[] dirs = models.listFiles();
		if (dirs == null) {
			return list;
		}

		for (File dir : dirs) {
			if (dir.isDirectory()) {
				String name = dir.getName();
				DeepImageJ dp = new DeepImageJ(pathModels + File.separator, name, log, isDeveloper);
				if (dp.valid && dp.params != null && dp.params.completeConfig == true) {
					list.put(dp.dirname, dp);
				} else if (dp.valid && dp.params.completeConfig != true) {
					IJ.error("Model " + dp.dirname + " could not load\n"
							+ "because its config.yaml file did not correspond\n"
							+ "to this version of the plugin.");
				}
				
			}
		}
		return list;
	}


	public boolean loadTfModel(boolean archi) {
		log.print("load model from " + path);

		double chrono = System.nanoTime();
		SavedModelBundle model;
		try {
			model = SavedModelBundle.load(path, TensorFlowModel.returnStringTag(params.tag));
			setTfModel(model);
		}
		catch (Exception e) {
			IJ.log("Exception in loading model " + dirname);
			IJ.log(e.toString());
			IJ.log(e.getMessage());
			log.print("Exception in loading model " + dirname);
			return false;
		}
		chrono = (System.nanoTime() - chrono) / 1000000.0;
		log.print("Loaded");
		if (msgLoads.size() == 0) {
			msgLoads.add("Metagraph size: " + model.metaGraphDef().length);
			msgLoads.add("Graph size: " + model.graph().toGraphDef().length);
			msgLoads.add("Loading time: " + chrono + "ms");
		}
		return true;
	}


	public boolean loadPtModel(String path) {
		try {
			URL url = new File(new File(path).getParent()).toURI().toURL();
			
			String modelName = new File(path).getName();
			modelName = modelName.substring(0, modelName.indexOf(".pt"));
			long startTime = System.nanoTime();
			Criteria<NDList, NDList> criteria = Criteria.builder()
			        .setTypes(NDList.class, NDList.class)
			         // only search the model in local directory
			         // "ai.djl.localmodelzoo:{name of the model}"
			        .optModelUrls(url.toString()) // search models in specified path
			        //.optArtifactId("ai.djl.localmodelzoo:resnet_18") // defines which model to load
			        .optModelName(modelName)
			        .optProgress(new ProgressBar()).build();
	
			ZooModel<NDList, NDList> model = ModelZoo.loadModel(criteria);
			this.setTorchModel(model);
			String torchscriptSize = FileTools.getFolderSizeKb(params.selectedModelPath);
			long stopTime = System.nanoTime();
			// Convert nanoseconds into seconds
			String loadingTime = "" + ((stopTime - startTime) / (float) 1000000000);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		} catch (ModelNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (MalformedModelException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			IJ.log("Model not found in the path provided:");
			IJ.log(path);
			e.printStackTrace();
			return false;
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			IJ.log("DeepImageJ could not load the Pytorch model.");
			IJ.log("This is probably because the Visual Studio 2019 Redistributables are missing.");
			IJ.log("In order to be able to load Pytorch models, download Visual Studio 2019 and ");
			IJ.log("its redistributables from teh following links.");
			IJ.log("- https://visualstudio.microsoft.com/es/downloads/");
			IJ.log("- https://support.microsoft.com/en-us/help/2977003/the-latest-supported-visual-c-downloads");
			IJ.log("If the problem persists visit the following link for more info:");
			IJ.log("- http://docs.djl.ai/docs/development/troubleshooting.html#13-unsatisfiedlinkerror-issue");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void writeParameters(TextArea info, ArrayList<String> checks) {
		if (params == null) {
			info.append("No params\n");
			return;
		}
		info.append("----------- BASIC INFO -----------\n");
		info.append("Name: " + params.name.replace("\"", "") + "\n");
		/* TODO remove
		 * info.append(checks.get(0) + "\n");
		// TODO remove
		if (checks.size() == 2) {
			info.append("Size: " + checks.get(1).substring(18) + "\n");
		} else {
			info.append("Size: " + FileTools.getFolderSizeKb(path + "variables") + "\n");
		}
		 */
		info.append("Authors" + "\n");
		for (String auth : params.author)
			info.append("  - " + auth + "\n");
		info.append("References" + "\n");
		// TODO robustness
		for (HashMap<String, String> ref : params.cite) {
			info.append("  - Article: " + ref.get("text") + "\n");
			info.append("    Doi: " + ref.get("doi") + "\n");
		}
		info.append("Framework:" + params.framework + "\n");
		

		info.append("------------ METADATA ------------\n");
		info.append("Tag: " + params.tag + "\n");
		info.append("Signature: " + params.graph + "\n");
		info.append("Allow tiling: " + params.allowPatching + "\n");

		info.append("Dimensions: ");
		/* TODO remove
		for (DijTensor inp : params.inputList) {
			info.append(Arrays.toString(inp.tensor_shape));
			int slices = 1;
			int zInd = Index.indexOf(inp.form.split(""), "Z");
			if (zInd != -1) {slices = inp.tensor_shape[zInd];}
			int channels = 1;
			int cInd = Index.indexOf(inp.form.split(""), "C");
			if (cInd != -1) {channels = inp.tensor_shape[cInd];}
			info.append(" Slices (" + slices + ") Channels (" + channels + ")\n");
		}
		*/
		info.append("Input:");
		for (DijTensor inp2 : params.inputList)
			info.append(" " + inp2.name + " (" + inp2.form + ")");
		info.append("\n");
		info.append("Output:");
		for (DijTensor out : params.outputList)
			info.append(" " + out.name + " (" + out.form + ")");
		info.append("\n");

		info.append("------------ TEST INFO -----------\n");
		info.append("Inputs:" + "\n");
		for (DijTensor inp : params.inputList) {
			info.append("  - Name: " + inp.exampleInput + "\n");
			info.append("    Size: " + inp.inputTestSize + "\n");
			info.append("      x: " + inp.inputPixelSizeX  + "\n");
			info.append("      y: " + inp.inputPixelSizeY  + "\n");
			info.append("      z: " + inp.inputPixelSizeZ  + "\n");			
		}
		info.append("Outputs:" + "\n");
		for (HashMap<String, String> out : params.savedOutputs) {
			// TODO Deicde info.append("  - Name: " + out.name + "\n");
			info.append("  - Type: " + out.get("type") + "\n");
			info.append("     Size: " + out.get("size")  + "\n");		
		}
		info.append("Memory peak: " + params.memoryPeak + "\n");
		info.append("Runtime: " + params.runtime + "\n");
		
	}

	public  boolean checkUser(String path, boolean recurrence) {
		File dir = new File(path);
		if (!dir.exists()) {
			return false;
		}
		if (!dir.isDirectory()) {
			return false;
		}
		boolean validTf = false;
		boolean validPt = false;
		
		File configFile = new File(path + "model.yaml");
		if (!configFile.exists() && !developer) {
			return false;
		}

		File modelFile = new File(path + "saved_model.pb");
		File variableFile = new File(path + "variables");
		if (modelFile.exists() && variableFile.exists()){
			validTf = true; 
			this.params.framework = "Tensorflow";
		}
		
		// If no tf model has been found. Look for a pytorch torchscript model
		if (findPytorchModel(dir)) {
			this.params.selectedModelPath = dir.getAbsolutePath();
			// TODO check checksum
			validPt = true;
			this.params.framework = "Pytorch";
		}
		
		if (validTf && validPt)
			this.params.framework = "Tensorflow/Pytorch";
		
		// TODO optimise with the name found in the yaml file
		if (!validTf && !validPt && !recurrence) {
			// Find zipped biozoo model
			findZippedBiozooModel(dir);
			return checkUser(path, true);
		}
		
		return validTf || validPt;
	}
	
	/*
	 * Method returns true if a torchscript model is found inside
	 * of the folder provided
	 */
	// TODO separate finding and unzipping
	public static boolean findZippedBiozooModel(File modelFolder) {
		for (String file : modelFolder.list()) {
			if (file.contains("tensorflow_saved_model_bundle.zip")) {
				try {
					FileTools.unzipFolder(new File(modelFolder.getAbsolutePath() + File.separator + file), modelFolder.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					IJ.error("Error unzipping: " + file);
				}
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Method returns true if a torchscript model is found inside
	 * of the folder provided
	 */
	public static boolean findPytorchModel(File modelFolder) {
		for (String file : modelFolder.list()) {
			if (file.contains(".pt"))
				return true;
		}
		return false;
	}
	
	/* TODO
	 * Method returns true if the checksum is the same
	 * for the torchscript model
	 */
	public static boolean pytorchChecksum(File modelFolder) {
		for (String file : modelFolder.list()) {
			if (file.contains(".pt"))
				return true;
		}
		return false;
	}
	
	public void askFrameworkGUI() {
		GenericDialog dlg = new GenericDialog("Choose model framework");
		dlg.addMessage("The folder provided contained both a Tensorflow and a Pytorch model");
		dlg.addMessage("Select which do you want to load.");
		dlg.addChoice("Select framework", new String[]{"Tensorflow", "Pytorch"}, "Tensorflow");
		dlg.showDialog();
		if (dlg.wasCanceled()) {
			dlg.dispose();
			return;
		}
		this.params.framework = dlg.getNextChoice();
	}

}

