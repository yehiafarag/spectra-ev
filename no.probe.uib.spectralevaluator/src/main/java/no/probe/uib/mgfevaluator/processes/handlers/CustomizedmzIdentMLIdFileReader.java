/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.probe.uib.mgfevaluator.processes.handlers;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.baseNaturalLogPowerMinusValue;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.baseTenPowerMinusValue;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.baseTenPowerPlusValue;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.baseTwoPowerMinusValue;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.noConversion;
import static com.compomics.util.experiment.io.identification.idfilereaders.MzIdentMLIdfileReader.RawValueConversionType.oneMinusValue;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.flat.SimpleFileReader;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.xml.bind.JAXBException;
import no.probe.uib.mgfevaluator.model.ExtendedPeptideAssumption;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author yfa041
 */
public class CustomizedmzIdentMLIdFileReader {

    /**
     * List of software used to create this file according to the file.
     */
    private HashMap<String, ArrayList<String>> tempSoftwareVersions = new HashMap<>();
    /**
     * The list of software according to the scores found.
     */
    private HashMap<String, ArrayList<String>> softwareVersions = new HashMap<>();
    /**
     * The mzIdentML file.
     */
    private File mzIdentMLFile;
    /**
     * The name of the mzIdentML file.
     */
    private String mzIdentMLFileName;
    /**
     * A temporary peptide map used by the custom parser only. Key: peptide
     * id/ref, element: the peptide object.
     */
    private HashMap<String, SimplePeptide> tempPeptideMap;
    /**
     * A temporary peptide evidence id to peptide ref map used by the custom
     * parser only. Key: peptide evidence id, element: the peptide ref.
     */
    private HashMap<String, String> tempPeptideEvidenceMap;
    /**
     * A map of the spectrum file names. Key: spectrum id/ref, element: spectrum
     * file name.
     */
    private HashMap<String, String> spectrumFileNameMap;
    /**
     * The list of fixed modifications extracted by the custom parser.
     */
    private ArrayList<SearchModificationCustom> fixedModificationsCustomParser;
  
    /**
     * The spectrum provider.
     */
    private SpectrumProvider spectrumProvider;
    /**
     * Set if the amino acid combinations are to be expanded. For example
     * replacing X's.
     */
    private boolean expandAaCombinations=true;
    

    /**
     * Constructor for an mzIdentML result file reader.
     *
     * @param mzIdentMLFile the mzIdentML file
     *
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws IOException if an IOException occurs
     */
    public CustomizedmzIdentMLIdFileReader(
            File mzIdentMLFile
    )
            throws IOException {
        this.mzIdentMLFile = mzIdentMLFile;
        this.mzIdentMLFileName = IoUtil.getFileName(mzIdentMLFile);
    }

    public ArrayList<SpectrumMatch> getAllSpectrumMatches(
            SpectrumProvider spectrumProvider,String updated_name
    )
            throws IOException, SQLException, ClassNotFoundException, InterruptedException, JAXBException, XmlPullParserException {

        this.spectrumProvider = spectrumProvider;
        return parseFile(updated_name);

    }

