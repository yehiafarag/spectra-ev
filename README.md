# 🔍 SpectraEv: Predicting Spectrum Identifiability in Proteomics

**SpectraEv** is a lightweight tool for evaluating the identifiability of MS/MS spectra in mass spectrometry–based proteomics. It helps researchers assess whether a given dataset's identification rate aligns with expected performance, based on intrinsic spectrum properties.

## 🚀 Overview

Mass spectrometry datasets often show high variability in spectrum identification rates. SpectraEv addresses this by analyzing sequence tag confidence and open search results to classify spectra into four distinct categories:

- ✅ **Confident tag + Identified**  
- ❓ **Confident tag + Not identified**  
- ⚠️ **No tag + Identified**  
- ❌ **No tag + Not identified**

Spectra with confident tags are typically identifiable, while those without tags are not. This classification provides insight into why certain spectra remain unidentified and enables fast prediction of dataset quality.

## 🔧 Features

- Sequence tag extraction and confidence scoring  
- Open search comparison for spectrum classification  
- Fast prediction of identification rates  
- Insightful categorization of spectra for downstream analysis  
- Compatible with standard proteomics formats (e.g., mzML, mzIdentML)


