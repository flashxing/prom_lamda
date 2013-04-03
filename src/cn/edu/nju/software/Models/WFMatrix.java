package cn.edu.nju.software.Models;

import java.util.ArrayList;
import java.util.Arrays;


public class WFMatrix {
	public int[][] dependency;
	public boolean[][] parallelism;
	public int[] sum;
	public WFMultiset[] ms;
	public WFMatrix(WFMultiset[] MS, int size) {
		this.ms = MS;
		//size额外加一，是为了让数组索引从1�?��，这样方便计�?
		size++;
		this.dependency = new int[size][size];
		this.parallelism = new boolean[size][size];
		this.sum = new int[size];
		this.calcMatrix();
	}
	private boolean isParall(int a, int b){
		/**
		 * 参看论文中的Definition 9
		 * There don’t exist task t∈T, t[Θ1], t[Θ2]∈MS  such that
         * a属于Θ1,b不属于�? and b属于Θ2,a不属于�?
		 */
		/**
		 * 这里采用的�?路是分别找出a属于但b不属于的MS,(a_not_b)
		 * 以及b属于但a不属于的MS，（b_not_a)�?
		 * 然后看这两个集合中是否有相同元素，如果有则说明存在t∈T满足条件，即a和b不满足潜在并行的关系�?
		 */
		ArrayList<Integer> a_not_b = new ArrayList<Integer>();
		ArrayList<Integer> b_not_a = new ArrayList<Integer>();
		for(int i=0;i<this.ms.length;i++){
			WFMultiset m = this.ms[i];
			int[] posts = m.posts;
			/**
			 * m.posts必须已经排好�?
			 */
			int a_idx = Arrays.binarySearch(posts, a);
			int b_idx = Arrays.binarySearch(posts, b);
			if(a_idx>=0 && b_idx<0){
				a_not_b.add(m.id);
			}
			if(b_idx>=0 && a_idx<0){
				b_not_a.add(m.id);
			}
		}
		/**
		 * 这里判断两个集合中是否有相同元素使用的方法很直接，复杂度也很高�?可以考虑优化，比如a_not_b和b_not_a
		 * 中应该都是已经排好序的�?
		 */
		for(int i=0;i<a_not_b.size();i++){
			for(int j=0;j<b_not_a.size();j++){
				if(b_not_a.get(j)==a_not_b.get(i)){
					return false;
				}
			}
		}
		return true;
		
	}
	private void calcMatrix(){
		for(int i=0;i<this.ms.length;i++){
			WFMultiset m = this.ms[i];
			if(m.id<=0 || m.id>this.ms.length){
				/**
				 * 不合法的id
				 */
				continue;
			}
			this.sum[m.id] += m.num;
			for(int j=0;j<m.posts.length;j++){
				this.dependency[m.id][m.posts[j]] += m.num;
				
				for(int k=j+1;k<m.posts.length;k++){
				
					int _a = m.posts[j],_b=m.posts[k];
					if(this.isParall(_a,_b)){
						this.parallelism[_a][_b] = true;
						this.parallelism[_b][_a] = true;
					}
				}
			}
		}
	}
	public void output(){
		StringBuilder sb = new StringBuilder();
		sb.append("w\t");
		for(int i=1;i<this.dependency.length;i++){
			sb.append('t').append(i).append('\t');
		}
		sb.append("sum\n");
		for(int i=1;i<this.dependency.length;i++){
			sb.append('t').append(i).append('\t');
			for(int j=1;j<this.dependency.length;j++){
				sb.append(this.dependency[i][j]).append('\t');
			}
			sb.append(this.sum[i]).append('\n');
		}
		System.out.println(sb.toString());
		System.out.println("-----------------");
		sb.setLength(0);
		
		sb.append("||\t");
		for(int i=1;i<this.parallelism.length;i++){
			sb.append('t').append(i).append('\t');
		}
		sb.append('\n');
		for(int i=1;i<this.parallelism.length;i++){
			sb.append('t').append(i).append('\t');
			for(int j=1;j<this.parallelism.length;j++){
				sb.append(this.parallelism[i][j]==true?1:0).append('\t');
			}
			sb.append('\n');
		}
		System.out.println(sb.toString());
	}
}
