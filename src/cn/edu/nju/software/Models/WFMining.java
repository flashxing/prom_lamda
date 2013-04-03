package cn.edu.nju.software.Models;

import java.util.ArrayList;


public class WFMining {
	public String[]	Tw;
	public WFPair[]	 Yw;
	public Integer[] Ti;
	public Integer[] To;
	/**
	 * 使用方法�?
	 * 先如下方式调用，
	 * WFMultiset[] ms = WFLoad.fromFile(filename);
	 * WFMining wfm = new WFMining();
	 * wfm.mine(ms);
	 * 然后直接访问公开成员变量�?
	 * wfm.Tw
	 * wfm.Yw
	 *
	 * Tw，Yw的含义同论文，其中Tw是task的名称的字符串数组�?
	 * Yw是元组数组，left, right公开成员变量代表两边的task的id列表(ArrayList<Integer>类型)�?
	 * 要得到task的名称，直接使用比如  wfm.Tw[wfm.Yw[3].left.get(1)]
	 */
	
	public static void main(String[] args){
	/*	WFMultiset[] ms = {
				new WFMultiset(1,8, new int[]{2}),
				new WFMultiset(2,8, new int[]{4,9}),
				new WFMultiset(4,4, new int[]{6,8}),
				new WFMultiset(6,16, new int[]{8}),
				new WFMultiset(8,8, new int[]{9}),
				new WFMultiset(9,8, new int[]{11}),
				new WFMultiset(6,8, new int[]{7}),
				new WFMultiset(7,8, new int[]{6}),
				new WFMultiset(4,12, new int[]{5,6}),
				new WFMultiset(5,12, new int[]{8}),
				new WFMultiset(5,3, new int[]{5}),
				new WFMultiset(1,8, new int[]{3}),
				new WFMultiset(3,8, new int[]{4,10}),
				new WFMultiset(8,8, new int[]{10}),
				new WFMultiset(10,8, new int[]{11}),
				new WFMultiset(11,16)
		};
		
	/*	String r = "1,2;2,3;3,4;4,5;5,6;6,7,8;7,9;9,10;10,11;8,12;12,13;13,14;14,15,16,17;15,19;16,18;17,19;18,19;19,20;20,21,22,23;21,24;22,24;23,25;11,24,25;25,24;24,26;26,27;27,28;28,29;29";
		//String r = "1,2;2,3;3,4;4,5;5";
		String[] rs = r.split(";");
		WFMultiset[] ms = new WFMultiset[rs.length];
		for(int i=0;i<rs.length;i++){
			String[] _r = rs[i].split(",");
			int[] posts = new int[_r.length-1];
			for(int j=0;j<_r.length-1;j++){
				posts[j] = Integer.parseInt(_r[j+1]);
			}
			ms[i] = new WFMultiset(Integer.parseInt(_r[0]), 1, posts);
		} */
		
		WFMultiset[] ms = WFLoad.fromFile("d:\\log.txt");
		for(int i=0;i<ms.length;i++){
			System.out.print(ms[i].id+":"+ms[i].name+"   ,  ");
		}
		System.out.println();
		WFMining m = new WFMining();
		m.mine(ms);
	}
	
