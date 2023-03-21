/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes.handlers;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import no.probe.uib.mgfevaluator.processes.DatasetUtilities;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;

/**
 *
 * @author yfa041
 */
public class DataStoreHandler {

    private final String storedDataFolderURL = "src\\main\\resources\\\\traningdata";
    private final DatasetUtilities datasetUtilities = new DatasetUtilities();
    private final ExecutorService executorService;

    public DataStoreHandler() {
        this.executorService = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public Set<Dataset> loadStoredData() {
        Set<Dataset> datasets = new LinkedHashSet<>();
        // Java code to illustrate reading a
        // CSV file line by line
        Set<Future> futSet = new HashSet<>();
        File director = new File(storedDataFolderURL);
        for (File f : director.listFiles()) {

            Future fut = executorService.submit(() -> {
                Dataset dataset = parseDatasetFile(f);
                if (dataset != null) {
                    datasets.add(dataset);
                }
            });
            futSet.add(fut);

        }
        while (true) {
            boolean breaktask = true;
            for (Future fut : futSet) {
                if (!fut.isDone()) {
                    breaktask = false;
                }
            }
            if (breaktask) {
                break;
            }

        }
        System.out.println("done filling data");

        return datasets;
    }

    private Dataset parseDatasetFile(File f) {
        try {

//              Create an object of filereader
//             class with CSV file as a parameter.
            FileReader filereader = new FileReader(f);
            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;
            String[] header = csvReader.readNext();
            String[] columnsId = new String[header.length - 2];
            List<String> rowIdsList = new ArrayList<>();
            List<double[]> dataAsList = new ArrayList<>();
            Map<String, List<Integer>> groupMap = new HashMap<>();
            for (int i = 0; i < columnsId.length; i++) {
                columnsId[i] = header[i + 1];
            }
            // we are going to read data line by line
            int rowIndex = 0;
            while ((nextRecord = csvReader.readNext()) != null) {
                rowIdsList.add(nextRecord[0]);
                String dataClass = nextRecord[nextRecord.length - 1];
                if (!groupMap.containsKey(dataClass)) {
                    groupMap.put(dataClass, new ArrayList<>());
                }

                groupMap.get(dataClass).add(rowIndex);
                double[] datarow = new double[nextRecord.length - 2];
                for (int columnIndex = 0; columnIndex < datarow.length; columnIndex++) {
                    datarow[columnIndex] = Double.parseDouble(nextRecord[columnIndex + 1]);
                }
                datarow[1] = 0;
                dataAsList.add(datarow);
                rowIndex++;
            }
            double[][] data = new double[dataAsList.size()][];
            boolean[][] missingMesurments = new boolean[dataAsList.size()][15];
            rowIndex = 0;
            for (double[] datarow : dataAsList) {
                data[rowIndex++] = datarow;
            }
            try {
                Dataset dataset = new Dataset(data, rowIdsList.toArray(new String[]{}), columnsId);
                dataset.setMissingMeasurements(missingMesurments);
                for (String groupName : groupMap.keySet()) {
                    datasetUtilities.createRowGroup(dataset, groupName, "", groupName, datasetUtilities.listToArr(groupMap.get(groupName)), true);
                }
                for (Group g : dataset.getColumnGroups()) {
                    if (g.getName().equalsIgnoreCase("ALL")) {
                        g.setActive(true);
                        g.setColor(Color.BLACK);
                        g.setHashColor("#000000");
                        for (int x = 0; x < dataset.getColumnIds().length; x++) {
                            g.addMember(x);
                        }
                        break;
                    }
                }

                dataset.setName(f.getName().replace(".txt", ""));
                return dataset;
            } catch (Exception exp) {
                System.out.println("at load ds error : " + f.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkDataExisted(String fileName) {
        File fileToWrite = new File(storedDataFolderURL, fileName);
        if (fileToWrite.exists()) {
            return true;
        }
        File director = new File(storedDataFolderURL);
        for (File f : director.listFiles()) {
            if (f.getName().contains(fileName)) {
                return true;
            }
        }
        return false;

    }

    public boolean storeDataset(Dataset ds) {
        File fileToWrite = new File(storedDataFolderURL, ds.getName() + ".txt");
        try {
            if (fileToWrite.exists()) {
                return false;
            }
            fileToWrite.createNewFile();

            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(fileToWrite);

            // adding header to csv
            try ( // create CSVWriter object filewriter object as parameter
                    CSVWriter writer = new CSVWriter(outputfile)) {
                // adding header to csv
                String[] header = new String[ds.getColumnIds().length + 2];
                header[0] = "ID";
                header[header.length - 1] = "CLASS";
                for (int i = 0; i < ds.getColumnIds().length; i++) {
                    header[i + 1] = ds.getColumnIds()[i];
                }
                writer.writeNext(header);
                int rowIndex = 0;
                int idIndex = -1;
                int unIdIndex = -1;
                for (Group g : ds.getRowGroups()) {
                    if (g.getName().equalsIgnoreCase("Identified")) {
                        idIndex = ds.getRowGroups().indexOf(g);
                    } else if (g.getName().equalsIgnoreCase("UnIdentified")) {
                        unIdIndex = ds.getRowGroups().indexOf(g);
                    }
                }
                for (double[] row : ds.getData()) {
                    // add data to csv
                    String[] data = new String[row.length + 2];
                    data[0] = ds.getRowIds()[rowIndex];
                    int colIndex = 1;
                    for (double d : row) {
                        data[colIndex++] = d + "";
                    }

                    if (ds.getRowGroups().get(idIndex).hasMember(rowIndex)) {
                        data[colIndex] = ds.getRowGroups().get(idIndex).getName();
                    } else {
                        data[colIndex] = ds.getRowGroups().get(unIdIndex).getName();
                    }
                    writer.writeNext(data);
                    rowIndex++;
                }
                // closing writer connection
            }
        } catch (IOException ex) {
            fileToWrite.delete();
            ex.printStackTrace();
        }

        return true;
    }
}
