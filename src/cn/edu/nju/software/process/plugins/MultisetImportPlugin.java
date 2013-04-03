package cn.edu.nju.software.process.plugins;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
	
//插件定义
	@Plugin(
	name = "Import Multiset", 
	parameterLabels = { "Filename" }, 
	returnLabels = { "MultisetList" }, 
	returnTypes = { MultisetList.class })
	//导入插件定义
	@UIImportPlugin(
	//表明传入的文件会被转化为Post类型在ProM中进行传输
	description = "Mul",
	//表明识别的自定义文件后缀名为mul
	extensions = { "mul" })

	public class MultisetImportPlugin extends AbstractImportPlugin {
		//实现importFromStream接口
		@Override
		protected MultisetList  importFromStream(
				final PluginContext context,final InputStream input, final String filename,
				final long fileSizeInBytes) {
			try {
				context.getFutureResult(0).setLabel("MultisetList imported from " + filename);
                 MultisetList mulList=new MultisetList(input);
				return mulList;
			} catch (Exception e) {
				// Don't care if this fails
				System.out.println("shit!");
				e.printStackTrace();
			}
			return null;
		}
	}