	private boolean addIfNotIn(ArrayList<WFPair> pl, WFPair p){
		int _s = pl.size();
		for(int z=0;z<_s;z++){
			WFPair sp = pl.get(z);
			if(sp.leftHash.equals(p.leftHash) && sp.rightHash.equals(p.rightHash)){
				return false;
			}
		}
		pl.add(p);
		return true;
	}
	/**
	 * 
	 * @param ms
	 * 
	 * ms 数组的id�?�?��到task的最大�?�?
	 * 
	 */
	public void mine(WFMultiset[] ms){
		int _len = 0;
		for(int i=0;i<ms.length;i++){
			if(_len<ms[i].id)
				_len = ms[i].id;
		}
		/*
		 * Tw 额外�?，为了方便数组读�?
		 */
		this.Tw = new String[_len+1];
		
		ArrayList<Integer> tmp_ti = new ArrayList<Integer>(), tmp_to = new ArrayList<Integer>();
		
		for(int i=0;i<ms.length;i++){
			WFMultiset m = ms[i];
			this.Tw[m.id] = m.name;
			if(tmp_ti.indexOf(m.id)<0)
				tmp_ti.add(m.id);
		}
		
		WFMatrix mat = new WFMatrix(ms, _len);
		
		mat.output();
		
		ArrayList<WFPair> pl = new ArrayList<WFPair>();
		
		for(int i=0;i<ms.length;i++){
			WFMultiset m = ms[i];
			if(m.posts.length==0 && tmp_to.indexOf(m.id)<0)
				tmp_to.add(m.id);
			
			for(int j=0;j<m.posts.length;j++){
				int __p = m.posts[j], __i = tmp_ti.indexOf(__p);
				if(__i>=0)
					tmp_ti.remove(__i);
				pl.add(new WFPair(m.id, __p));
			}
		}
		
		this.Ti = (Integer[])tmp_ti.toArray(new Integer[tmp_ti.size()]);
		this.To = (Integer[])tmp_to.toArray(new Integer[tmp_to.size()]);
		
		int debug = 0;
		boolean _new = true;
		int _size = 0;
		/**
		 * debug是为了防止死循环
		 */
		
		out : 
		while(_new && debug < 10000){
			_size = pl.size();
			//System.out.println("cur size:"+_size);
			_new = false;
			for(int i=0;i<_size;i++){
				for(int j=i+1;j<_size;j++){
			
					WFPair p1 = pl.get(i), p2 = pl.get(j);
					if(p1.leftHash.equals(p2.leftHash) && this.addIfNotIn(pl,p1.leftMerge(p2))==true){
						_new = true;
					} else if(p1.rightHash.equals(p2.rightHash) && addIfNotIn(pl, p1.rightMerge(p2))==true ){
						_new = true;
					}
					if(_new == true){
//						for(int t=0;t<pl.size();t++){
//							System.out.print(pl.get(t).toString());
//						}
//						System.out.println();
						debug++;
						continue out;
					}
				}
			}
			
			System.out.print("while finish...\n");
		}
		//System.out.println("debug:"+debug);
		if(debug>=10000){
			System.err.print("调试计数达到�?��值，可能发生死循�?..");
		}
		_size = pl.size();
		
		ArrayList<WFPair> tmp_Yw = new ArrayList<WFPair>();
		out_for:
		for(int i=0;i<_size;i++){
			WFPair p = pl.get(i);
			Integer[] l = (Integer[])p.left.toArray(new Integer[p.left.size()]), r = (Integer[])p.right.toArray(new Integer[p.right.size()]);
			
			for(int j=0;j<l.length;j++){
				for(int k=j+1;k<l.length;k++){
					if(mat.parallelism[l[j]][l[k]]==true){
						//p.deleted = true;
						continue out_for;
					}
				}
			}
			
			for(int j=0;j<r.length;j++){
				
				int __b = r[j];
				for(int k=j+1;k<r.length;k++){
					if(mat.parallelism[__b][r[k]]==true){
						//p.deleted = true;
						continue out_for;
					}
				}
				
				int en = 0;
				for(int k=0;k<l.length;k++){
					en += mat.dependency[l[k]][__b];
				}
				if(en != mat.sum[__b]){
					//p.deleted = true;
					continue out_for;
				}
			}
			
			tmp_Yw.add(p);
			//System.out.print(p.toString());
		}
		
		//System.out.println("\n-------------\n");
		_size = tmp_Yw.size();
		
		for(int i=0;i<_size;i++){
			for(int j=i+1;j<_size;j++){
				WFPair p1 = tmp_Yw.get(i), p2 = tmp_Yw.get(j);
				if(p1.leftHash.contains(p2.leftHash) && p1.rightHash.contains(p2.rightHash)){
					p2.parent = p1;
				} else if(p2.leftHash.contains(p1.leftHash) && p2.rightHash.contains(p1.rightHash)){
					p1.parent = p2;
				}
			}
			
		}
		
		//int n = 0;
		ArrayList<WFPair> Yw = new ArrayList<WFPair>();
		for(int i=0;i<_size;i++){
			WFPair p = tmp_Yw.get(i);
			StringBuilder sb = new StringBuilder();
			if(p.parent == null){
				Yw.add(p);
				sb.append("({");
				for(int k=0;k<p.left.size();k++)
					sb.append(this.Tw[p.left.get(k)]).append(',');
				sb.append("},{");
				for(int k=0;k<p.right.size();k++)
					sb.append(this.Tw[p.right.get(k)]).append(',');
				sb.append("})   ");
				System.out.print(sb.toString());
				//n++;
			}
		}
		
		this.Yw = (WFPair[])Yw.toArray(new WFPair[Yw.size()]);
	
		System.out.println("\nplace size:"+(this.Yw.length+2));
		System.out.println(this.Ti[0]);
		System.out.println(this.To[0]);
	}
}