    /**
     * Parse the mzid file.
     *
     * @return the list of spectrum matches
     */
    private ArrayList<SpectrumMatch> parseFile(String updated_name) throws XmlPullParserException, IOException {

        ArrayList<SpectrumMatch> result = new ArrayList<>();

        // create the pull parser
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        // create a reader for the input file
        try (SimpleFileReader reader = SimpleFileReader.getFileReader(mzIdentMLFile)) {
            // set the XML Pull Parser to read from this reader
            parser.setInput(reader.getReader());
            // start the parsing
            int type = parser.next();
            tempPeptideMap = new HashMap<>();
            tempPeptideEvidenceMap = new HashMap<>();
            spectrumFileNameMap = new HashMap<>();
            fixedModificationsCustomParser = new ArrayList<>();

            // reset the software versions to keep only the advocates which were used for scoring
            softwareVersions.clear();

            // get the analysis software, the spectra data,the peptides and the psms
            while (type != XmlPullParser.END_DOCUMENT) {

                if (type == XmlPullParser.START_TAG && parser.getName().equals("AnalysisSoftware")) {
                    parseSoftware(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("Peptide")) {
                    parsePeptide(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("PeptideEvidence")) {
                    parsePeptideEvidence(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("SpectraData")) {
                    parseSpectraData(parser, spectrumFileNameMap,updated_name);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("ModificationParams")) {
                    parseFixedModifications(parser);
                } else if (type == XmlPullParser.START_TAG && parser.getName().equals("SpectrumIdentificationResult")) {
                    parsePsm(parser, result);
                }

                type = parser.next();
            }
        }

        return result;
    }

    /**
     * Parse the list of fixed modifications.
     *
     * @param parser the XML parser
     */
    private void parseFixedModifications(
            XmlPullParser parser
    ) throws XmlPullParserException, IOException {

        parser.next();
        parser.next();

        if (parser.getName() != null && !parser.getName().equals("ModificationParams")) {

            while (parser.getName().equalsIgnoreCase("SearchModification")) {

                String residues = null;
                Double massDelta = null;
                boolean fixed = false;
                ArrayList<String> modRuleCvTerms = new ArrayList<>();
                ArrayList<String> modCvTerms = new ArrayList<>();

                for (int i = 0; i < parser.getAttributeCount(); i++) {
                    String attributeName = parser.getAttributeName(i);

                    if (attributeName.equalsIgnoreCase("residues")) {
                        residues = parser.getAttributeValue(i);
                    } else if (attributeName.equalsIgnoreCase("massDelta")) {
                        massDelta = Double.parseDouble(parser.getAttributeValue(i));
                    } else if (attributeName.equalsIgnoreCase("fixedMod")) {
                        fixed = Boolean.parseBoolean(parser.getAttributeValue(i));
                    }
                }

                parser.next();
                parser.next();

                if (parser.getName() != null && parser.getName().equals("SpecificityRules")) {

                    parser.next();

                    if (parser.getName() == null) { // no idea why this is needed for ms-gf+...
                        parser.next();
                    }

                    while (parser.getName() != null && parser.getName().equals("cvParam")) {

                        if (parser.getName().equals("cvParam")) {

                            String accession = null;

                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                String attributeName = parser.getAttributeName(i);

                                if (attributeName.equalsIgnoreCase("accession")) {
                                    accession = parser.getAttributeValue(i);
                                }
                            }

                            modRuleCvTerms.add(accession);
                        }

                        parser.next();
                        parser.next();
                        parser.next();
                    }

                    parser.next();
                    if (parser.getName() == null) { // don't get why this is needed for ms-gf+...
                        parser.next();
                    }
                }

                while (parser.getName() != null && (parser.getName().equals("cvParam") || parser.getName().equals("userParam"))) {

                    if (parser.getName().equals("cvParam")) {

                        String accession = null;

                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String attributeName = parser.getAttributeName(i);

                            if (attributeName.equalsIgnoreCase("accession")) {
                                accession = parser.getAttributeValue(i);
                            }
                        }

                        if (accession != null && !accession.equalsIgnoreCase("MS:1002504")) { // ignore MS:1002504 - modification index
                            modCvTerms.add(accession);
                        }
                    }

                    parser.next();
                    parser.next();
                    parser.next();
                }

                if (fixed && !modCvTerms.isEmpty()) {

                    for (String tempCvTerm : modCvTerms) {

                        fixedModificationsCustomParser.add(new CustomizedmzIdentMLIdFileReader.SearchModificationCustom(tempCvTerm, residues, massDelta, modRuleCvTerms));

                    }

                }

                parser.next();
                parser.next();
            }
        }
    }

    /**
     * Returns true of the given modification is to be considered as variable.
     *
     * @param accession the accession of the modification
     * @param location the location of the modification
     * @param monoMassDelta the delta mass of the modification
     * @param peptideSequence the peptide sequence of the modification
     */
    private boolean isVariableModification(
            CustomizedmzIdentMLIdFileReader.SearchModificationCustom modification,
            String peptideSequence
    ) {

        boolean fixed = false;
        int peptidePtmLocation = modification.getLocation();

        // check if the current modification is a fixed modification
        for (CustomizedmzIdentMLIdFileReader.SearchModificationCustom fixedModification : fixedModificationsCustomParser) {

            // find the mass difference, needed if the cv term is not provided
            double massDifference = Math.abs(fixedModification.getMassDelta() - modification.getMassDelta());

            // compare accession numbers (excluding  MS:1001460 - unknown modification) and if not equal then compare the delta masses
            if ((modification.getAccession().equals(fixedModification.getAccession()) && !modification.getAccession().equals("MS:1001460"))
                    || massDifference < 0.00001) { // @TODO: is there a better way of doing this..?

                boolean allRules = true;
                ArrayList<String> specificityRuleCvTerms = fixedModification.getModRuleCvTerms();
                if (specificityRuleCvTerms != null && !specificityRuleCvTerms.isEmpty()) {
                    for (String specificityRuleCvTerm : specificityRuleCvTerms) {
                        if (specificityRuleCvTerm.equals("MS:1001189") || specificityRuleCvTerm.equals("MS:1002057")) {
                            if (peptidePtmLocation != 0) {
                                allRules = false;
                                break;
                            }
                        } else if (specificityRuleCvTerm.equals("MS:1001190") || specificityRuleCvTerm.equals("MS:1002058")) {
                            if (peptidePtmLocation != peptideSequence.length() + 1) {
                                allRules = false;
                                break;
                            }
                        } else if (specificityRuleCvTerm.equals("MS:1001875")) {
                            // can we use this?
                        } else if (specificityRuleCvTerm.equals("MS:1001876")) {
                            // not a specificity rule but the scoring of the specificity
                        } else {
                            throw new IllegalArgumentException("Specificity rule " + specificityRuleCvTerm + " not recognized.");
                        }

                        if (!allRules) {
                            break;
                        }
                    }
                } else if (peptidePtmLocation == 0 || peptidePtmLocation == peptideSequence.length() + 1) {
                    // no specificity rules, so the modification cannot be terminal (but can still be on the first or last residue)
                    allRules = false;
                }

                if (allRules) {
                    String residues = fixedModification.getResidues();
                    if (residues == null || residues.isEmpty()) {
                        fixed = true;
                        break;
                    } else {
                        char aaAtLocation;
                        if (peptidePtmLocation == 0) {
                            aaAtLocation = peptideSequence.charAt(0);
                        } else if (peptidePtmLocation == peptideSequence.length() + 1) {
                            aaAtLocation = peptideSequence.charAt(peptidePtmLocation - 2);
                        } else {
                            aaAtLocation = peptideSequence.charAt(peptidePtmLocation - 1);
                        }
                        for (char residue : residues.toCharArray()) {
                            if (residue == aaAtLocation || residue == '.') {
                                fixed = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (fixed) {
                break;
            }
        }

        return !fixed;
    }

    /**
     * Parse a SpectraData element.
     *
     * @param parser the XML parser
     * @param spectrumFileNameMap the spectrum file name map
     */
    private void parseSpectraData(
            XmlPullParser parser,
            HashMap<String, String> spectrumFileNameMap,String updated_name
    ) {

        String location = updated_name;
        String id = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {

            String attributeName = parser.getAttributeName(i);

            if (attributeName.equalsIgnoreCase("location")) {
                location = parser.getAttributeValue(i);
            } else if (attributeName.equalsIgnoreCase("id")) {
                id = parser.getAttributeValue(i);
            }
        }

        if (location != null && id != null) {

            String fileName = location;

            if (location.lastIndexOf("/") != -1) {
                fileName = location.substring(location.lastIndexOf("/") + 1);
            } else if (location.lastIndexOf("\\") != -1) {
                fileName = location.substring(location.lastIndexOf("\\") + 1);
            }

            //String fileName = new File(new URI(location)).getName(); // @TODO: check if this work cross platform... (if it does the above code could be replaced)
            spectrumFileNameMap.put(id, fileName);
        }
    }

    /**
     * Parse a PSM object.
     *
     * @param parser the XML parser
     * @param result the list to add the extracted PSM to
     */
    private void parsePsm(
            XmlPullParser parser,
            ArrayList<SpectrumMatch> result
    ) throws XmlPullParserException, IOException {

        String spectraDataRef = null;
        String spectrumId = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if (attributeName.equalsIgnoreCase("spectraData_ref")) {
                spectraDataRef = parser.getAttributeValue(i);
            } else if (attributeName.equalsIgnoreCase("spectrumID")) {
                spectrumId = parser.getAttributeValue(i);
            }
        }

        if (spectraDataRef == null || spectrumId == null) {
            throw new IllegalArgumentException("Error parsing SpectrumIdentificationResult!");
        }

        String spectrumTitle = null;

        // get the spectrum file name
        String spectrumFileName = spectrumFileNameMap.get(spectraDataRef);

        // get the spectrum index and potentially the spectrum name
        if (spectrumId.startsWith("index=")) { // @TODO: support more index types
            Integer spectrumIndex = Integer.valueOf(spectrumId.substring(spectrumId.indexOf("=") + 1));
            spectrumTitle = spectrumProvider.getSpectrumTitles(IoUtil.removeExtension(spectrumFileName))[spectrumIndex];
        }

        // set up the spectrum match
        SpectrumMatch currentMatch = new SpectrumMatch(spectrumFileName, spectrumId);

        parser.next();
        int type = parser.next();

        while (type != XmlPullParser.END_TAG && !parser.getName().equals("cvParam")) {

            Integer rank = null;
            String peptideRef = null;
            Integer chargeState = null;
            String spectrumIdItemId = null;
            boolean validated=true;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("rank")) {
                    rank = Integer.parseInt(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("peptide_ref")) {
                    peptideRef = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("chargeState")) {
                    chargeState = Integer.parseInt(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("id")) {
                    spectrumIdItemId = parser.getAttributeValue(i);
                }
                 else if (attributeName.equalsIgnoreCase("passThreshold")) {
                    validated = Boolean.parseBoolean(parser.getAttributeValue(i));
                }
            }

            if (rank == null || chargeState == null || spectrumIdItemId == null) {
                System.out.println("spectrumIdItemId: " + spectrumIdItemId);
                throw new IllegalArgumentException("Error parsing SpectrumIdentificationItem!");
            }

            type = parser.next();

            // read until we get to the peptide evidence references
            while (parser.getName() == null || (parser.getName() != null && !parser.getName().equals("PeptideEvidenceRef"))) {
                type = parser.next();
            }

            // see if we need to get the peptide reference from the peptide evidence element
            String peptideEvidenceRef = null;
            if (peptideRef == null) {
                if (parser.getName() != null && parser.getName().equals("PeptideEvidenceRef")) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attributeName = parser.getAttributeName(i);
                        if (attributeName.equalsIgnoreCase("peptideEvidence_ref")) {
                            peptideEvidenceRef = parser.getAttributeValue(0);
                            break;
                        }
                    }
                    type = parser.next();
                }
            }

            if (peptideRef == null && peptideEvidenceRef == null) {
                System.out.println("spectrumIdItemId: " + spectrumIdItemId);
                throw new IllegalArgumentException("Error parsing SpectrumIdentificationItem!");
            }

            // skip the (rest of) the peptide evidence references
            while (parser.getName() == null || (parser.getName() != null && parser.getName().equals("PeptideEvidenceRef"))) {
                type = parser.next();
            }

            // skip the fragmentation
            if (parser.getName().equals("Fragmentation")) {
                parser.next();
                while (parser.getName() == null || (parser.getName() != null && !parser.getName().equals("Fragmentation"))) {
                    parser.next();
                }

                parser.next();
                type = parser.next();
            }

            HashMap<String, Double> eValueMap = new HashMap<>();

            while (parser.getName() != null && (parser.getName().equals("cvParam") || parser.getName().equals("userParam"))) {

                if (parser.getName().equals("cvParam")) {

                    String accession = null;
                    Double value = null;

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        String attributeName = parser.getAttributeName(i);

                        if (attributeName.equalsIgnoreCase("accession")) {
                            accession = parser.getAttributeValue(i);
                        } else if (attributeName.equalsIgnoreCase("value")) {
                            try {
                                value = Double.parseDouble(parser.getAttributeValue(i));
                            } catch (NumberFormatException e) {
                                // ignore, not a number
                            }
                        }
                    }

                    if (value != null) {
                        eValueMap.put(accession, value);
                        
                    }
                }

                parser.next();
                parser.next();
                type = parser.next();
            }

            if (parser.getName().equals("SpectrumIdentificationItem") && type == XmlPullParser.END_TAG) {
                parser.next();
                type = parser.next();
            } else {
                parser.next();
                parser.next();
                type = parser.next();
            }

           
            // get the e-value
            CustomizedmzIdentMLIdFileReader.EValueObject tempEValue = getEValue(eValueMap, spectrumIdItemId);          
            Advocate advocate = tempEValue.getAdvocate();
            Double eValue = tempEValue.getEValue();
            Double rawScore = tempEValue.getRawScore();

            if (rawScore == null) {
                rawScore = eValue;
            }

            // get the peptide reference
            if (peptideRef == null) {
                peptideRef = tempPeptideEvidenceMap.get(peptideEvidenceRef);
            }

            if (!tempPeptideMap.containsKey(peptideRef)) {
                System.out.println("spectrumIdItemId: " + spectrumIdItemId);
                throw new IllegalArgumentException("Error parsing SpectrumIdentificationItem!");
            }

            // get the peptide
            CustomizedmzIdentMLIdFileReader.SimplePeptide tempPeptide = tempPeptideMap.get(peptideRef);

            // create a new peptide
            ArrayList<ModificationMatch> modMatches = new ArrayList<>();

            for (CustomizedmzIdentMLIdFileReader.SearchModificationCustom tempMod : tempPeptide.getModifications()) {

                if (isVariableModification(tempMod, tempPeptide.getPeptideSequence())) {

                    // correct for terminal modifications
                    int location = tempMod.getLocation();

                    if (location == 0) {

                        location = 1; // n-term ptm

                    } else if (location == tempPeptide.getPeptideSequence().length() + 1) {

                        location -= 1; // c-term ptm

                    }

                    modMatches.add(new ModificationMatch(tempMod.getMassDelta() + "@" + tempPeptide.getPeptideSequence().charAt(location - 1), location));

                }
            }

            Peptide peptide = new Peptide(tempPeptide.getPeptideSequence(), modMatches.toArray(new ModificationMatch[modMatches.size()]), true);

            // create the peptide assumption
            ExtendedPeptideAssumption peptideAssumption = new ExtendedPeptideAssumption(
                    peptide,
                    rank,
                    advocate.getIndex(),
                    chargeState,
                    rawScore,
                    eValue,
                    mzIdentMLFileName,
                    validated
            );

            if (expandAaCombinations && AminoAcidSequence.hasCombination(peptideAssumption.getPeptide().getSequence())&&false) {

                ModificationMatch[] previousModificationMatches = peptide.getVariableModifications();

                for (StringBuilder expandedSequence : AminoAcidSequence.getCombinations(peptide.getSequence())) {

                    ModificationMatch[] newModificationMatches = Arrays.stream(previousModificationMatches)
                            .map(modificationMatch -> new ModificationMatch(modificationMatch.getModification(), modificationMatch.getSite()))
                            .toArray(ModificationMatch[]::new);

                    Peptide newPeptide = new Peptide(expandedSequence.toString(), newModificationMatches, true);

                    ExtendedPeptideAssumption newAssumption = new ExtendedPeptideAssumption(
                            newPeptide,
                            peptideAssumption.getRank(),
                            peptideAssumption.getAdvocate(),
                            peptideAssumption.getIdentificationCharge(),
                            rawScore,
                            peptideAssumption.getScore(),
                            peptideAssumption.getIdentificationFile(),peptideAssumption.isValid()
                    );

                    currentMatch.addPeptideAssumption(advocate.getIndex(), newAssumption);

                }
            } else {
               
                currentMatch.addPeptideAssumption(advocate.getIndex(), peptideAssumption);
            }
        }
      

        // get the spectrum title
        while (parser.getName() != null && parser.getName().equals("cvParam")) {

            String accession = null;
            String name = null;
            String value = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("accession")) {
                    accession = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("value")) {
                    value = parser.getAttributeValue(i);
                } else if (attributeName.equalsIgnoreCase("name")) {
                    name = parser.getAttributeValue(i);
                }
            }

            if (accession != null && name != null && value != null) {
                if (accession.equalsIgnoreCase("MS:1000796") || name.equalsIgnoreCase("spectrum title")) {
                    spectrumTitle = value.trim();
                    // remove any html from the title
                    spectrumTitle = URLDecoder.decode(spectrumTitle, "utf-8");
                }
            }

            parser.next();
            parser.next();
            parser.next();
        }

        // update the spectrum key with the correct spectrum title
        if (spectrumTitle != null) {
            currentMatch.setSpectrumTitle(spectrumTitle);
        }

        result.add(currentMatch);
    }

