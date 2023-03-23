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
import no.probe.uib.mgfevaluator.model.DatasetInfo;
import no.probe.uib.mgfevaluator.model.TraningDataset;
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
    public static int dsInfoLastIndex = 0;

    public DataStoreHandler() {
        this.executorService = new ThreadPoolExecutor(2, 5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public Set<DatasetInfo> loadDatasetInformation() {
        Set<String> unused = loadUnusedDatasetAccessions();
        Set<DatasetInfo> datasetsInfo = new LinkedHashSet<>();
        File f = new File(storedDataFolderURL, "dataset_informaton.txt");
        if (!f.exists()) {
            return datasetsInfo;
        }
        try {

            CSVReader csvReader;
            // create csvReader object passing
            // file reader as a parameter
            try (//              Create an object of filereader
                    //             class with CSV file as a parameter.
                    FileReader filereader = new FileReader(f)) {
                // create csvReader object passing
                // file reader as a parameter
                csvReader = new CSVReader(filereader);
                String[] nextRecord;
                csvReader.readNext();
                // we are going to read data line by line
                int rowIndex = 0;
                while ((nextRecord = csvReader.readNext()) != null) {
                    DatasetInfo datasetInfo = new DatasetInfo();
                    datasetInfo.setIndex(Integer.parseInt(nextRecord[0]));
                    dsInfoLastIndex = datasetInfo.getIndex();
                    datasetInfo.setAccession(nextRecord[1]);
                    if (unused.contains(datasetInfo.getAccession())) {
                        continue;
                    }
                    datasetInfo.setTechnology(nextRecord[2]);
                    datasetInfo.setYear(Integer.parseInt(nextRecord[3]));
                    datasetInfo.setSpect_total_num(Integer.parseInt(nextRecord[4]));
                    datasetInfo.setIdent_num(Integer.parseInt(nextRecord[5]));
                    datasetInfo.setUn_ident_num(Integer.parseInt(nextRecord[6]));
                    datasetInfo.setComparable_col(nextRecord[7]);

                    datasetInfo.setDt_acc(Double.parseDouble(nextRecord[8]));
                    datasetInfo.setRt_acc(Double.parseDouble(nextRecord[9]));
                    datasetInfo.setRt_r2(Double.parseDouble(nextRecord[10]));
                    datasetsInfo.add(datasetInfo);

                }
            }
            csvReader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return datasetsInfo;
    }

    public Set<String> loadUnusedDatasetAccessions() {
        Set<String> unusedDatasetAccessions = new LinkedHashSet<>();
        File f = new File(storedDataFolderURL, "unused_dataset.txt");
        if (!f.exists()) {
            return unusedDatasetAccessions;
        }
        try {

            CSVReader csvReader;
            // create csvReader object passing
            // file reader as a parameter
            try (//              Create an object of filereader
                    //             class with CSV file as a parameter.
                    FileReader filereader = new FileReader(f)) {
                // create csvReader object passing
                // file reader as a parameter
                csvReader = new CSVReader(filereader);
                String[] nextRecord;
                // we are going to read data line by line
                while ((nextRecord = csvReader.readNext()) != null) {
                    unusedDatasetAccessions.add(nextRecord[0]);
                }
            }
            csvReader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return unusedDatasetAccessions;

    }

    public void flagUnusedDatasets(Set<String> toRemoveFiles) {
        File director = new File(storedDataFolderURL);
        for (File f : director.listFiles()) {
            if (f.getName().equalsIgnoreCase("unused_dataset.txt") || f.getName().equalsIgnoreCase("dataset_informaton.txt")) {
                continue;
            }
            String fileName = f.getName().replace(".txt", "");
            if (toRemoveFiles.contains(fileName)) {
                storeUnusedDs(f.getName().replace(".txt", ""));
            }
        }

    }

    public void storeUnusedDs(String dsName) {
        File fileToWrite = new File(storedDataFolderURL, "unused_dataset.txt");
        try {
            if (!fileToWrite.exists()) {
                fileToWrite.createNewFile();
            }
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(fileToWrite, true);
            try ( // create CSVWriter object filewriter object as parameter
                    CSVWriter writer = new CSVWriter(outputfile)) {
                writer.writeNext(new String[]{dsName});
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void storeDatasetInformation(TraningDataset tds) {
        File fileToWrite = new File(storedDataFolderURL, "dataset_informaton.txt");

        try {
            boolean addHeader = false;
            if (!fileToWrite.exists()) {
                fileToWrite.createNewFile();
                addHeader = true;
            }
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(fileToWrite, true);

            try ( // create CSVWriter object filewriter object as parameter
                    CSVWriter writer = new CSVWriter(outputfile)) {

                // adding header to csv
                if (addHeader) {
                    String[] header = new String[]{"index", "accession", "technology", "year", "spect_total_num", "ident_num", "un_ident_num", "comparable_col", "dt_acc", "rt_acc", "rt_r2"};
                    writer.writeNext(header);
                }

                int idnum = 0;
                int unIdnum = 0;
                for (Group g : tds.getSourceDataset().getRowGroups()) {
                    if (g.getName().equalsIgnoreCase("Identified")) {
                        idnum = g.getMembers().length;
                    } else if (g.getName().equalsIgnoreCase("UnIdentified")) {
                        unIdnum = g.getMembers().length;
                    }
                }
                String[] row = new String[]{(dsInfoLastIndex + 1) + "", tds.getDatasetName().split("__")[0], tds.getDatasetName().split("__")[1], tds.getDatasetName().split("__")[2].split("-")[0], tds.getSourceDataset().getDataLength() + "", idnum + "", unIdnum + "", tds.getSelectedFeaturesKey(), tds.getDtAccurcy() + "", tds.getRtAccurcy() + "", tds.getRtR2() + ""};
                writer.writeNext(row);
                dsInfoLastIndex++;
                // closing writer connection
//                writer.close();
//                outputfile.close();
            }
        } catch (IOException ex) {
            fileToWrite.delete();
            ex.printStackTrace();
        }

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

    public Dataset loadDataset(String datasetName) {
        File director = new File(storedDataFolderURL);
        for (File f : director.listFiles()) {
            if (f.getName().equalsIgnoreCase(datasetName + ".txt")) {
                return parseDatasetFile(f);
            }

        }
        return null;

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
                filereader.close();
                csvReader.close();
                System.gc();

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
        File director = new File(storedDataFolderURL);
        for (File f : director.listFiles()) {
            if (f.getName().contains(fileName)) {
                return true;
            }
        }
        return false;

    }

    public boolean storeDataset(Dataset ds) {
        File fileToWrite = new File(storedDataFolderURL, ds.getName().split("__")[0] + ".txt");
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
