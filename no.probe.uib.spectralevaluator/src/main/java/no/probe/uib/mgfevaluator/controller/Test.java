/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author yfa041
 */
public class Test {
        /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        System.out.println(LocalDateTime.now().toLocalDate());
        
//    DecisionTree tree = new DecisionTree();
//DecisionTreeTest test= new DecisionTreeTest();
//test.testBreastCancer();
        String server = "ftp.pride.ebi.ac.uk";
        int port = 21;
        String user = "anonymous";
        String pass = "";
 
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
//            ftpClient.enterLocalPassiveMode();
//            ftpClient.setFileType(FTP.STREAM_TRANSFER_MODE);
 
            // APPROACH #1: using retrieveFile(String, OutputStream)
            String remoteFile1 = "/pride/data/archive/2021/04/PXD008650/txt_MiFO170522_170727.zip";
            File downloadFile1 = new File("D:/test.zip");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();
 
            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }
 
//            // APPROACH #2: using InputStream retrieveFileStream(String)
//            String remoteFile2 = "/test/song.mp3";
//            File downloadFile2 = new File("D:/Downloads/song.mp3");
//            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
//            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
//            byte[] bytesArray = new byte[4096];
//            int bytesRead = -1;
//            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
//                outputStream2.write(bytesArray, 0, bytesRead);
//            }
// 
//            success = ftpClient.completePendingCommand();
//            if (success) {
//                System.out.println("File #2 has been downloaded successfully.");
//            }
//            outputStream2.close();
//            inputStream.close();
 
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