    /**
     * Parse a peptide evidence object.
     *
     * @param parser the XML parser
     */
    private void parsePeptideEvidence(
            XmlPullParser parser
    ) {

        String peptideEvidenceId = null;
        String peptideRef = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if (attributeName.equalsIgnoreCase("id")) {
                peptideEvidenceId = parser.getAttributeValue(i);
            } else if (attributeName.equalsIgnoreCase("peptide_ref")) {
                peptideRef = parser.getAttributeValue(i);
            }
        }

        if (peptideEvidenceId != null && peptideRef != null) {
            tempPeptideEvidenceMap.put(peptideEvidenceId, peptideRef);
        }
    }

    /**
     * Parse a peptide object.
     *
     * @param parser the XML parser
     */
    private void parsePeptide(
            XmlPullParser parser
    ) throws XmlPullParserException, IOException {

        String pepKey = parser.getAttributeValue(0);

        int type = parser.next();
        while (type != XmlPullParser.START_TAG || !parser.getName().equals("PeptideSequence")) {
            type = parser.next();
        }
        type = parser.next();
        String peptideSequence = parser.getText().trim();

        while (parser.getName() == null || (!parser.getName().equals("Peptide") && !parser.getName().equals("Modification"))) {
            type = parser.next();
        }

        ArrayList<CustomizedmzIdentMLIdFileReader.SearchModificationCustom> modifications = new ArrayList<>();

        while (type != XmlPullParser.END_TAG && parser.getName() != null && parser.getName().equals("Modification")) {

            Integer location = null;
            Double monoMassDelta = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("monoisotopicMassDelta")) {
                    monoMassDelta = Double.parseDouble(parser.getAttributeValue(i));
                } else if (attributeName.equalsIgnoreCase("location")) {
                    location = Integer.parseInt(parser.getAttributeValue(i));
                }
            }

            parser.next();
            parser.next();

            String accession = null;

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("accession")) { // note that only the first ptm cv term is used
                    accession = parser.getAttributeValue(i);
                }
            }

