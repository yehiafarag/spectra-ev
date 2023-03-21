/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.spectraev.ml;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import no.probe.uib.mgfevaluator.model.TraningDataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.type.DataType;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

/**
 *
 * @author yfa041
 */
public class DataReader {

    /**
     * The schema of data structure.
     */
    private StructType schema;
    /**
     * Charset of file.
     */
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * Reads a limited number of records from a CSV file.
     *
     * @param path the input file path.
     * @param limit the number number of records to read.
     * @throws IOException when fails to read the file.
     * @return the data frame.
     */
    public DataFrame initDataFrame(TraningDataset dataset, int limit) {
        if (schema == null) {
            // infer the schema from top 1000 rows.
            schema = inferSchema(dataset, Math.min(1000, limit));
        }

        return readData(dataset, limit);
    }

    private DataFrame readData(TraningDataset dataset, int limit) {
        if (schema == null) {
            // infer the schema from top 1000 rows.
            throw new IllegalStateException("The schema is not set or inferred.");
        }
        List<Tuple> rows = new ArrayList<>();    
        for (int j = 1; j < dataset.getData().length; j++) {
            Object[] row = dataset.getData()[j].clone();
            rows.add(Tuple.of(row, schema));
            if (rows.size() >= limit) {
                break;
            }
        }       
        schema = schema.boxed(rows);
       
        return DataFrame.of(rows, schema);

    }

    /**
     * Infer the schema from the top n rows.
     * <ol>
     * <li>Infer type of each row.</li>
     * <li>Merge row types to find common type</li>
     * <li>String type by default.</li>
     * </ol>
     *
     * @param reader the file reader.
     * @param limit the number of records to read.
     * @throws IOException when fails to read the file.
     * @return the data frame.
     */
    public StructType inferSchema(TraningDataset dataset, int limit)  {

        Object[] names = dataset.getData()[0];
        DataType[] types = new DataType[names.length];
        Object[] dataRow = dataset.getData()[1];
        int cIndex = 0;
        for (Object obj : dataRow) {
            types[cIndex++] = DataType.infer((obj + "").trim());
           
        }
        int k = 0;
        for (int j = 2; j < dataset.getData().length; j++) {
            Object[] row = dataset.getData()[j];
            for (int i = 0; i < names.length; i++) {
                types[i] = DataType.coerce(types[i], DataType.infer((row[i] + "").trim()));
            }

            if (++k >= limit) {
                break;
            }
        }

        StructField[] fields = new StructField[names.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = new StructField(names[i] + "", types[i] == null ? DataTypes.StringType : types[i]); 
        }
        return DataTypes.struct(fields);

    }
}
