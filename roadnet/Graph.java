/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

import java.util.ArrayList;

/** 
 * 图的接口 
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 * 
 */  
public interface Graph {  
    /** 
     * 添加顶点 
     * @param v 
     */  
    public void addVex(Object v);  
    /** 
     * 添加边 
     * @param v1 第一个顶点 
     * @param v2 第二个顶点 
     * @param weight 权值 
     */  
    public void addEdge(Object v1, Object v2, double weight);  
    /** 
     * 添加边 
    * @param v1 第一个顶点 
     * @param v2 第二个顶点 
     * @param info 边信息 
     * @param weight 权值 
     */  
    public void addEdge(Object v1, Object v2, Object info, double weight);  
    /** 
     * 置空图 
     */  
    public void clear();  
    /** 
     * 获得顶点v的第一个邻接结点 
     * @param v 顶点 
     * @return 顶点v的第一个邻接结点 
     */  
    public Object getFirstVertex(Object v);  
    /** 
     * 在图G中寻找v1结点的邻接结点v2的下一个邻接结点 
     * @param v1 顶点 
     * @param v2 v1的一个邻接结点 
     * @return 邻接v1的在v2后的一个结点 
     */  
    public Object getNextVertex(Object v1, Object v2);  
    /** 
     * 获得顶点的个数 
     * @return 
     */  
    public int getVertexSize();  
    /** 
     *获得边的条数 
     * @return 
     */  
    public int getEdgeSize();  
    /** 
     * 移除顶点 
     * @param v 顶点 
     */  
    public void removeVex(Object v);  
    /** 
     * 移除边 
     * @param v1 顶点1 
     * @param v2 顶点2 
     */  
    public void removeEdge(Object v1, Object v2);  
    /** 
     * 深度优先遍历 
     * @param o 遍历的初始顶点 
     * @return 遍历的结果 
     */  
    public String dfs(Object o);
   // public Queue dfs(Object start,Object end);
    /** 
     * 广度优先遍历 
     * @param o 遍历的初始顶点 
     * @return 遍历的结果 
     */  
    public String bfs(Object o);  
    /////

    /**
     *
     * @param cNode current node
     * @param pNode previous node 
     * @param sNode: start node 
     * @param eNode: end node
     * @return 
     */
    public boolean getPaths(Object cNode, Object pNode, Object sNode, Object eNode);
    public ArrayList getRelation1Nodes(Object Node);
    /** 
     * 打印图的各顶点 
     * @return 
     */  
    public String printGraph();  
}  