//            if (location == null || monoMassDelta == null || accession == null) {
//                throw new IllegalArgumentException("Could not parse PTM!");
//            }

//            modifications.add(new CustomizedmzIdentMLIdFileReader.SearchModificationCustom(accession, location, monoMassDelta));

            parser.next();

            while (parser.getName() == null || (parser.getName() != null && parser.getName().equals("cvParam"))) {
                parser.next();
            }

            parser.next();
            parser.next();
        }

        tempPeptideMap.put(pepKey, new CustomizedmzIdentMLIdFileReader.SimplePeptide(peptideSequence, modifications));
    }

    /**
     * Parse a software object.
     *
     * @param parser the XML parser
     *
     */
    private void parseSoftware(XmlPullParser parser) throws XmlPullParserException, IOException {

        String softwareVersion = null;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attributeName = parser.getAttributeName(i);
            if (attributeName.equalsIgnoreCase("version")) {
                softwareVersion = parser.getAttributeValue(i);
            }
        }

        parser.next();

        while (parser.getName() == null || (parser.getName() != null && !parser.getName().equals("SoftwareName"))) {
            parser.next();
        }

        parser.next();
        if (parser.getName() == null) {
            parser.next();
        }

        String softwareName = null;

        if (parser.getName().equals("cvParam")) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("name")) {
                    softwareName = parser.getAttributeValue(i);
                }
            }
        } else if (parser.getName().equals("userParam")) {
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String attributeName = parser.getAttributeName(i);
                if (attributeName.equalsIgnoreCase("name")) {
                    softwareName = parser.getAttributeValue(i);
                }
            }
        }

        if (softwareName != null && softwareVersion != null) {

            // only keep known software
            if (Advocate.getAdvocate(softwareName) != null) {

                ArrayList<String> versions = tempSoftwareVersions.get(softwareName);

                if (versions == null) {
                    versions = new ArrayList<>();
                    versions.add(softwareVersion);
                    tempSoftwareVersions.put(softwareName, versions);
                } else if (!versions.contains(softwareVersion)) {
                    versions.add(softwareVersion);
                }

                softwareVersions.put(softwareName, versions);
            }
        }
        if(softwareVersions.isEmpty())
            softwareVersions.put("Generic", new ArrayList<>());
        System.out.println("at software are "+softwareVersions);

        softwareVersions.putAll(tempSoftwareVersions);
    }

    /**
     * Returns the extracted e-value details.
     *
     * @param scoreMap the map of the possible e-values
     * @param spectrumIdItemId the spectrum identification ID, only used if no
     * e-value is found
     *
     * @return the extracted e-value details
     */
    private EValueObject getEValue(
            HashMap<String, Double> scoreMap,
            String spectrumIdItemId
    ) {

        String cvTerm; //TODO: select the "best" algorithm or include all?

        // MyriMatch
        cvTerm = "MS:1001589";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.myriMatch, cvTerm, RawValueConversionType.baseNaturalLogPowerMinusValue);
        }
        cvTerm = "MS:1001590";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.myriMatch, cvTerm, RawValueConversionType.baseNaturalLogPowerMinusValue);
        }

        // ms-gf+
        cvTerm = "MS:1002052";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002053";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002056";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002055";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002054";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002049";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msgf, cvTerm, RawValueConversionType.noConversion);
        }

        // PEAKS
        cvTerm = "MS:1002448";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.peaks, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001950";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.peaks, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // X!Tandem
        cvTerm = "MS:1001330";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.xtandem, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001331";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.xtandem, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // OMSSA
        cvTerm = "MS:1001328";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.omssa, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001329";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.omssa, cvTerm, RawValueConversionType.noConversion);
        }

        // MS Amanda
        cvTerm = "MS:1002319";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msAmanda, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Andromeda
        cvTerm = "MS:1002338";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.andromeda, cvTerm, RawValueConversionType.noConversion);
        }

        // Comet
        cvTerm = "MS:1002255";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.comet, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002252";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.comet, cvTerm, RawValueConversionType.baseTenPowerPlusValue);
        }

        // Mascot
        cvTerm = "MS:1001172";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.mascot, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001171";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.mascot, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // PeptideShaker
        cvTerm = "MS:1002466";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.peptideShaker, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1002467";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.peptideShaker, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Byonic
        cvTerm = "MS:1002262";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.byonic, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1002311";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.byonic, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1002265";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.byonic, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002309";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.byonic, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1002266";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.byonic, cvTerm, RawValueConversionType.baseTenPowerPlusValue);
        }

        // MS Fit
        cvTerm = "MS:1001501";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.msFit, cvTerm, RawValueConversionType.noConversion);
        }

        // Phenyx
        cvTerm = "MS:1001396";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.phenyx, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001395";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.phenyx, cvTerm, RawValueConversionType.baseTwoPowerMinusValue);
        }

        // Profound
        cvTerm = "MS:1001499";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proFound, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1001498";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proFound, cvTerm, RawValueConversionType.baseTwoPowerMinusValue);
        }

        // ProteinLynx
        cvTerm = "MS:1001570";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinLynx, cvTerm, RawValueConversionType.baseTenPowerPlusValue);
        }
        cvTerm = "MS:1001569";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinLynx, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // ProteinProspector
        cvTerm = "MS:1002045";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinProspector, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002044";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinProspector, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // ProteinScape
        cvTerm = "MS:1001503";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinScape, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001504";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinScape, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Sequest
        cvTerm = "MS:1001154";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sequest, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001155";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sequest, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1001215";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sequest, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002248";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sequest, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // SQID
        cvTerm = "MS:1001887";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sqid, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Sonar
        cvTerm = "MS:1001502";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.sonar, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // SpectraST
        cvTerm = "MS:1001417";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.spectraST, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // SpectrumMill
        cvTerm = "MS:1001572";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.spectrumMill, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // ZCore
        cvTerm = "MS:1001952";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.zCore, cvTerm, RawValueConversionType.noConversion);
        }

        // Percolator
        cvTerm = "MS:1001491";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.percolator, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001493";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.percolator, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1001492";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.percolator, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Morpheus
        cvTerm = "MS:1002662";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.morpheus, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // MetaMorpheus
        cvTerm = "MS:1002827";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.metaMorpheus, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }

        // Morpheus and MetaMorpheus (PSM-level q-value)
        cvTerm = "MS:1002354";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.morpheus, cvTerm, RawValueConversionType.noConversion); // @TODO: change advocate to metaMorpheus?
        }

        // IdentiPy
        cvTerm = "MS:1002353";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.identiPy, cvTerm, RawValueConversionType.noConversion);
        }
        cvTerm = "MS:1002989";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.identiPy, cvTerm, RawValueConversionType.baseTenPowerMinusValue); // @TODO: could also add "IdentiPy:RHNS" (MS:1002988)?
        }

        // Protein Pilot
        cvTerm = "MS:1001166";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinPilot, cvTerm, RawValueConversionType.baseTenPowerMinusValue);
        }
        cvTerm = "MS:1001167";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.proteinPilot, cvTerm, RawValueConversionType.noConversion);
        }

        // Scaffold
        cvTerm = "MS:1001568";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, Advocate.scaffold, cvTerm, RawValueConversionType.noConversion);
        }

        // Generic q-value
        cvTerm = "MS:1002354";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, getAdvocate(), cvTerm, RawValueConversionType.noConversion);
        }

        // Generic probability/confidence
        cvTerm = "MS:1002357";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, getAdvocate(), cvTerm, RawValueConversionType.oneMinusValue);
        }

        // Generic probability/confidence
        cvTerm = "MS:1002352";
        if (scoreMap.containsKey(cvTerm)) {
            return getEValueObject(scoreMap, getAdvocate(), cvTerm, RawValueConversionType.oneMinusValue);
        }
        scoreMap.put(cvTerm, 0.0);
        return getEValueObject(scoreMap, getAdvocate(), cvTerm, RawValueConversionType.oneMinusValue);
     //   throw new IllegalArgumentException("No e-value found for SpectrumIdentificationItem with ID " + spectrumIdItemId + " in file " + mzIdentMLFileName + ".");
    }

    /**
     * Returns the advocate.
     *
     * @return the advocate
     */
    private Advocate getAdvocate() {
        for (String softwareName : tempSoftwareVersions.keySet()) {
            Advocate advocate = Advocate.getAdvocate(softwareName);
            if (advocate != null) {
                return advocate;
            }
        }
        for (String softwareName : tempSoftwareVersions.keySet()) {
            return Advocate.addUserAdvocate(softwareName);
        }
        return Advocate.genericMzId;
    }

    /**
     * Returns the e-value object for the given CV term, null if not found.
     *
     * @param scoreMap the score map
     * @param advocate the advocate
     * @param cvTerm the CV term to look for
     * @param rawValueConversionType the raw value conversion type
     *
     * @return the e-value object for the given CV term, null if not found
     */
    private CustomizedmzIdentMLIdFileReader.EValueObject getEValueObject(
            HashMap<String, Double> scoreMap,
            Advocate advocate,
            String cvTerm,
            RawValueConversionType rawValueConversionType
    ) {

        CustomizedmzIdentMLIdFileReader.EValueObject eValueObject = null;
        Double eValue = scoreMap.get(cvTerm), rawScore = null;

        if (eValue != null) {

            // convert score to e-value if needed
            switch (rawValueConversionType) {
                case noConversion:
                    // do nothing
                    break;
                case baseTwoPowerMinusValue:
                    eValue = Math.pow(2, -eValue);
                    break;
                case baseTenPowerMinusValue:
                    eValue = Math.pow(10, -eValue);
                    break;
                case baseTenPowerPlusValue:
                    eValue = Math.pow(10, eValue);
                    break;
                case baseNaturalLogPowerMinusValue:
                    eValue = Math.pow(Math.E, -eValue);
                    break;
                case oneMinusValue:
                    eValue = 1 - eValue;
                    break;
            }

            // get the software version
            String name = advocate.getName();
            if (!softwareVersions.containsKey(name)) {
                ArrayList<String> versions = tempSoftwareVersions.get(name);
                if (versions == null) {
                    versions = new ArrayList<>();
                }
                softwareVersions.put(name, versions);
            }

            // create the e-value object
            eValueObject = new CustomizedmzIdentMLIdFileReader.EValueObject(eValue, rawScore, advocate);
        }

        return eValueObject;
    }

    /**
     * The e-value details.
     */
    private class EValueObject {

        /**
         * The e-value.
         */
        private Double eValue;
        /**
         * The advocate.
         */
        private Advocate advocate;
        /**
         * The raw score.
         */
        private Double rawScore;

        /**
         * Create a new EValueObject.
         *
         * @param eValue the e-value
         * @param rawScore the raw score
         * @param advocate the advocate
         */
        public EValueObject(
                Double eValue,
                Double rawScore,
                Advocate advocate
        ) {
            this.eValue = eValue;
            this.rawScore = rawScore;
            this.advocate = advocate;
        }

        /**
         * Returns the e-value.
         *
         * @return the e-value
         */
        public Double getEValue() {
            return eValue;
        }

        /**
         * Returns the raw score.
         *
         * @return the raw score
         */
        public Double getRawScore() {
            return rawScore;
        }

        /**
         * Returns the advocate.
         *
         * @return the advocate
         */
        public Advocate getAdvocate() {
            return advocate;
        }
    }

    /**
     * A modification extracted by the custom parser.
     */
    private class SearchModificationCustom {

        /**
         * The accession.
         */
        private String accession;
        /**
         * The residues.
         */
        private String residues;
        /**
         * The mass delta.
         */
        private double massDelta;
        /**
         * The specificity rule CV terms.
         */
        private ArrayList<String> modRuleCvTerms;
        /**
         * The location of the modification.
         */
        private int location;

        /**
         * Create a new SearchModificationCustom.
         *
         * @param accession the PTM accession
         * @param location the location
         * @param massDelta the mass delta
         */
        public SearchModificationCustom(
                String accession,
                int location,
                double massDelta
        ) {
            this.accession = accession;
            this.location = location;
            this.massDelta = massDelta;
        }

        /**
         * Create a new SearchModificationCustom.
         *
         * @param accession the PTM accession
         * @param residues the residues
         * @param massDelta the mass delta
         * @param modRuleCvTerms the specificity rule CV terms
         */
        public SearchModificationCustom(
                String accession,
                String residues,
                double massDelta,
                ArrayList<String> modRuleCvTerms
        ) {
            this.accession = accession;
            this.residues = residues;
            this.massDelta = massDelta;
            this.modRuleCvTerms = modRuleCvTerms;
        }

        /**
         * Returns the residues.
         *
         * @return the residues
         */
        public String getResidues() {
            return residues;
        }

        /**
         * Returns the mass delta.
         *
         * @return the mass delta
         */
        public double getMassDelta() {
            return massDelta;
        }

        /**
         * Returns the specificity rule CV terms.
         *
         * @return the specificity rule CV terms
         */
        public ArrayList<String> getModRuleCvTerms() {
            return modRuleCvTerms;
        }

        /**
         * Returns the PTM accession.
         *
         * @return the PTM accession
         */
        public String getAccession() {
            return accession;
        }

        /**
         * Returns the location.
         *
         * @return the location
         */
        public int getLocation() {
            return location;
        }
    }

    /**
     * A simple model for a peptide.
     */
    private class SimplePeptide {

        /**
         * The peptide sequence.
         */
        private String peptideSequence;
        /**
         * The modifications.
         */
        private ArrayList<SearchModificationCustom> modifications;

        /**
         * Create a new PeptideCustom object.
         *
         * @param peptideSequence the peptide sequence
         * @param modifications the modifications
         */
        public SimplePeptide(
                String peptideSequence,
                ArrayList<SearchModificationCustom> modifications
        ) {
            this.peptideSequence = peptideSequence;
            this.modifications = modifications;
        }

        /**
         * Returns the peptide sequence.
         *
         * @return the peptideSequence
         */
        public String getPeptideSequence() {
            return peptideSequence;
        }

        /**
         * Returns the modifications.
         *
         * @return the modifications
         */
        public ArrayList<SearchModificationCustom> getModifications() {
            return modifications;
        }
    }
}
