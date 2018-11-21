/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

/** 
 * 邻接矩阵表示法表示的图 
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 * 
 */  
public class MatrixEdge extends Edge {  
    private final Object v1, v2;  
    /** 
     * 构造函数 
     */  
    public MatrixEdge(){
        v1 = null;  
        v2 = null;  
        this.info = null;  
        this.weight = 0; 
    }
    
    public MatrixEdge(double weight) {  
        v1 = null;  
        v2 = null;  
        this.info = null;  
        this.weight = weight;  
    }  
    /** 
     * 构造函数 
     * @param v1 第一个顶点 
     * @param v2 第二个顶点 
     * @param info 边的信息 
     * @param weight 权值 
     */  
    public MatrixEdge( Object v1, Object v2, Object info, double weight ) {  
        super(info, weight);  
        this.v1 = v1;  
        this.v2 = v2;  
    }  
    @Override
    public double getWeight(){
        if(v1 == null && v2 == null)
            this.setWeight(0);
        return this.weight;
    }
    @Override  
    public Object getFirstVertex() {  
        return v1;  
    }  
    @Override  
    public Object getSecondVertex() {  
        return v2;  
    }  
    @Override  
    public int compareTo(Object o) {  
        Edge e = (Edge)o;  
        if(this.weight > e.getWeight())  
            return 1;  
        else if(this.weight < e.getWeight())  
            return -1;  
        else  
            return 0;  
    }  
    @Override  
    public boolean equals(Object obj) {  
        return ((Edge)obj).info.equals(info) &&  ((Edge)obj).getWeight() == this.weight;  
    }  
    @Override  
    public String toString() {  
        //return "边信息：" + info + "\t权值：" + weight + "\t顶点1:" + getFirstVertex() + "\t顶点2：" + getSecondVertex();  
        return "" + weight;  
    }  
}  

