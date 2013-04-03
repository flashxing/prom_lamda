package cn.edu.nju.software.process.plugins;

import java.io.InputStream;

import cn.edu.nju.software.Models.WFLoad;
import cn.edu.nju.software.Models.WFMultiset;

public class MultisetList {
	WFMultiset[] ms;
   
	public MultisetList(String filePath) {
		ms = WFLoad.fromFile(filePath);
	}
	public MultisetList(InputStream input) {
		ms = WFLoad.fromFile(input);
	}
	public WFMultiset[] getMs() {
		return ms;
	}

	public void setMs(WFMultiset[] ms) {
		this.ms = ms;
	}
	
}
