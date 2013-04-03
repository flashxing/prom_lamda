package cn.edu.nju.software.process.plugins;

import java.io.InputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
	
//�������
	@Plugin(
	name = "Import Multiset", 
	parameterLabels = { "Filename" }, 
	returnLabels = { "MultisetList" }, 
	returnTypes = { MultisetList.class })
	//����������
	@UIImportPlugin(
	//����������ļ��ᱻת��ΪPost������ProM�н��д���
	description = "Mul",
	//����ʶ����Զ����ļ���׺��Ϊmul
	extensions = { "mul" })

	public class MultisetImportPlugin extends AbstractImportPlugin {
		//ʵ��importFromStream�ӿ�
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
