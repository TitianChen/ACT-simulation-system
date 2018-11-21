/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
/** 
 * 邻接矩阵法表示图 
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 * 
 */  
public class MatrixGraph implements Graph {  
    private static final int defaultSize = 10; //默认尺寸; 
    private int maxLen;  //矩阵的最大长度  
    private int edgeNum; //边的条数   
    protected List<Object> vertexs; //顶点矩阵  
    public Edge edges[][];  //邻接矩阵
    
     /* 临时保存路径节点的栈 */  
    public static Stack<Object> stack = new Stack<Object>();  
    
    /* 存储路径的集合 */  
  
    public static Queue<Object> que = new ArrayDeque<>();
    
      
    private enum Visit{unvisited, visited};  
    /** 
     * 构造函数 
     */  
    public MatrixGraph() {  
        maxLen = defaultSize;  
        vertexs  = new ArrayList<>();  
        edges = new MatrixEdge[maxLen][maxLen]; 
    }  
    /** 
     * 构造函数 
     * @param vexs 顶点的数组 
     */  
    public MatrixGraph(Object vexs[]) {  
        maxLen = vexs.length;  
        vertexs  = new ArrayList<>();  
        edges = new MatrixEdge[maxLen][maxLen];  
        for(int i=0; i<maxLen; i++) {  
            vertexs.add(vexs[i]);  
        }
        for(int i=0;i<maxLen;i++){
            for(int j=0;j<maxLen;j++){
                edges[i][j] = new MatrixEdge(0);
            }
        }
    }  
    @Override  
    public void addEdge(Object v1, Object v2, double weight) {  
        int i1 = vertexs.indexOf(v1); //返回索引位置,找不到时返回-1;
        int i2 = vertexs.indexOf(v2);  
        if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) {  
            edges[i1][i2] = new MatrixEdge(v1, v2, null, weight);  
            edgeNum ++;  
        } else {  
            throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！");  
        }  
    }  
    @Override  
    public void addEdge(Object v1, Object v2, Object info, double weight) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);
        if (v1 !=  null && v2 != null){
            if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) { 
                edges[i1][i2] = new MatrixEdge( v1, v2, info, weight);//v1指向v2 
                edgeNum ++;  
            } else {
                throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！"); 
            } 
        } 
    }  
    @Override  
    public void addVex(Object v) {  
        //加顶点
        vertexs.add(v);
        if(vertexs.size() > maxLen) {  
            expand();//扩展edge[][]  
        }  
    }  
    private void expand() {  
        MatrixEdge newEdges[][] = new MatrixEdge[vertexs.size()][vertexs.size()];  
        for(int i=0; i<maxLen; i++) {  
            for(int j=0; j<maxLen; j++) {  
                newEdges[i][j] = (MatrixEdge) edges[i][j]; 
            }  
        }   
        maxLen = vertexs.size();
        edges = newEdges;  
        for(int i=0;i<maxLen;i++){
            for(int j=0;j<maxLen;j++){
                if (edges[i][j] == null){
                    edges[i][j] = new MatrixEdge(0);
                }
            }
        }
        System.out.println(this.printGraph());
    }  
    @Override  
    public int getEdgeSize() {  
        return edgeNum;  
    }  
    @Override  
    public int getVertexSize() {  
        return vertexs.size();  
    }  
    @Override  
    public void removeEdge(Object v1, Object v2) {  
        //移边;
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) {  
            if(edges[i1][i2] == null) {  
                try {  
                    throw new Exception("该边不存在！");  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            } else {  
                edges[i1][i2] = null;  
                edgeNum --;  
            }  
        } else {  
            throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！");  
        }  
    }  
    @Override  
    public void removeVex(Object v) {  
        //移点;
        int index = vertexs.indexOf(v);  
        int n = vertexs.size();  
        vertexs.remove(index);  
        for(int i=0; i<n; i++){  
            edges[i][n-1] = null;  
            edges[n-1][i] = null;  
        }  
    }  
    @Override  
    public String printGraph() {  
        StringBuilder strb = new StringBuilder();  
        int n = getVertexSize();  
        for (int i = 0; i < n; i++) {  
            for(int j = 0; j < n; j++) {  
                //System.out.print(i+"->");
                //System.out.print(j+"->");
                //System.out.print(n+"->");
                //if (edges[i][j] == null)
                    //System.out.print("&&");
                //System.out.println(edges[i][j].weight);
                strb.append("  ").append(edges[i][j]);
            }  
            strb.append("\n");  
        }  
        return strb.toString();  
    }  
    @Override  
    public void clear() {  
        maxLen = defaultSize;  
        vertexs.clear();  
        edges = null;
        que.clear();
        stack.clear();
    }   
    @Override  
    public String bfs(Object o) {  
     /** 
     * 打印图的顶点 
     * @return 
     */  
        Visit visit[] = new Visit[vertexs.size()];  
        for(int i=0; i < vertexs.size(); i++){  
            visit[i] = Visit.unvisited; 
        }
        StringBuilder strb = new StringBuilder();  
        bfs(o, visit, strb);  
        return strb.toString();  
    }  
    private void bfs(Object o, Visit[] visit, StringBuilder strb) {  
        Queue<Object> queue = new ArrayDeque<>();  
        int n = vertexs.indexOf(o);//o在vertexs里面的位置编号;  
        strb.append("\n"); 
        strb.append(o);
        strb.append("\t");  
        visit[n] = Visit.visited;     
        queue.offer(o);//队列中加入o
        while(!queue.isEmpty()) {  
            Object u = queue.poll();//取出一个,并删除;
            Object v = getFirstVertex(u);//得到与之相连的第一个点;
            while(null != v) {  
                if(Visit.unvisited == visit[vertexs.indexOf(v)]) {  
                    strb.append(v);
                    strb.append("\t");  
                    visit[vertexs.indexOf(v)] = Visit.visited;  
                    queue.offer(v);  
                }
                v = getNextVertex(u, v);  
            } 
        }  
    }  
    /* 判断节点是否在栈中 */  
    public static boolean isNodeInStack(Object node)  
    {  
        Iterator<Object> it = stack.iterator();  
        while (it.hasNext()) {  
            Object node1 = it.next();  
            if (node.equals(node1) == true)  
                return true;  
        }  
        return false;  
    }  
    /* 此时栈中的节点组成一条所求路径，转储并打印输出 */  
    public static void showAndSavePath()  
    {  
        Object[] o = stack.toArray();
        for (int i = 0; i < o.length; i++) {  
            Object nNode = o[i]; 
            if(i < (o.length - 1)){
                que.add(nNode);
            //    System.out.print(nNode + "->");
            }else{
                que.add(nNode);
                que.add("end");
            //    System.out.print(nNode);
            }  
        }  
        //System.out.println("\n");  
    }
    @Override
    public ArrayList<Object> getRelation1Nodes(Object Node){
        int num =  this.vertexs.indexOf(Node);
        ArrayList<Object> list = new ArrayList<>();
        for(int i = 0;i < edges[0].length;i++)  
        {
            if (this.edges[num][i].getWeight() != 0)//////
                list.add(this.vertexs.get(i));
        }
        return list;
    }
    /* 
     * 寻找路径的方法  
     * cNode: 当前的起始节点currentNode 
     * pNode: 当前起始节点的上一节点previousNode 
     * sNode: 最初的起始节点startNode 
     * eNode: 终点endNode 
     */
    @Override
    public boolean getPaths(Object cNode, Object pNode, Object sNode, Object eNode) {  
        Object nNode = null;
        if(cNode.equals(sNode) == true){
            que.clear();
            stack.clear();
        }
        /* 如果符合条件判断说明出现环路，不能再顺着该路径继续寻路，返回false */  
        if (cNode != null && pNode != null && cNode.equals(pNode) == true)  
            return false;  
        if (cNode != null) {  
            int i = 0;  
            /* 起始节点入栈 */  
            stack.push(cNode); 
            /* 如果该起始节点就是终点，说明找到一条路径 */  
            if (cNode.equals(eNode) == true)  
            {  
                /* 转储并打印输出该路径，返回true */  
                showAndSavePath();  
                return true;  
            }  
            /* 如果不是,继续寻路 */  
            else  
            {  
                /*  
                 * 从与当前起始节点cNode有连接关系的节点集中按顺序遍历得到一个节点 
                 * 作为下一次递归寻路时的起始节点  
                 */
                if(this.getRelation1Nodes(cNode).isEmpty() == false){ 
                    nNode = this.getRelation1Nodes(cNode).get(i);}
                else
                    nNode = null;
                while (nNode != null) {  
                    /* 
                     * 如果nNode是最初的起始节点或者nNode就是cNode的上一节点或者nNode已经在栈中 ，  
                     * 说明产生环路 ，应重新在与当前起始节点有连接关系的节点集中寻找nNode 
                     */  
                    if (pNode != null  
                            && (nNode.equals(sNode) == true || nNode.equals(pNode) == 
                            true || isNodeInStack(nNode))) {  
                        i++;  
                        if (i >= this.getRelation1Nodes(cNode).size())  
                            nNode = null;  
                        else  
                            nNode = this.getRelation1Nodes(cNode).get(i);  
                        continue;  
                    }  
                    /* 以nNode为新的起始节点，当前起始节点cNode为上一节点，递归调用寻路方法 */  
                    if (getPaths(nNode, cNode, sNode, eNode))/* 递归调用 */  
                    {  
                        /* 如果找到一条路径，则弹出栈顶节点 */  
                        stack.pop();  
                    }  
                    /* 继续在与cNode有连接关系的节点集中测试nNode */  
                    i++;  
                    if (i >= this.getRelation1Nodes(cNode).size())  
                        nNode = null;  
                    else  
                        nNode = this.getRelation1Nodes(cNode).get(i);  
                }  
                /*  
                 * 当遍历完所有与cNode有连接关系的节点后， 
                 * 说明在以cNode为起始节点到终点的路径已经全部找到  
                 */  
                stack.pop();  
                return false;  
            }  
        } else  
            return false;  
    }
    public Queue<Object> getQueue(){
        return que;
    }
    public void clearQue(){
        que.clear();
    }
    @Override  
    public String dfs(Object o) {  
    /** 
     * 深度优先遍历 
     * @param o 遍历的初始顶点 
     * @return 遍历的结果 
     */ 
        Visit visit[] = new Visit[vertexs.size()];  
        for(int i=0; i<vertexs.size(); i++)  
            visit[i] = Visit.unvisited;  
        StringBuilder strb = new StringBuilder();  
        dfs(o, visit, strb); 
        return strb.toString();  
    }  
    private void dfs(Object o, Visit[] visit, StringBuilder strb) {  
        int n = vertexs.indexOf(o);  
        strb.append(o);
        strb.append("\t");  
        visit[n] = Visit.visited;  
          
        Object v = getFirstVertex(o);  
        while(null != v) {  
            if(Visit.unvisited == visit[vertexs.indexOf(v)])  
                dfs(v, visit, strb);  
            v = getNextVertex(o, v);  
        }  
    }  
    @Override  
    public Object getFirstVertex(Object v) {  
        int i = vertexs.indexOf(v);  
        if(i<0)  
            throw new ArrayIndexOutOfBoundsException("顶点v不存在！");  
        for(int col=0; col<vertexs.size(); col++)  
            if(edges[i][col] != null)  
                return vertexs.get(col);  
        return null;  
    }  
    @Override  
    public Object getNextVertex(Object v1, Object v2) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        if(i1<0 || i2<0){  
            throw new ArrayIndexOutOfBoundsException("顶点v不存在！"); 
        }
        for(int col = i2 + 1; col < vertexs.size(); col++)
            if(edges[i1][col] != null)  
                return vertexs.get(col); 
        return null;   
    }
}