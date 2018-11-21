/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template FileMethod, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileMethod {

    public void FileMethod(){
        
    }
    public final void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists())
        {
            if (file.isFile()) 
            {
                file.delete();
            } else if (file.isDirectory())
            {
                File files[] = file.listFiles();
                for (File file1 : files) {
                    this.deleteFile(file1.toString());
                }
            }
            file.delete();
        }else {
            System.out.println("所删除的文件不存在！");
        }
    }
}
