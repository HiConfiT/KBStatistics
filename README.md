# KBStatistics - A Knowledge Base Statistics Tool

| *version*                                                                         | *status* |
|-----------------------------------------------------------------------------------|----------|
| [1.3.1](https://github.com/manleviet/CA-CDR-V2/releases/tag/kbstatistics-v1.3.1)  | latest   |

`kbstatistics` is a Java `jar` tool that calculates statistics of given knowledge bases.

### Features

The tool analysis and prints out the following statistics of given knowledge bases:

1. **General statistics** (for all types of knowledge base)
- The knowledge base name
- The knowledge base source
- Number of variables
- Number of constraints
- Number of Choco variables
- Number of Choco constraints
- The consistency of the knowledge base

2. **Statistics for feature model**
- The CTC ratio
- The number of features
- The number of relationships
- The number of cross-tree constraints
- The number of MANDATORY relationships
- The number of OPTIONAL relationships
- The number of ALTERNATIVE relationships
- The number of OR relationships
- The number of REQUIRES constraints
- The number of EXCLUDES constraints

## Supported knowledge bases

- _Feature models_ from [SPLOT], [FeatureIDE], [Glencoe], and other tools. You can find some feature model examples in [here].
- _PC_, _Renault_, and _Camera_ from [CLib].

## Usage

**Download**: [Latest (v1.3.1)]{: .label .label-green }

**Requirements**: [Latest Java]{: .label .label-green }

**Syntax**:
```bash
java -jar kbstatistics.jar [-h] [-kb <PC>|<Renault>|<Camera>] [-fm <feature_model_name>] [-fm-dir <path_to_folder>] [-out <path_to_file>]
```

If the parameter `-out` isn't specified, the statistics results will be saved in the file `statistics.txt`.

**Examples**:
- Print out the help
```bash
java -jar kbstatistics.jar -h
```
- Saving the statistics of the _PC_ knowledge base on the default file (`statistics.txt`)
```bash
java -jar kbstatistics.jar -kb PC
```
- Saving the statistics of the _PC_ knowledge base on `pc_results.txt `
```bash
java -jar kbstatistics.jar -kb PC -out ./pc_results.txt
```
- Saving the statistics of the _PC_ and _Renault_ knowledge bases on the default file (`statistics.txt`)
```bash
java -jar kbstatistics.jar -kb PC Renault
```
- Saving the statistics of the smartwatch feature model on the default file (`statistics.txt`)
```bash
java -jar kbstatistics.jar -fm smartwatch.sxfm
```
- Saving the statistics of feature models in the folder `fms` on the default file (`statistics.txt`)
```bash
java -jar kbstatistics.jar -fm-dir ./fms
```
- Saving the statistics of _PC_, _Renault_, and feature models in the folder `fms` on the default file (`statistics.txt`)
```bash
java -jar kbstatistics.jar -fm-dir ./fms -kb PC Renault
```

[Latest (V1.3.1)]: https://github.com/manleviet/CA-CDR-V2/releases/tag/kbstatistics-v1.3.1
[SPLOT]: http://www.splot-research.org
[CLib]: https://www.itu.dk/research/cla/externals/clib/
[FeatureIDE]: https://featureide.github.io
[Glencoe]: https://glencoe.hochschule-trier.de
[here]: https://github.com/manleviet/KBStatistics/tree/main/src/test/resources/fms
[Latest Java]: https://www.java.com/en/download/manual.jsp